package ito.akira.edson.sftpstreamingmessagesource;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.filters.ChainFileListFilter;
import org.springframework.integration.metadata.ConcurrentMetadataStore;
import org.springframework.integration.sftp.filters.SftpPersistentAcceptOnceFileListFilter;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.sftp.inbound.SftpStreamingMessageSource;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.ChannelSftp;

@Component
public class TestSftpStreamingMessageSource {

	@Autowired
	private SftpRemoteFileTemplate sftpRemoteFileTemplate;

	@Bean
	@InboundChannelAdapter(channel = "stream", poller = @Poller(cron = "0 * * * * *"))
//	@InboundChannelAdapter(channel = "stream", poller = {@Poller(fixedRate = "1000", maxMessagesPerPoll = "1000")})
	public MessageSource<InputStream> ftpMessageSource(ConcurrentMetadataStore metadataStore) {
		SftpStreamingMessageSource messageSource = new SftpStreamingMessageSource(sftpRemoteFileTemplate);
		messageSource.setRemoteDirectory("/stage/ftp/sftp/fda/first");
		ChainFileListFilter<ChannelSftp.LsEntry> chainFileListFilter = new ChainFileListFilter<>();
		
		SftpPersistentAcceptOnceFileListFilter sftpPersistentAcceptOnceFileListFilter = new SftpPersistentAcceptOnceFileListFilter(metadataStore, "file_");
		sftpPersistentAcceptOnceFileListFilter.setFlushOnUpdate(true);
		
		chainFileListFilter.addFilter(new SftpSimplePatternFileListFilter("*.txt"));
		chainFileListFilter.addFilter(sftpPersistentAcceptOnceFileListFilter);
		messageSource.setFilter(chainFileListFilter);
		messageSource.setFileInfoJson(false);
		messageSource.setMaxFetchSize(1000);
		return messageSource; 
	}

	@Bean
	public MessageChannel stream() {
		DirectChannel channel = new DirectChannel();
		return channel;
	}

	@Transformer(inputChannel = "stream", outputChannel = "amqpOutboundChannel")
	public String transform(Message<?> aFile) throws Exception {
		MessageHeaders headers = aFile.getHeaders();
		String remoteDirectory = headers.getOrDefault(FileHeaders.REMOTE_DIRECTORY, "/").toString();
		String fileName = headers.getOrDefault(FileHeaders.REMOTE_FILE, "").toString();
		return remoteDirectory + "/" + fileName;
	}

}
