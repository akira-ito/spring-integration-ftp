package asdf.a;

import java.io.File;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.ftp.filters.FtpSimplePatternFileListFilter;
import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizer;
import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizingMessageSource;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.DefaultFtpsSessionFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

//@Configuration
public class OutroConfig {
	
	@Bean
    public DefaultFtpSessionFactory  myFtpsSessionFactory(){
		DefaultFtpSessionFactory sess = new DefaultFtpSessionFactory();
        sess.setHost("localhost");
        sess.setPort(21);
        sess.setUsername("ekode");
        sess.setPassword("ekode123");
        return sess;
    }
	
	@Bean
	public FtpInboundFileSynchronizer ftpInboundFileSynchronizer() {
		FtpInboundFileSynchronizer fileSynchronizer = new FtpInboundFileSynchronizer(myFtpsSessionFactory());
		fileSynchronizer.setDeleteRemoteFiles(false);
		fileSynchronizer.setPreserveTimestamp(true);
//		fileSynchronizer.setRemoteDirectory("");
//		fileSynchronizer.setFilter(new FtpSimplePatternFileListFilter("*.*"));
		return fileSynchronizer;
	}

	@Bean
	@InboundChannelAdapter(channel = "ftpChannel", poller = @Poller(fixedDelay = "500"))
	public MessageSource<File> ftpMessageSource() {
		FtpInboundFileSynchronizingMessageSource source = new FtpInboundFileSynchronizingMessageSource(ftpInboundFileSynchronizer());
		source.setLocalDirectory(new File("/home/administrator/Donwloads"));
		source.setAutoCreateLocalDirectory(true);
		source.setLocalFilter(new AcceptOnceFileListFilter<File>());
		return source;
	}

	@Bean
	@ServiceActivator(inputChannel = "ftpChannel")
	public MessageHandler handler() {
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
