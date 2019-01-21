package asdf.a;

import java.io.File;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.interceptor.WireTap;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.file.remote.gateway.AbstractRemoteFileOutboundGateway.Option;
import org.springframework.integration.ftp.gateway.FtpOutboundGateway;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.handler.LoggingHandler.Level;
import org.springframework.messaging.MessageChannel;

//@Configuration
//@EnableIntegration
public class BasicIntegrationConfig {
	public String INPUT_DIR = "/home/administrator/Imagens/test/a";
	public String OUTPUT_DIR = "/home/administrator/Imagens/test/b";
	public String FILE_PATTERN = "*.*";

//	@Bean
//	public MessageChannel fileChannel() {
//		return new DirectChannel();
//	}
//
//	@Bean
//	@InboundChannelAdapter(value = "fileChannel", poller = @Poller(fixedDelay = "1000"))
//	public MessageSource<File> fileReadingMessageSource() {
//		FileReadingMessageSource sourceReader = new FileReadingMessageSource();
//		sourceReader.setDirectory(new File(INPUT_DIR));
//		sourceReader.setFilter(new SimplePatternFileListFilter(FILE_PATTERN));
//		return sourceReader;
//	}
//
//	@Bean
//	@ServiceActivator(inputChannel = "fileChannel")
//	public MessageHandler fileWritingMessageHandler() {
//		FileWritingMessageHandler handler = new FileWritingMessageHandler(new File(OUTPUT_DIR));
//		handler.setFileExistsMode(FileExistsMode.REPLACE);
//		handler.setExpectReply(false);
//		return handler;
//	}

//	----------------------------------------

	@Bean
	public DefaultFtpSessionFactory myFtpsSessionFactory() {
		DefaultFtpSessionFactory sess = new DefaultFtpSessionFactory();
		sess.setHost("localhost");
		sess.setPort(21);
		sess.setUsername("ekode");
		sess.setPassword("ekode123");
		sess.setClientMode(FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE);
		return sess;
	}

//	@Bean
//	public FtpInboundFileSynchronizer ftpInboundFileSynchronizer() {
//		FtpInboundFileSynchronizer fileSynchronizer = new FtpInboundFileSynchronizer(myFtpsSessionFactory());
//		fileSynchronizer.setDeleteRemoteFiles(false);
//		fileSynchronizer.setPreserveTimestamp(true);
//		fileSynchronizer.setRemoteDirectory("/");
////		fileSynchronizer.setFilter(new FtpSimplePatternFileListFilter("*"));
//		return fileSynchronizer;
//	}
//
////	@Bean
////	@InboundChannelAdapter(channel = "fromSftpChannel", poller = @Poller(fixedDelay = "100000"))
//	public MessageSource<File> sftpMessageSource() {
//		FtpInboundFileSynchronizingMessageSource source = new FtpInboundFileSynchronizingMessageSource(
//				ftpInboundFileSynchronizer());
//		source.setLocalDirectory(new File("/home/administrator/Imagens/test/a"));
//		source.setAutoCreateLocalDirectory(true);
//		source.setLocalFilter(new AcceptOnceFileListFilter<File>());
//		return source;
//	}
//	
//	@Bean
//	@ServiceActivator(inputChannel = "fromSftpChannel")
//	public MessageHandler resultFileHandler() {
//		return new MessageHandler() {
//			@Override
//			public void handleMessage(Message<?> message) throws MessagingException {
//				Object payload = message.getPayload();
//				if (payload instanceof File) {
//					File f = (File) payload;
//					System.out.println(f.getName());
//				} else {
//					System.out.println(message.getPayload());
//				}
//			}
//		};
//	}
//	
//	@Bean
//    public FtpRemoteFileTemplate template(DefaultFtpSessionFactory sf) {
//		FtpRemoteFileTemplate template = new FtpRemoteFileTemplate(sf);
//		template.setRemoteDirectoryExpression(new LiteralExpression("."));
//        return template;
//    }

//	------------------------------------------------
	@MessagingGateway(defaultRequestChannel = "ftpChannel", defaultReplyChannel = "results")
	public interface Gateway {
		public List<File> fetchFiles(String ftpDirectory);
	}

	@Bean
	@ServiceActivator(inputChannel = "ftpChannel")
	public FtpOutboundGateway gateway() {

		FtpOutboundGateway gateway = new FtpOutboundGateway(myFtpsSessionFactory(), "ls", "payload");

		gateway.setOption(Option.ALL);
//		gateway.setLocalDirectoryExpression(new LiteralExpression("/home/administrator/Imagens/test/a"));
		gateway.setOutputChannelName("results");

		return gateway;
	}

	@Bean
	public MessageChannel results() {
		DirectChannel channel = new DirectChannel();
		channel.addInterceptor(tap());
		return channel;
	}

	@Bean
	public WireTap tap() {
		return new WireTap("logging");
	}

	@ServiceActivator(inputChannel = "logging")
	@Bean
	public LoggingHandler logger() {
		LoggingHandler logger = new LoggingHandler(Level.INFO);
		logger.setLogExpressionString("'Files:' + payload");
		return logger;
	}
}