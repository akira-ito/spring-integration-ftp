package asdf;

import java.io.File;
import java.util.List;

import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import asdf.a.BasicIntegrationConfig.Gateway;

@SpringBootApplication
@EnableIntegration
@EnableScheduling
public class FtpApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(FtpApplication.class, args);
	}

//	@Autowired
//	private FtpRemoteFileTemplate template;

//	@Autowired
//	private Gateway gateway;

	@Override
	public void run(String... args) throws Exception {
//		List<File> files = gateway.fetchFiles(".");
//		System.out.println(files);
//		Thread.sleep(2000);
		
//		FTPFile[] list = template.list(".");
//		System.out.println(list.length);
//		template.get("/", inputStream -> {
//			System.out.println("kdfkdsakfkdsfkdsfkdsf");
//			FileCopyUtils.copy(inputStream,
//					new FileOutputStream(new File("/home/administrator/Imagens/test/a/bar.txt")));
//		});
	}

//	@Bean
//	public ApplicationRunner runner(Gate gate) {
//		return args -> {
//			List list = gate.list(".");
//			System.out.println("Result:" + list);
//		};
//	}
}
