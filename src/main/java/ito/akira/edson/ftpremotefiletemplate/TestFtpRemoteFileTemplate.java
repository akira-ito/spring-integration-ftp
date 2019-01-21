package ito.akira.edson.ftpremotefiletemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;
import org.springframework.util.FileCopyUtils;

public class TestFtpRemoteFileTemplate implements CommandLineRunner {
	
	@Autowired
	private FtpRemoteFileTemplate template;

	
	@Override
	public void run(String... args) throws Exception {
		FTPFile[] list = template.list(".");
		System.out.println(list.length);
		template.get("/", inputStream -> {
			FileCopyUtils.copy(inputStream,
					new FileOutputStream(new File("/home/administrator/Imagens/test/a/bar.txt")));
		});
	}
	
	
	
	
}
