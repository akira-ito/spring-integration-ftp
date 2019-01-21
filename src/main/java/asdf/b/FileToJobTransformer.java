package asdf.b;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.batch.BatchProperties.Job;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.integration.annotation.Transformer;
import org.springframework.stereotype.Component;

@Component
public class FileToJobTransformer implements ApplicationContextAware {

	private static final Logger log = LoggerFactory.getLogger(FileToJobTransformer.class);

	private ApplicationContext context;

	@Transformer(inputChannel = "fileToJobProcessor", outputChannel = "jobChannel")
	public String transform(File aFile) {

		String fileName = aFile.getAbsolutePath();

//		JobParameters jobParameters = new JobParametersBuilder().addString("fileName", fileName)
//				.addDate("dateTime", new Date()).toJobParameters();
//
//		JobLaunchRequest request = new JobLaunchRequest(job, jobParameters);

		return fileName;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}
}