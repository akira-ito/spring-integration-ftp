package ito.akira.edson.ftpinboundfilesynchronizer;

import java.io.File;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizer;
import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizingMessageSource;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

//@Configurable
public class TestFtpInboundFileSynchronizer {

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
	public FtpInboundFileSynchronizer sftpInboundFileSynchronizer() {
		FtpInboundFileSynchronizer fileSynchronizer = new FtpInboundFileSynchronizer(ftpSessionFactory());
		fileSynchronizer.setRemoteDirectory("/");
		fileSynchronizer.setDeleteRemoteFiles(false);
		fileSynchronizer.setPreserveTimestamp(true);
//		fileSynchronizer.setFilter(new FtpSimplePatternFileListFilter("*"));
		return fileSynchronizer;
	}

	@Bean
	@InboundChannelAdapter(channel = "fromSftpChannel", poller = @Poller(cron = "0/5 * * * * *"))
//	@InboundChannelAdapter(channel = "fromSftpChannel", poller = @Poller(fixedDelay = "100000"))
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
				Object payload = message.getPayload();
				if (payload instanceof File) {
					File f = (File) payload;
					System.out.println(f.getName());
				} else {
					System.out.println(message.getPayload());
				}
			}
		};
	}

}
