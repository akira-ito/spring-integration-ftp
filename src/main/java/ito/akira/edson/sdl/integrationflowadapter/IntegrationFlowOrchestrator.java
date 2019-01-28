package ito.akira.edson.sdl.integrationflowadapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.dsl.context.IntegrationFlowRegistration;

import ito.akira.edson.sdl.integrationflowadapter.properties.FirstProperties;

@Configuration
public class IntegrationFlowOrchestrator implements CommandLineRunner {

	@Autowired
	private IntegrationFlowContext flowContext;
	
	@Autowired
	private IntegrationFlowWorker adsfsdaf;
	
	@Autowired
	private FirstProperties properties;

	@Override
	public void run(String... args) throws Exception {
//		IntegrationFlowRegistration flow1 = this.flowContext.registration(new TestSFTPIntegrationFlowAdapter(1234))
//				.id("tcp1").register();

		IntegrationFlowRegistration flow1 = this.flowContext.registration(adsfsdaf.create()).register();
		flow1.getId();
//		IntegrationFlowRegistration flow1ss = this.flowContext.registration(adsfsdaf.asdfkdskfkd())
//				.id("tcp1xxx").register();
		
	}

}
