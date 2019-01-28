package ito.akira.edson.sdl.integrationflowadapter;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.amqp.dsl.AmqpOutboundEndpointSpec;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.dsl.IntegrationFlowAdapter;
import org.springframework.integration.dsl.IntegrationFlowDefinition;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.filters.ChainFileListFilter;
import org.springframework.integration.metadata.ConcurrentMetadataStore;
import org.springframework.integration.sftp.dsl.Sftp;
import org.springframework.integration.sftp.filters.SftpPersistentAcceptOnceFileListFilter;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import com.jcraft.jsch.ChannelSftp;

public class TestSFTPIntegrationFlowAdapter extends IntegrationFlowAdapter {

	public TestSFTPIntegrationFlowAdapter(int i) {
	}

	@Autowired
	private SftpRemoteFileTemplate sftpRemoteFileTemplate;
	
	@Autowired
	private ConcurrentMetadataStore metadataStore;
	
	@Autowired
	AmqpTemplate amqpTemplate;

	@Override
	protected IntegrationFlowDefinition<?> buildFlow() {
		ChainFileListFilter<ChannelSftp.LsEntry> chainFileListFilter = new ChainFileListFilter<>();
		
		SftpPersistentAcceptOnceFileListFilter sftpPersistentAcceptOnceFileListFilter = new SftpPersistentAcceptOnceFileListFilter(				metadataStore, "file_");
		sftpPersistentAcceptOnceFileListFilter.setFlushOnUpdate(true);
		chainFileListFilter.addFilter(new SftpSimplePatternFileListFilter("*.txt"));
		chainFileListFilter.addFilter(sftpPersistentAcceptOnceFileListFilter);
		
		return from(Sftp.inboundStreamingAdapter(sftpRemoteFileTemplate)
					.remoteDirectory("/stage/ftp/sftp/fda/first")
					.filter(chainFileListFilter)
					.maxFetchSize(100),
					e -> e.poller(p -> p.cron("* * * * * *")))
				.channel(c -> c.direct("stream"))
				.transform(this)
				.channel(c -> c.direct("teste"))
				.handle(this, "amqpOutbound");
	}

	public AmqpOutboundEndpointSpec amqpOutbound() {
		return Amqp.outboundAdapter(amqpTemplate)
			.routingKey("orders-queue")
			.exchangeName("orders-exchange");
    }

	@Transformer
	public Message<String> transform(Message<?> aFile) throws Exception {
		MessageHeaders headers = aFile.getHeaders();
		String remoteDirectory = headers.getOrDefault(FileHeaders.REMOTE_DIRECTORY, "/").toString();
		String fileName = headers.getOrDefault(FileHeaders.REMOTE_FILE, "").toString();
		return MessageBuilder.withPayload(remoteDirectory + "/" + fileName).copyHeaders(aFile.getHeaders()).build();
	}

}
