package asdf.a;
import java.util.List;

import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway(defaultRequestChannel = "ftpLS", defaultReplyChannel = "results")
public interface Gate {

	List list(String directory);

}