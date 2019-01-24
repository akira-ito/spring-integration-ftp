package ito.akira.edson.config;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.integration.metadata.ConcurrentMetadataStore;
import org.springframework.integration.redis.metadata.RedisMetadataStore;

@Configuration
public class RedisConfig {

	@Bean
	RedisConnectionFactory redisConnectionFactory () {
		RedisStandaloneConfiguration standaloneConfiguration = new RedisStandaloneConfiguration();
		standaloneConfiguration.setHostName("localhost");
		standaloneConfiguration.setPort(6379);
		standaloneConfiguration.setDatabase(1);
		standaloneConfiguration.setPassword(RedisPassword.none());
		
		RedisClusterConfiguration a = new RedisClusterConfiguration();
		RedisSentinelConfiguration b = new RedisSentinelConfiguration();

		LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(standaloneConfiguration);
		return lettuceConnectionFactory;
	}
	
	@Bean
	ConcurrentMetadataStore metadataStore () {
		return new RedisMetadataStore(redisConnectionFactory(), "invoices");
	}
}
