package ito.akira.edson.sdl.integrationflowadapter;

import java.util.function.Consumer;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.amqp.dsl.AmqpOutboundEndpointSpec;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageHandlerSpec;
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec;
import org.springframework.integration.dsl.StandardIntegrationFlow;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.filters.ChainFileListFilter;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.handler.advice.ErrorMessageSendingRecoverer;
import org.springframework.integration.handler.advice.IdempotentReceiverInterceptor;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.integration.handler.advice.RetryStateGenerator;
import org.springframework.integration.handler.advice.SpelExpressionRetryStateGenerator;
import org.springframework.integration.redis.metadata.RedisMetadataStore;
import org.springframework.integration.selector.MetadataStoreSelector;
import org.springframework.integration.sftp.filters.SftpPersistentAcceptOnceFileListFilter;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;

import ito.akira.edson.sdl.integrationflowadapter.properties.FirstProperties.AmqpProperties;
import ito.akira.edson.sdl.integrationflowadapter.properties.FirstProperties.PollerProperties;
import ito.akira.edson.sdl.integrationflowadapter.properties.FirstProperties.ProviderProperties;
import ito.akira.edson.sdl.integrationflowadapter.properties.FirstProperties.RedisProperties;
import ito.akira.edson.sdl.integrationflowadapter.properties.FirstProperties.SftpProperties;
import ito.akira.edson.sdl.integrationflowadapter.properties.FtpFilePayload;
import ito.akira.edson.sdl.integrationflowadapter.properties.SftpStreamingInboundChannelAdapterSpecCustom;
import ito.akira.edson.sdl.integrationflowadapter.properties.TypeProvider;

@Component
public class IntegrationFlowWorker {

	@Autowired
	private RabbitTemplate amqpTemplate;

	@Autowired
	private RedisConnectionFactory redisConnectionFactory;

	private IdempotentReceiverInterceptor idempotentReceiverInterceptor;

	public StandardIntegrationFlow create(TypeProvider provider, ProviderProperties providerProperties) {
		int maxFetchSize = providerProperties.getMaxFetchSize();
		PollerProperties poller = providerProperties.getPoller();
		SftpProperties sftp = providerProperties.getSftp();
		AmqpProperties amqpProperties = providerProperties.getAmqp();
		RedisProperties redisProperties = providerProperties.getRedis();

		ChainFileListFilter<LsEntry> chainFileListFilter = createFilters(providerProperties.getPatternFileFilter(),
				providerProperties.getMetadataStorePrefix(), redisProperties);

		return IntegrationFlows.from(createInbound(chainFileListFilter, maxFetchSize, sftp), createPollers(poller))
				.transform(Message.class, createTransformer(provider))
				.handle((MessageHandlerSpec) createOutbound(amqpProperties), c -> c.advice(createAdvisorAMQP())).get();

	}

	private RequestHandlerRetryAdvice createAdvisorAMQP() {
		RequestHandlerRetryAdvice requestHandlerRetryAdvice = new RequestHandlerRetryAdvice();
		DirectChannel channel = new DirectChannel();
		channel.subscribe(m -> {
			m.getPayload();
			System.out.println(m);
		});
//		channel.setBeanName("errour");
		ErrorMessageSendingRecoverer errorMessageSendingRecoverer = new ErrorMessageSendingRecoverer(channel);
		requestHandlerRetryAdvice.setRecoveryCallback(errorMessageSendingRecoverer);

		RetryTemplate retryTemplate = new RetryTemplate();
		retryTemplate.setRetryPolicy(new SimpleRetryPolicy(3));
		retryTemplate.registerListener(requestHandlerRetryAdvice);

		ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
		backOffPolicy.setInitialInterval(1000);
		backOffPolicy.setMultiplier(1.1);
		backOffPolicy.setMaxInterval(30000);
		retryTemplate.setBackOffPolicy(backOffPolicy);
		
		

		requestHandlerRetryAdvice.setRetryTemplate(retryTemplate);
		
		RetryStateGenerator retryStateGenerator =  new SpelExpressionRetryStateGenerator("payload");
//		requestHandlerRetryAdvice.setRetryStateGenerator(retryStateGenerator);
		return requestHandlerRetryAdvice;
	}

	private GenericTransformer<Message, Message<FtpFilePayload>> createTransformer(TypeProvider provider) {
		GenericTransformer<Message, Message<FtpFilePayload>> a = aFile -> {
			MessageHeaders headers = aFile.getHeaders();
			String remoteDirectory = headers.getOrDefault(FileHeaders.REMOTE_DIRECTORY, "/").toString();
			String fileName = headers.getOrDefault(FileHeaders.REMOTE_FILE, "").toString();

			FtpFilePayload ftpFilePayload = new FtpFilePayload();
			ftpFilePayload.setFile(remoteDirectory + "/" + fileName);
			ftpFilePayload.setProvider(provider);

			return MessageBuilder.withPayload(ftpFilePayload).copyHeaders(aFile.getHeaders()).build();
		};
		return a;
	}

