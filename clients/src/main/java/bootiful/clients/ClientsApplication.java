package bootiful.clients;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.tcp.SslProvider;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

@SpringBootApplication
public class ClientsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientsApplication.class, args);
	}

	@Bean
	RestClient restClient (RestClient.Builder builder) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		final TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
		final SSLContext sslContext = SSLContexts.custom()
				.loadTrustMaterial(null, acceptingTrustStrategy)
				.build();
		final SSLConnectionSocketFactory sslCsf =
				new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
		final Registry<ConnectionSocketFactory> socketFactoryRegistry =
				RegistryBuilder.<ConnectionSocketFactory> create()
						.register("https", sslCsf)
						.register("http", new PlainConnectionSocketFactory())
						.build();

		final PoolingHttpClientConnectionManager connectionManager =
				new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		HttpClient httpClient = HttpClients.custom()
				.setConnectionManager(connectionManager)
				.build();

		final HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		return builder
				.requestFactory(requestFactory)
				.build();
	}
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplateBuilder().build();
	}
	@Bean
	public WebClient webClient() throws SSLException {
		final TrustManagerFactory trustManagerFactory = InsecureTrustManagerFactory.INSTANCE;
		final SslContext sslContext = SslContextBuilder.forClient()
				.trustManager(trustManagerFactory)
				.build();
		final reactor.netty.http.client.HttpClient httpClient = reactor.netty.http.client.HttpClient.create()
				.secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
		return WebClient.builder()
				.clientConnector(new ReactorClientHttpConnector(httpClient))
				.build();
	}

}
// https://catfact.ninja/fact
@Component @RequiredArgsConstructor
class CatFactRestClient {
	private final RestClient restClient;
	private final RestTemplate restTemplate;
	private final WebClient webClient;
	private final String url = "https://catfact.ninja/fact";
	CatFact oneCatFact() {
		return this.restClient
				.get()
				.uri(url)
				.retrieve()
				.body(CatFact.class);
	}

	CatFact oneCatFactWithRT() {
		return this.restTemplate.getForObject(url, CatFact.class);
	}
	CatFact oneCatFactWithWC() {
		return this.webClient.get().uri(url).retrieve().bodyToMono(CatFact.class).block();
	}

}

record CatFact (String fact, long length) {}

@Repository
@RequiredArgsConstructor
class CustomerRepository {
	private final JdbcClient jdbcClient;
	private final RowMapper<Customer> customerRowMapper = (rs, rowNum) -> new Customer(rs.getInt("id"), rs.getString("name"));
	Collection<Customer> findAll() {
		return this.jdbcClient
				.sql("select * from customer")
				.query(customerRowMapper)
				.list();
	}
}

@RestController
@RequiredArgsConstructor
class CatFactsController {
	private final CatFactRestClient catFactRestClient;
	@GetMapping("/cat-fact")
	CatFact facts() {
		return this.catFactRestClient.oneCatFactWithWC();
	}
}

@RestController
@RequiredArgsConstructor
class CustomerController {
	private final CustomerRepository customerRepository;

	@GetMapping("/customers") Collection<Customer> customers(){
		return this.customerRepository.findAll();
	}
}

record Customer (Integer id, String name) {}
