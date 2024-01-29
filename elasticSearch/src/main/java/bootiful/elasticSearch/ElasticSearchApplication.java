package bootiful.elasticSearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ElasticSearchApplication {

	public static void main(String[] args) {
//		ElasticApmAttacher.attach(); // Attach the Elastic Apm [application performance monitoring] agent to the current JVM.
		SpringApplication.run(ElasticSearchApplication.class, args);
	}

}
