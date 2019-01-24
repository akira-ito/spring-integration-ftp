package ito.akira.edson.config;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;

import com.jcraft.jsch.ChannelSftp.LsEntry;

@Configuration
public class FtpConfig {
	
	@Bean
	public SessionFactory<FTPFile> ftpSessionFactory() {
		DefaultFtpSessionFactory sf = new DefaultFtpSessionFactory();
		sf.setHost("localhost");
		sf.setPort(21);
		sf.setUsername("ekode");
		sf.setPassword("ekode123");
		sf.setClientMode(FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE);
//		sf.setTestSession(true);
		
//		FTPClientConfig config = new FTPClientConfig();
//		sf.setConfig(config);
		
		CachingSessionFactory<FTPFile> cachingSessionFactory = new CachingSessionFactory<FTPFile>(sf);
		cachingSessionFactory.setPoolSize(2);
		cachingSessionFactory.setSessionWaitTimeout(2000);
		cachingSessionFactory.resetCache();
		return cachingSessionFactory;
	}
	
	@Bean
	public SessionFactory<LsEntry> sftpSessionFactory() {
		DefaultSftpSessionFactory sf = new DefaultSftpSessionFactory();
		sf.setHost("172.26.103.37");
		sf.setPort(22);
		sf.setUser("fda");
		sf.setPassword("1q2w3e4r");
		sf.setAllowUnknownKeys(true);
		
		CachingSessionFactory<LsEntry> cachingSessionFactory = new CachingSessionFactory<>(sf);
		cachingSessionFactory.setPoolSize(2);
		cachingSessionFactory.setSessionWaitTimeout(2000);
		cachingSessionFactory.resetCache();
		return sf;
	}

	
	@Bean
	public SftpRemoteFileTemplate sftpRemoteFileTemplate() {
		org.springframework.integration.sftp.session.SftpRemoteFileTemplate sftpRemoteFileTemplate = new SftpRemoteFileTemplate(sftpSessionFactory());
		return sftpRemoteFileTemplate;
	}
	
	@Bean
	public FtpRemoteFileTemplate template() {
		FtpRemoteFileTemplate ftpRemoteFileTemplate = new FtpRemoteFileTemplate(ftpSessionFactory());
		return ftpRemoteFileTemplate;
	}

}
