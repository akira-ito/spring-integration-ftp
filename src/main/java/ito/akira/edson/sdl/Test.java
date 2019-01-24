package ito.akira.edson.sdl;

import java.io.File;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.filters.ChainFileListFilter;
import org.springframework.integration.file.filters.FileSystemPersistentAcceptOnceFileListFilter;
import org.springframework.integration.file.filters.IgnoreHiddenFileListFilter;
import org.springframework.integration.file.filters.LastModifiedFileListFilter;
import org.springframework.integration.metadata.ConcurrentMetadataStore;
import org.springframework.integration.redis.metadata.RedisMetadataStore;

public class Test {
	
	@Bean
	public ConcurrentMetadataStore metadataStore() {
		return new RedisMetadataStore(new LettuceConnectionFactory());
	}

	@Bean
	public FileSystemPersistentAcceptOnceFileListFilter persistentAcceptOnceFileFilter() {
		return new FileSystemPersistentAcceptOnceFileListFilter(metadataStore(), "edi-file-locks");
	}

	@Bean
	public IntegrationFlow flowInboundNetTransferFile(
			@Value("${edi.incoming.directory.netTransfers}") String inboundDirectory,
			@Value("${edi.incoming.age-before-ready-seconds:30}") int ageBeforeReadySeconds,
			@Value("${taskExecutor.inboundFile.corePoolSize:4}") int corePoolSize,
			@Qualifier("taskExecutorInboundFile") TaskExecutor taskExecutor) {

		// Setup a filter to only pick up a files older than a certain age, relative to
		// the current time. This prevents cases
		// where something is writing to the file as the EDI processor is moving that
		// file.
		LastModifiedFileListFilter lastModifiedFilter = new LastModifiedFileListFilter();
		lastModifiedFilter.setAge(ageBeforeReadySeconds);
		return IntegrationFlows.from(Files.inboundAdapter(new File(inboundDirectory))
//	                    .locker(ediDocumentLocker())
				.filter(new ChainFileListFilter<File>()).filter(new IgnoreHiddenFileListFilter())
				.filter(lastModifiedFilter).filter(persistentAcceptOnceFileFilter()),
				e -> e.poller(Pollers.fixedDelay(20000).maxMessagesPerPoll(corePoolSize).taskExecutor(taskExecutor)))
//	            .channel(channelInboundFile())
				.get();
	}
}
