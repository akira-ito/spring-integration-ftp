package ito.akira.edson.sdl.integrationflowadapter;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec;
import org.springframework.integration.dsl.StandardIntegrationFlow;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.filters.ChainFileListFilter;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.metadata.ConcurrentMetadataStore;
import org.springframework.integration.sftp.dsl.Sftp;
import org.springframework.integration.sftp.dsl.SftpStreamingInboundChannelAdapterSpec;
import org.springframework.integration.sftp.filters.SftpPersistentAcceptOnceFileListFilter;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;

import ito.akira.edson.sdl.integrationflowadapter.properties.FirstProperties.ProviderProperties;
import ito.akira.edson.sdl.integrationflowadapter.properties.FirstProperties.SftpProperties;
import ito.akira.edson.sdl.integrationflowadapter.properties.TypeProvider;

@Component
public class IntegrationFlowWorker {

	@Autowired
	private ConcurrentMetadataStore metadataStore;

	@Autowired
	AmqpTemplate amqpTemplate;

	@Bean
	public QueueChannel holdingTank() {
		return MessageChannels.queue().get();
	}

	public StandardIntegrationFlow create(TypeProvider provider, ProviderProperties providerProperties) {
		ChainFileListFilter<LsEntry> chainFileListFilter = createFilters(providerProperties.getPatternFileFilter(),
				providerProperties.getMetadataStorePrefix());

		int maxFetchSize = providerProperties.getMaxFetchSize();
		List<String> pollers = providerProperties.getPoller();
		SftpProperties sftp = providerProperties.getSftp();

		return IntegrationFlows.from(createInbound(chainFileListFilter, maxFetchSize, sftp), createPollers(pollers))
				.channel("holdingTank")
//				.route(r -> "subscribableChannel")
				.get();

	}

	private SftpStreamingInboundChannelAdapterSpec createInbound(ChainFileListFilter<LsEntry> chainFileListFilter,
			int maxFetchSize, SftpProperties sftp) {
		return Sftp.inboundStreamingAdapter(createSftp(sftp)).remoteDirectory(sftp.getRemoteDirectory())
				.filter(chainFileListFilter).maxFetchSize(maxFetchSize);
	}

	private SftpRemoteFileTemplate createSftp(SftpProperties sftp) {
		DefaultSftpSessionFactory sf = new DefaultSftpSessionFactory();
		sf.setHost(sftp.getHost());
		sf.setPort(sftp.getPort());
		sf.setUser(sftp.getUsername());
		sf.setPassword(sftp.getPassword());
		sf.setAllowUnknownKeys(true);

		CachingSessionFactory<LsEntry> cachingSessionFactory = new CachingSessionFactory<>(sf);
		cachingSessionFactory.setPoolSize(sftp.getPoolSize());
		cachingSessionFactory.setSessionWaitTimeout(sftp.getSessionWaitTimeout());

		SftpRemoteFileTemplate sftpRemoteFileTemplate = new SftpRemoteFileTemplate(cachingSessionFactory);
		return sftpRemoteFileTemplate;
	}

	private ChainFileListFilter<LsEntry> createFilters(String patternFileFilter, String metadataStorePrefix) {
		ChainFileListFilter<ChannelSftp.LsEntry> chain = new ChainFileListFilter<>();
		SftpPersistentAcceptOnceFileListFilter sftpPersistentAcceptOnce = new SftpPersistentAcceptOnceFileListFilter(
				metadataStore, metadataStorePrefix);
		sftpPersistentAcceptOnce.setFlushOnUpdate(true);

		chain.addFilter(sftpPersistentAcceptOnce);
		chain.addFilter(new SftpSimplePatternFileListFilter(patternFileFilter));
		return chain;
	}

	private Consumer<SourcePollingChannelAdapterSpec> createPollers(List<String> pollers) {
		return e -> {
			e = e.autoStartup(false);
			for (String poller : pollers) {
				e = e.poller(p -> p.cron(poller));
			}
		};
	}

	@Bean
	public IntegrationFlow dddddd() {
		return IntegrationFlows.from("holdingTank").bridge(e -> e.poller(Pollers.fixedRate(2, TimeUnit.SECONDS, 1)))
				.transform(Message.class, aFile -> {
					MessageHeaders headers = aFile.getHeaders();
					String remoteDirectory = headers.getOrDefault(FileHeaders.REMOTE_DIRECTORY, "/").toString();
					String fileName = headers.getOrDefault(FileHeaders.REMOTE_FILE, "").toString();
					return MessageBuilder.withPayload(remoteDirectory + "/" + fileName).copyHeaders(aFile.getHeaders())
							.build();
				}).handle(Amqp.outboundAdapter(amqpTemplate).routingKey("orders-queue").exchangeName("orders-exchange"))
				.get();
	}

//	@Bean
//	public IntegrationFlow sss() {
//		return IntegrationFlows.from("subscribableChannel")
//				.handle(Amqp.outboundAdapter(amqpTemplate)
//						.routingKey("orders-queue")
//						.exchangeName("orders-exchange"))
//				.get();
//	}

}
