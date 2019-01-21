package asdf.a;


import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.interceptor.WireTap;
import org.springframework.integration.ftp.gateway.FtpOutboundGateway;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.handler.LoggingHandler.Level;
import org.springframework.messaging.MessageChannel;
import org.springframework.integration.file.remote.gateway.AbstractRemoteFileOutboundGateway.Option;

//@Configuration
public class FtpConfiguration {
	@ServiceActivator(inputChannel = "ftpLS")
	@Bean
	public FtpOutboundGateway getGW() {
		FtpOutboundGateway gateway = new FtpOutboundGateway(sf(), "ls");
		gateway.setOption(Option.NAME_ONLY);
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

	@Bean
	public DefaultFtpSessionFactory sf() {
		DefaultFtpSessionFactory sf = new DefaultFtpSessionFactory();
		sf.setHost("localhost");
		sf.setPort(21);
		sf.setUsername("ekode");
		sf.setPassword("ekode123");
		return sf;
	}

	
}