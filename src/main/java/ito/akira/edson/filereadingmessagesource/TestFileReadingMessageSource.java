package ito.akira.edson.filereadingmessagesource;

import java.io.File;

import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

//@Configuration
//@EnableIntegration
public class TestFileReadingMessageSource {
	public String INPUT_DIR = "/home/administrator/Imagens/test/a";
	public String OUTPUT_DIR = "/home/administrator/Imagens/test/b";
	public String FILE_PATTERN = "*.*";

	@Bean
	public MessageChannel fileChannel() {
		return new DirectChannel();
	}
	
	@Bean
    public MessageChannel fileToJobProcessor() {
        return new DirectChannel();
    }
 
	@Bean
	@InboundChannelAdapter(value = "fileChannel", poller = @Poller(fixedDelay = "1000"))
	public MessageSource<File> fileReadingMessageSource() {
		FileReadingMessageSource sourceReader = new FileReadingMessageSource();
		sourceReader.setDirectory(new File(INPUT_DIR));
		sourceReader.setFilter(new SimplePatternFileListFilter(FILE_PATTERN));
		sourceReader.setScanEachPoll(true);
//        sourceReader.setUseWatchService(true);
		sourceReader.setAutoCreateDirectory(true);
		return sourceReader;
	}
	
	@Transformer(inputChannel = "fileChannel", outputChannel = "fileToJobProcessor")
	public File transform(File aFile) throws Exception {
		System.out.println(aFile);

		// Move old file to archive directory.
		if (aFile.renameTo(new File("/temp/" + aFile.getName()))) {
			System.out.println(aFile.getName() +" "+  aFile.getAbsolutePath());
		}
		return aFile;
	}
	
	@Transformer(inputChannel = "fileToJobProcessor", outputChannel = "fileChannel")
	public String transformJob(File aFile) {
		String fileName = aFile.getAbsolutePath();
//		JobParameters jobParameters = new JobParametersBuilder().addString("fileName", fileName)
//				.addDate("dateTime", new Date()).toJobParameters();
//
//		JobLaunchRequest request = new JobLaunchRequest(job, jobParameters);
		return fileName;
	}

	@Bean
	@ServiceActivator(inputChannel = "fileChannel")
	public MessageHandler fileWritingMessageHandler() {
		FileWritingMessageHandler handler = new FileWritingMessageHandler(new File(OUTPUT_DIR));
		handler.setFileExistsMode(FileExistsMode.REPLACE);
		handler.setExpectReply(false);
		return handler;
	}

}