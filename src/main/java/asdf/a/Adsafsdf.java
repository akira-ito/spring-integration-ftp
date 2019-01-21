package asdf.a;

import java.io.File;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.ftp.filters.FtpSimplePatternFileListFilter;
import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizer;
import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizingMessageSource;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

//@Configurable
public class Adsafsdf {
	@Bean
	public DefaultFtpSessionFactory sftpSessionFactory() {
		DefaultFtpSessionFactory factory = new DefaultFtpSessionFactory();
		factory.setHost("localhost");
		factory.setPort(21);
		factory.setUsername("ekode");
		factory.setPassword("ekode123");
		return factory;
	}

	@Bean
	public FtpInboundFileSynchronizer sftpInboundFileSynchronizer() {
		FtpInboundFileSynchronizer fileSynchronizer = new FtpInboundFileSynchronizer(sftpSessionFactory());
		fileSynchronizer.setDeleteRemoteFiles(true);
		fileSynchronizer.setRemoteDirectory("/");
//		fileSynchronizer.setFilter(new FtpSimplePatternFileListFilter("/"));
		return fileSynchronizer;
	}

	@Bean
	@InboundChannelAdapter(channel = "fromSftpChannel", poller = @Poller(cron = "0/5 * * * * *"))
	public MessageSource<File> sftpMessageSource() {
		FtpInboundFileSynchronizingMessageSource source = new FtpInboundFileSynchronizingMessageSource(
				sftpInboundFileSynchronizer());
		source.setLocalDirectory(new File("/"));
		source.setAutoCreateLocalDirectory(true);
		source.setLocalFilter(new AcceptOnceFileListFilter<File>());
		return source;
	}

	@Bean
	@ServiceActivator(inputChannel = "fromSftpChannel")
	public MessageHandler resultFileHandler() {
		return new MessageHandler() {
			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				System.err.println(message.getPayload());
			}
		};
	}

}
