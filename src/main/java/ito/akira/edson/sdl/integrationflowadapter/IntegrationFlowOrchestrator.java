package ito.akira.edson.sdl.integrationflowadapter;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.StandardIntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;

import ito.akira.edson.sdl.integrationflowadapter.properties.FirstProperties;
import ito.akira.edson.sdl.integrationflowadapter.properties.FirstProperties.ProviderProperties;
import ito.akira.edson.sdl.integrationflowadapter.properties.TypeProvider;

@Configuration
public class IntegrationFlowOrchestrator implements CommandLineRunner {

	@Autowired
	private IntegrationFlowContext flowContext;

	@Autowired
	private IntegrationFlowWorker worker;

	@Autowired
	private FirstProperties properties;

	@Override
	public void run(String... args) throws Exception {
		Map<TypeProvider, ProviderProperties> providers = properties.getProvider();
		providers.forEach((type, properties) -> {
			StandardIntegrationFlow integrationFlow = worker.create(type, properties);
			this.flowContext.registration(integrationFlow).register();
		});

	}

}
