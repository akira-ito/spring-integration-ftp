package ito.akira.edson.ftpstreamingmessagesource;

import java.io.File;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.interceptor.WireTap;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.FileNameGenerator;
import org.springframework.integration.ftp.inbound.FtpStreamingMessageSource;
import org.springframework.integration.ftp.outbound.FtpMessageHandler;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.handler.LoggingHandler.Level;
import org.springframework.integration.metadata.ConcurrentMetadataStore;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.integration.transformer.StreamTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import ito.akira.edson.factories.FactoryFilter;

//@Component
public class TestFtpStreamingMessageSource {
	
	@Autowired
	private FactoryFilter factoryFilter;
	
	@Autowired
	private FtpRemoteFileTemplate ftpRemoteFileTemplate;
	
	@Autowired
	private SftpRemoteFileTemplate sftpRemoteFileTemplate;
	
	@Bean
	@InboundChannelAdapter(channel = "stream", poller = @Poller(cron = "0 * * * * ? *"))
	public MessageSource<InputStream> ftpMessageSource(ConcurrentMetadataStore metadataStore) {
		FtpStreamingMessageSource messageSource = new FtpStreamingMessageSource(ftpRemoteFileTemplate);
		messageSource.setRemoteDirectory("/");
		messageSource.setFilter(factoryFilter.createFileListFilter(metadataStore));
		messageSource.setFileInfoJson(false);
		messageSource.setMaxFetchSize(1000);
		return messageSource; 
	}

	@Bean
	public MessageChannel stream() {
		DirectChannel channel = new DirectChannel();
//		channel.addInterceptor(tap());
		return channel;
	}

	@Bean
	public WireTap tap() {
		return new WireTap("logging");
	}

	@Bean
	@ServiceActivator(inputChannel = "logging")
	public LoggingHandler logger() {
		LoggingHandler logger = new LoggingHandler(Level.INFO);
		logger.setLogExpressionString("'Files:' + payload");
		return logger;
	}

//	@Bean
//	@Transformer(inputChannel = "stream", outputChannel = "amqpOutboundChannel")
//	public org.springframework.integration.transformer.Transformer transformer() {
//		return new StreamTransformer("UTF-8");
//	}
	
	@Transformer(inputChannel = "stream", outputChannel = "amqpOutboundChannel")
	public String transform(Message<?> aFile) throws Exception {
		MessageHeaders headers = aFile.getHeaders();
		String remoteDirectory = headers.getOrDefault(FileHeaders.REMOTE_DIRECTORY, "/").toString();
		String fileName = headers.getOrDefault(FileHeaders.FILENAME, "").toString();
		return remoteDirectory + fileName;
	}

	@ServiceActivator(inputChannel = "amqpOutboundChannel")
	@Bean
	public MessageHandler handle() {
		return System.out::println;
	}

//	@Bean
//	@ServiceActivator(inputChannel = "amqpOutboundChannel")
//	public MessageHandler handler() {
//		FtpMessageHandler ftpMessageHandler = new FtpMessageHandler(ftpRemoteFileTemplate);
//		ftpMessageHandler.setRemoteDirectoryExpression(new LiteralExpression("/"));
//		ftpMessageHandler.setFileNameGenerator(new FileNameGenerator() {
//			
//			@Override
//			public String generateFileName(Message<?> message) {
//				return "kkkk"+message.getHeaders().getId();
//			}
//		});
//		return ftpMessageHandler;
//	}
}
