package asdf.b;

import java.io.File;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

//@Component
public class IntergrationConfiguration {
 
    private String ftpUploadDir = "ftp://ekode:ekode123@localhost:21/";
 
    @Bean
    public MessageChannel fileInputChannel() {
        return new DirectChannel();
    }
 
    @Bean
    public DirectChannel pgpFileProcessor() {
        return new DirectChannel();
    }
    
    
    @Bean
	public SessionFactory<FTPFile> myFtpsSessionFactory() {
		DefaultFtpSessionFactory sess = new DefaultFtpSessionFactory();
		sess.setHost("localhost");
		sess.setPort(21);
		sess.setUsername("ekode");
		sess.setPassword("ekode123");
		sess.setClientMode(FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE);
		return sess;
	}
    
    
 
    @Bean
//    @InboundChannelAdapter(value = "fileInputChannel", poller = @Poller(fixedDelay = "1000"))
    public MessageSource<File> fileReadingMessageSource() {
        FileReadingMessageSource source = new FileReadingMessageSource();
//        source.setDirectory(new File("/home/administrator/Imagens/test/a"));
        source.setDirectory(new File(ftpUploadDir));
        source.setFilter(new SimplePatternFileListFilter("*.*"));
        source.setScanEachPoll(true);
//        source.setUseWatchService(true);
        source.setAutoCreateDirectory(true);
		
        return source;
    }
    
    @Bean
	public IntegrationFlow outboundFlow() {
		return IntegrationFlows
				.from(fileReadingMessageSource(), 
						spec -> spec.poller(Pollers.fixedDelay(1000, 1000))
				).handle(resultFileHandler()).get();
	}
 
//    @Bean
    @ServiceActivator(inputChannel = "jobChannel", outputChannel = "nullChannel")
    public String launcherWSSSSSSSSSSSS(String jobLauncher) {
    	String res = jobLauncher.concat("XXXXXXXXXX");
        return res;
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