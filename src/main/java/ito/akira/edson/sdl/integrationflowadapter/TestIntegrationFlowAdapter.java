package ito.akira.edson.sdl.integrationflowadapter;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.springframework.integration.annotation.Aggregator;
import org.springframework.integration.annotation.Filter;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Splitter;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.dsl.IntegrationFlowAdapter;
import org.springframework.integration.dsl.IntegrationFlowDefinition;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.TriggerContext;
import org.springframework.util.StringUtils;

public class TestIntegrationFlowAdapter extends IntegrationFlowAdapter {
	
	public TestIntegrationFlowAdapter(int i) {
	}
	
	private final AtomicBoolean invoked = new AtomicBoolean();

    public Date nextExecutionTime(TriggerContext triggerContext) {
          return this.invoked.getAndSet(true) ? null : new Date();
    }

    @Override
    protected IntegrationFlowDefinition<?> buildFlow() {
        return from(this, "messageSource",
                      e -> e.poller(p -> p.trigger(this::nextExecutionTime)))
                 .split(this)
                 .transform(this)
                 .aggregate(a -> a.processor(this, null))
                 .enrichHeaders(Collections.singletonMap("thing1", "THING1"))
                 .channel("TesteSS")
                 .filter(this)
                 .channel("Kkkk")
                 .handle(this)
                 .channel(c -> c.queue("myFlowAdapterOutput"));
    }

    public String messageSource() {
         return "T,H,I,N,G,2";
    }

    @Splitter
    public String[] split(String payload) {
         return StringUtils.commaDelimitedListToStringArray(payload);
    }

    @Transformer
    public String transform(String payload) {
         return payload.toLowerCase();
    }

    @Aggregator
    public String aggregate(List<String> payloads) {
           return payloads.stream().collect(Collectors.joining());
    }

    @Filter
    public boolean filter(@Header Optional<String> thing1) {
            return thing1.isPresent();
    }

    @ServiceActivator()
    public String handle(String payload, @Header String thing1) {
           return payload + ":" + thing1;
    }

}
