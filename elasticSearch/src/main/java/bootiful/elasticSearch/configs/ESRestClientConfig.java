package bootiful.elasticSearch.configs;

import bootiful.elasticSearch.base.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

import java.net.URI;

/**
 * @author pari on 19/01/24
 */
@Configuration
@Slf4j
class ESRestClientConfig extends ElasticsearchConfiguration {
    @Value("${app.elastic_search_uri}")
    private String ELASTICSEARCH_URL;
    /**
      The ElasticsearchConfiguration class allows further configuration by overriding for example the jsonpMapper() or transportOptions() methods.
      The following beans can then be injected in other Spring components:
     1. ElasticsearchOperations operations;
     2. ElasticsearchClient elasticsearchClient;
     3. RestClient restClient;
     4. JsonpMapper jsonpMapper;
     - Basically one should just use the ElasticsearchOperations to interact with the Elasticsearch cluster.
        When using repositories, this instance is used under the hood as well.
    */
    @Autowired
    private SslBundles sslBundles;
    @Override
    public ClientConfiguration clientConfiguration() {
        final String stringUrl = ELASTICSEARCH_URL;
        final URI uri = URI.create(stringUrl);

        String host = uri.getHost();
        int port = uri.getPort() == -1 ? 9200 : uri.getPort();
        ClientConfiguration.MaybeSecureClientConfigurationBuilder builder =
                ClientConfiguration.builder()
                        .connectedTo(host + ":" + port);
        final boolean isSsl = "https".equals(uri.getScheme());
        if (isSsl) {
            final SslBundle clientBundle = sslBundles.getBundle("client");
            builder.usingSsl(clientBundle.createSslContext());
        }
        // enable basic auth if specified
        final String userInfo = uri.getUserInfo();
        if (userInfo!=null) {
            String[] userPass = userInfo.split(":", 2);
            builder.withBasicAuth(userPass[0], userPass[1]);
        }
        log.info("Elasticsearch server [{}:{}] ssl[{}] auth[{}]", host, port, isSsl, userInfo != null);

        return builder.build();
    }

    @Bean
    public AdminService adminService() {
        return new AdminService();
    }
}
