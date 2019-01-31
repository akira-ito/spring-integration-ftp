package ito.akira.edson.sdl.integrationflowadapter.properties;

import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.remote.AbstractFileInfo;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.sftp.inbound.SftpStreamingMessageSource;

import com.jcraft.jsch.ChannelSftp.LsEntry;

public class SftpStreamingMessageSourceCustom extends SftpStreamingMessageSource {

	public SftpStreamingMessageSourceCustom(RemoteFileTemplate<LsEntry> template) {
		super(template);
	}

	@Override
	protected Object doReceive() {
		AbstractFileInfo<LsEntry> file = poll();
		if (file != null) {
			return getMessageBuilderFactory().withPayload(remotePath(file))
					.setHeader(FileHeaders.REMOTE_DIRECTORY, file.getRemoteDirectory())
					.setHeader(FileHeaders.REMOTE_FILE, file.getFilename())
					.setHeader(FileHeaders.REMOTE_FILE_INFO, file.toJson());
		}
		return null;
	}

}
