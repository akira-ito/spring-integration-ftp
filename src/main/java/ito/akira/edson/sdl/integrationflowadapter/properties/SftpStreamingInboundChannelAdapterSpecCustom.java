package ito.akira.edson.sdl.integrationflowadapter.properties;

import org.springframework.integration.file.dsl.RemoteFileStreamingInboundChannelAdapterSpec;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.sftp.filters.SftpRegexPatternFileListFilter;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.sftp.inbound.SftpStreamingMessageSource;

import com.jcraft.jsch.ChannelSftp.LsEntry;

public class SftpStreamingInboundChannelAdapterSpecCustom extends
		RemoteFileStreamingInboundChannelAdapterSpec<LsEntry, SftpStreamingInboundChannelAdapterSpecCustom, SftpStreamingMessageSource> {

	public SftpStreamingInboundChannelAdapterSpecCustom(RemoteFileTemplate<LsEntry> remoteFileTemplate) {
		this.target = new SftpStreamingMessageSourceCustom(remoteFileTemplate);
	}

	@Override
	public SftpStreamingInboundChannelAdapterSpecCustom patternFilter(String pattern) {
		return filter(new SftpSimplePatternFileListFilter(pattern));
	}

	@Override
	public SftpStreamingInboundChannelAdapterSpecCustom regexFilter(String regex) {
		return filter(new SftpRegexPatternFileListFilter(regex));
	}

}