	private AmqpOutboundEndpointSpec createOutbound(AmqpProperties amqpProperties) {
		Queue deadLetter = QueueBuilder.durable(amqpProperties.getDeadLetterRouteKey()).build();
		Queue queue = QueueBuilder.durable(amqpProperties.getQueueName()).withArgument("x-dead-letter-exchange", "")
				.withArgument("x-dead-letter-routing-key", amqpProperties.getDeadLetterRouteKey()).build();
		Exchange exchange = ExchangeBuilder.directExchange(amqpProperties.getExchangeName()).build();
		Binding binding = BindingBuilder.bind(queue).to(exchange).with(amqpProperties.getRouteKey()).noargs();

		RabbitAdmin rabbitAdmin = new RabbitAdmin(amqpTemplate);
		rabbitAdmin.declareQueue(queue);
		rabbitAdmin.declareQueue(deadLetter);
		rabbitAdmin.declareExchange(exchange);
		rabbitAdmin.declareBinding(binding);
		rabbitAdmin.setAutoStartup(true);

		return Amqp.outboundAdapter(amqpTemplate).routingKey(amqpProperties.getRouteKey())
				.exchangeName(amqpProperties.getExchangeName());
	}

	private SftpStreamingInboundChannelAdapterSpecCustom createInbound(ChainFileListFilter<LsEntry> chainFileListFilter,
			int maxFetchSize, SftpProperties sftp) {

		SftpRemoteFileTemplate remoteFileTemplate = createSftp(sftp);

		return new SftpStreamingInboundChannelAdapterSpecCustom(remoteFileTemplate)
				.remoteDirectory(sftp.getRemoteDirectory()).filter(chainFileListFilter).maxFetchSize(maxFetchSize);

//		return Sftp.inboundStreamingAdapter(remoteFileTemplate).remoteDirectory(sftp.getRemoteDirectory())
//				.filter(chainFileListFilter).maxFetchSize(maxFetchSize);
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

		SftpRemoteFileTemplate sftpRemoteFileTemplate = new SftpRemoteFileTemplate(sf);
		return sftpRemoteFileTemplate;
	}

	private ChainFileListFilter<LsEntry> createFilters(String patternFileFilter, String metadataStorePrefix,
			RedisProperties redisProperties) {
		ChainFileListFilter<ChannelSftp.LsEntry> chain = new ChainFileListFilter<>();
		RedisMetadataStore createMetadataStore = createMetadataStore(redisProperties);

		idempotentReceiverInterceptor = new IdempotentReceiverInterceptor(
				new MetadataStoreSelector(message -> message.getPayload().toString(),
						message -> message.getPayload().toString().toUpperCase(), createMetadataStore));

		SftpPersistentAcceptOnceFileListFilter sftpPersistentAcceptOnce = new SftpPersistentAcceptOnceFileListFilter(
				createMetadataStore, metadataStorePrefix);
		sftpPersistentAcceptOnce.setFlushOnUpdate(true);

		chain.addFilter(new SftpSimplePatternFileListFilter(patternFileFilter));
		chain.addFilter(sftpPersistentAcceptOnce);
		return chain;
	}

	private Consumer<SourcePollingChannelAdapterSpec> createPollers(PollerProperties poller) {
		return source -> source.autoStartup(false)
				.poller(p -> p.cron(poller.getCron()).maxMessagesPerPoll(poller.getMaxMessagesPerPoll()));
	}

	private RedisMetadataStore createMetadataStore(RedisProperties redisProperties) {
		return new RedisMetadataStore(redisConnectionFactory, redisProperties.getNameMetadataStore());
	}

//	@Bean
//	public RequestHandlerRetryAdvice retryAdvice() {
//		RequestHandlerRetryAdvice requestHandlerRetryAdvice = createAdvisorAMQP();
//		requestHandlerRetryAdvice.setRecoveryCallback(errorMessageSendingRecoverer());
//		return requestHandlerRetryAdvice;
//	}
//
//	@Bean
//	public ErrorMessageSendingRecoverer errorMessageSendingRecoverer() {
//		return new ErrorMessageSendingRecoverer(recoveryChannel());
//	}
//
//	@Bean
//	public MessageChannel recoveryChannel() {
//		return new DirectChannel();
//	}
//
//	@Bean
//	public IntegrationFlow handleRecovery() {
//		return IntegrationFlows.from("recoveryChannel").log(LoggingHandler.Level.ERROR, "error", m -> {
//			return m.getPayload();
//		}).get();
//	}

	@Bean
	public IntegrationFlow handleRecoveryC() {
		return IntegrationFlows.from("ERROUR").log(LoggingHandler.Level.ERROR, "error", m -> {
			return m.getPayload();
		}).get();
	}
}
