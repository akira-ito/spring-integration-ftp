package ito.akira.edson.sdl.integrationflowadapter.properties;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("first")
public class FirstProperties {

	private Map<TypeProvider, ProviderProperties> provider;

	public Map<TypeProvider, ProviderProperties> getProvider() {
		return provider;
	}

	public void setProvider(Map<TypeProvider, ProviderProperties> provider) {
		this.provider = provider;
	}

	public static class ProviderProperties {

		private int maxFetchSize;
		private String patternFileFilter;
		private String metadataStorePrefix;
		private PollerProperties poller;
		private RedisProperties redis;
		private SftpProperties sftp;
		private AmqpProperties amqp;

		public int getMaxFetchSize() {
			return maxFetchSize;
		}

		public void setMaxFetchSize(int maxFetchSize) {
			this.maxFetchSize = maxFetchSize;
		}

		public String getPatternFileFilter() {
			return patternFileFilter;
		}

		public void setPatternFileFilter(String patternFileFilter) {
			this.patternFileFilter = patternFileFilter;
		}

		public String getMetadataStorePrefix() {
			return metadataStorePrefix;
		}

		public void setMetadataStorePrefix(String metadataStorePrefix) {
			this.metadataStorePrefix = metadataStorePrefix;
		}

		public PollerProperties getPoller() {
			return poller;
		}

		public void setPoller(PollerProperties poller) {
			this.poller = poller;
		}

		public RedisProperties getRedis() {
			return redis;
		}

		public void setRedis(RedisProperties redis) {
			this.redis = redis;
		}

		public SftpProperties getSftp() {
			return sftp;
		}

		public void setSftp(SftpProperties sftp) {
			this.sftp = sftp;
		}

		public AmqpProperties getAmqp() {
			return amqp;
		}

		public void setAmqp(AmqpProperties amqp) {
			this.amqp = amqp;
		}

	}

	public static class PollerProperties {
		private String cron;
		private Long maxMessagesPerPoll;

		public String getCron() {
			return cron;
		}

		public void setCron(String cron) {
			this.cron = cron;
		}

		public Long getMaxMessagesPerPoll() {
			return maxMessagesPerPoll;
		}

		public void setMaxMessagesPerPoll(Long maxMessagesPerPoll) {
			this.maxMessagesPerPoll = maxMessagesPerPoll;
		}

	}

	public static class SftpProperties {
		private String host;
		private Integer port;
		private String username;
		private String password;
		private String remoteDirectory;
		private Integer poolSize;
		private Long sessionWaitTimeout;

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public Integer getPort() {
			return port;
		}

		public void setPort(Integer port) {
			this.port = port;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public Long getSessionWaitTimeout() {
			return sessionWaitTimeout;
		}

		public void setSessionWaitTimeout(Long sessionWaitTimeout) {
			this.sessionWaitTimeout = sessionWaitTimeout;
		}

		public Integer getPoolSize() {
			return poolSize;
		}

		public void setPoolSize(Integer poolSize) {
			this.poolSize = poolSize;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getRemoteDirectory() {
			return remoteDirectory;
		}

		public void setRemoteDirectory(String remoteDirectory) {
			this.remoteDirectory = remoteDirectory;
		}

	}

	public static class RedisProperties {
		private String nameMetadataStore;

		public void setNameMetadataStore(String nameMetadataStore) {
			this.nameMetadataStore = nameMetadataStore;
		}

		public String getNameMetadataStore() {
			return nameMetadataStore;
		}

	}

	public static class AmqpProperties {
		private String routeKey;
		private String exchangeName;
		private String queueName;
		private String deadLetterRouteKey;

		public String getRouteKey() {
			return routeKey;
		}

		public void setRouteKey(String routeKey) {
			this.routeKey = routeKey;
		}

		public String getExchangeName() {
			return exchangeName;
		}

		public void setExchangeName(String exchangeName) {
			this.exchangeName = exchangeName;
		}

		public String getQueueName() {
			return queueName;
		}

		public void setQueueName(String queueName) {
			this.queueName = queueName;
		}

		public String getDeadLetterRouteKey() {
			return deadLetterRouteKey;
		}

		public void setDeadLetterRouteKey(String deadLetterRouteKey) {
			this.deadLetterRouteKey = deadLetterRouteKey;
		}

	}

}
