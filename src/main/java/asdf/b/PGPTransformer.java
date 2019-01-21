package asdf.b;

import java.io.File;

import org.springframework.integration.annotation.Transformer;
import org.springframework.stereotype.Component;

@Component
public class PGPTransformer {

	private String archiveDir = "/home/administrator/Downloads/test";

	@Transformer(inputChannel = "fileInputChannel", outputChannel = "fileToJobProcessor")
	public File transform(File aFile) throws Exception {
		
		System.out.println(aFile);

		// Move old file to archive directory.
		if (aFile.renameTo(new File(archiveDir + "/" + aFile.getName()))) {
			System.out.println(aFile.getName() +" "+  aFile.getAbsolutePath());
		}
		return aFile;
	}
}