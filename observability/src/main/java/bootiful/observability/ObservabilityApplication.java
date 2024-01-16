package bootiful.observability;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.Map;

@SpringBootApplication
public class ObservabilityApplication {

	public static void main(String[] args) {
		SpringApplication.run(ObservabilityApplication.class, args);
	}

	@Bean
	RestClient restClient(RestClient.Builder builder) {
		return builder.requestFactory(new JdkClientHttpRequestFactory())
				.build();
	}
	@Bean
	CatClient catClient(RestClient restClient) {
		return HttpServiceProxyFactory
				.builderFor(RestClientAdapter.create(restClient))
				.build()
				.createClient(CatClient.class);
	}
}

@RestController
@RequiredArgsConstructor
@Slf4j
class CatsController {
	private final CatClient catClient;
	private final ObservationRegistry observationRegistry;
	@GetMapping("/fact")
	Map<String, Object> fact() {
		final CatClient.Fact aCatFact = this.catClient.getOneFact();
		// can use the following fine-grained observation instead of using @Observed annotations.
//		var factObserved = Observation.createNotStarted("cats", this.observationRegistry)
//						.observe(()-> {
//							var f = this.catClient.getOneFact();
//							log.info(f.toString());
//							return f;
//						});
//		return Map.of("fact", factObserved);
		log.info(aCatFact.toString());
		return Map.of("fact", aCatFact);
	}
}

// declarative client
interface CatClient {
	@Observed(name="cat-fact") // enable instrumentation for this method call
	@GetExchange("https://catfact.ninja/fact")
	Fact getOneFact();
	record Fact(String fact) {}
}