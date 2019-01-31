package ito.akira.edson.sdl.integrationflowadapter.properties;

public class FtpFilePayload {
	private String file;
	private TypeProvider provider;

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public TypeProvider getProvider() {
		return provider;
	}

	public void setProvider(TypeProvider provider) {
		this.provider = provider;
	}

}
