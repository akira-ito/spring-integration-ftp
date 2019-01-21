package ito.akira.edson.ftpstreamingmessagesource;

import java.io.InputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.interceptor.WireTap;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.filters.FtpPersistentAcceptOnceFileListFilter;
import org.springframework.integration.ftp.inbound.FtpStreamingMessageSource;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.handler.LoggingHandler.Level;
import org.springframework.integration.handler.advice.ExpressionEvaluatingRequestHandlerAdvice;
import org.springframework.integration.metadata.SimpleMetadataStore;
import org.springframework.integration.transformer.StreamTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Component
public class TestFtpStreamingMessageSource {

	@Bean
	public SessionFactory<FTPFile> ftpSessionFactory() {
		DefaultFtpSessionFactory sf = new DefaultFtpSessionFactory();
		sf.setHost("localhost");
		sf.setPort(21);
		sf.setUsername("ekode");
		sf.setPassword("ekode123");
		sf.setClientMode(FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE);
//		sf.setTestSession(true);
		return new CachingSessionFactory<FTPFile>(sf);
	}

	@Bean
	@InboundChannelAdapter(channel = "stream", poller = @Poller(fixedDelay = "1000"))
	public MessageSource<InputStream> ftpMessageSource() {
		FtpStreamingMessageSource messageSource = new FtpStreamingMessageSource(template());
		messageSource.setRemoteDirectory("/");
//		messageSource.setFilter(new AcceptAllFileListFilter<>());
//		messageSource.setFilter(new AcceptOnceFileListFilter<>());
		messageSource.setFilter(new FtpPersistentAcceptOnceFileListFilter(new SimpleMetadataStore(), "rotate"));
		messageSource.setMaxFetchSize(1);

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

	@Bean
	@Transformer(inputChannel = "stream", outputChannel = "data")
	public org.springframework.integration.transformer.Transformer transformer() {
		return new StreamTransformer("UTF-8");
	}

	@Bean
	public FtpRemoteFileTemplate template() {
		return new FtpRemoteFileTemplate(ftpSessionFactory());
	}

	@ServiceActivator(inputChannel = "data2", adviceChain = "after")
	@Bean
	public MessageHandler handle() {
		return System.out::println;
	}

	@Bean
	@ServiceActivator(inputChannel = "data")
	public MessageHandler handler() {
		return new MessageHandler() {
			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				System.out.println(message.getPayload());
//				throw new MessagingException("error");
			}
		};
	}

	@Bean
	public ExpressionEvaluatingRequestHandlerAdvice after() {
		ExpressionEvaluatingRequestHandlerAdvice advice = new ExpressionEvaluatingRequestHandlerAdvice();
//		advice.setOnSuccessExpression(new FunctionExpression<Boolean>(a ->  {
//			return true;
//		}));
		advice.setPropagateEvaluationFailures(true);
		return advice;
	}

}
