package ito.akira.edson.factories;

import java.io.File;

import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.integration.file.filters.ChainFileListFilter;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.FileListFilter;
import org.springframework.integration.file.filters.IgnoreHiddenFileListFilter;
import org.springframework.integration.file.filters.LastModifiedFileListFilter;
import org.springframework.integration.ftp.filters.FtpPersistentAcceptOnceFileListFilter;
import org.springframework.integration.ftp.filters.FtpRegexPatternFileListFilter;
import org.springframework.integration.ftp.filters.FtpSimplePatternFileListFilter;
import org.springframework.integration.metadata.ConcurrentMetadataStore;
import org.springframework.stereotype.Component;

@Component
public class FactoryFilter {
	
	public FileListFilter<FTPFile> createFileListFilter(ConcurrentMetadataStore metadataStore) {
		
//		new FtpPersistentAcceptOnceFileListFilter(new SimpleMetadataStore(), "rotate");
//		FtpRegexPatternFileListFilter sw = new FtpRegexPatternFileListFilter("");
		FtpSimplePatternFileListFilter sw = new FtpSimplePatternFileListFilter("*");
		FtpPersistentAcceptOnceFileListFilter ftpPersistentAcceptOnceFileListFilter = new FtpPersistentAcceptOnceFileListFilter(metadataStore, "file_");
		ftpPersistentAcceptOnceFileListFilter.setFlushOnUpdate(true);
		
		ChainFileListFilter<FTPFile> chain = new ChainFileListFilter<>();
		chain.addFilter(sw);
		chain.addFilter(ftpPersistentAcceptOnceFileListFilter);

//		Just file system
		LastModifiedFileListFilter lastModifiedFilter = new LastModifiedFileListFilter(60 * 3);
		IgnoreHiddenFileListFilter ignore = new IgnoreHiddenFileListFilter();
//		new AcceptAllFileListFilter<>();
//		new AcceptOnceFileListFilter<>();
		
		CompositeFileListFilter<File> compose = new CompositeFileListFilter<>();
		compose.addFilter(lastModifiedFilter);
		compose.addFilter(ignore);
      		
		return chain;
	}
}
