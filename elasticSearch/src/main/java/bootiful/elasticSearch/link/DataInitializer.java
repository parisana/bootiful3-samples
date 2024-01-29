package bootiful.elasticSearch.link;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

/**
 * @author pari on 19/01/24
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
    private final ElasticsearchOperations elasticsearchOperations;
    @Bean
    CommandLineRunner saveLinksToDb() {
        log.info("#saveLinksToDb");
        return (args) -> {
            if (elasticsearchOperations.indexOps(Link.class).exists()) {
//                this.elasticsearchOperations.indexOps(Link.class).delete();
            } else // create index if not exist.
                this.elasticsearchOperations.indexOps(Link.class).create();
            final long count = elasticsearchOperations.count(new CriteriaQuery(new Criteria()), Link.class);
            if (count > 0)
                log.info("Links exists in db");
            else {
                log.info("initializing links");
                elasticsearchOperations.save(links());
            }
        };
    }

    private static List<Link> links() {
        return List.of(
                Link.builder()
                        .title("Elasticsearch - Securing a search engine while maintaining usability")
                        .description("Security is often an afterthought when writing applications. Time pressure to finish features or developers not being aware of issues can be two out of many reasons. This talk will use the Elasticsearch codebase as an example of how to write a broadly used software, but keep security in mind. Not only pure Java features like the Java Security Manager will be covered or how to write a secure scripting engine, but also operating system features that can be leveraged. The goal of this talk is most importantly to make you think about your own codebase and where you can invest time to improve security of it - with maybe less efforts than you would think.")
                        .url("https://spinscale.de/posts/2020-04-07-elasticsearch-securing-a-search-engine-while-maintaining-usability.html")
                        .category("elasticsearch")
                        .createdAt(Date.from(LocalDate.of(2020, 4, 7).atStartOfDay().toInstant(ZoneOffset.UTC)))
                        .votes(12L)
                        .approved(true)
                        .submittedBy("user1")
                        .build(),
                Link.builder()
                        .title("Testing & releasing the Elastic stack")
                        .description("Elasticsearch is well known piece of software. This talk explains the different levels of testing along with packaging and releasing as part of the Elastic Stack. Testing a well known software like Elasticsearch is not too different to any other software. In this session we will peak into the different testing strategies for unit and integration tests including randomized testing, how we leverage gradle, how we do packaging tests, how we test the REST layer, what our CI infrastructure and tooling around that looks like and finally what happens in order to release Elasticsearch and other parts of the Elastic Stack. ")
                        .url("https://spinscale.de/posts/2020-04-22-testing-and-releasing-elasticsearch-and-the-elastic-stack.html")
                        .category("elasticsearch")
                        .createdAt(Date.from(LocalDate.of(2020, 4, 22).atStartOfDay().toInstant(ZoneOffset.UTC)))
                        .votes(2L)
                        .approved(true)
                        .submittedBy("user1")
                        .build(),
                Link.builder()
                        .title("Introduction into the Java HTTP REST client for Elasticsearch")
                        .description("Elasticsearch comes with a bunch of clients for different languages, like JavaScript, Ruby, Go, .NET, PHP, Perl, Python and most recently even Rust. A late starter (starting in 5.x, but only fully supported from 7.0) was the Java High Level REST client, that intended to replace the TransportClient. This presentation talks a little bit about the reasoning while showing a lot of examples how to use the client, and even comes with a small sample project.")
                        .url("https://spinscale.de/posts/2020-04-15-introduction-into-the-elasticsearch-java-rest-client.html")
                        .category("elasticsearch")
                        .createdAt(Date.from(LocalDate.of(2020, 4, 15).atStartOfDay().toInstant(ZoneOffset.UTC)))
                        .votes(5L)
                        .approved(true)
                        .submittedBy("user1")
                        .build(),
                Link.builder()
                        .title("The journey to support nanosecond timestamps in Elasticsearch")
                        .description("The ability to store dates in nanosecond resolution required a significant refactoring within the Elasticsearch code base. Read this blog post for the why and how on our journey to be able to store dates in nanosecond resolution from Elasticsearch 7.0 onwards.")
                        .url("https://www.elastic.co/blog/journey-support-nanosecond-timestamps-elasticsearch")
                        .category("elasticsearch")
                        .createdAt(Date.from(LocalDate.of(2019, 6, 27).atStartOfDay().toInstant(ZoneOffset.UTC)))
                        .votes(15L)
                        .approved(true)
                        .submittedBy("user1")
                        .build(),
                Link.builder()
                        .title("Elasticsearch Langdetect Ingest Processor")
                        .description("Ingest processor doing language detection for fields. Uses the langdetect plugin to try to find out the language used in a field.")
                        .url("https://github.com/spinscale/elasticsearch-ingest-langdetect")
                        .category("elasticsearch")
                        .createdAt(Date.from(LocalDate.of(2016, 6, 10).atStartOfDay().toInstant(ZoneOffset.UTC)))
                        .votes(42L)
                        .approved(true)
                        .submittedBy("user2")
                        .build(),
                Link.builder()
                        .title("Elasticsearch OpenNLP Ingest Processor")
                        .description("An Elasticsearch ingest processor to do named entity extraction using Apache OpenNLP")
                        .url("https://github.com/spinscale/elasticsearch-ingest-opennlp")
                        .category("elasticsearch")
                        .createdAt(Date.from(LocalDate.of(2016, 4, 25).atStartOfDay().toInstant(ZoneOffset.UTC)))
                        .votes(208L)
                        .approved(true)
                        .submittedBy("user2")
                        .build(),
                Link.builder()
                        .title("A cookiecutter template for an elasticsearch ingest processor plugin")
                        .description("A cookiecutter template for an Elasticsearch Ingest Processor. This should simplify the creation of Elasticsearch Ingest Processors, this template will set up all the different java classes to get started.")
                        .url("https://github.com/spinscale/cookiecutter-elasticsearch-ingest-processor")
                        .category("elasticsearch")
                        .createdAt(Date.from(LocalDate.of(2016, 4, 29).atStartOfDay().toInstant(ZoneOffset.UTC)))
                        .votes(37L)
                        .approved(true)
                        .submittedBy("user2")
                        .build(),
                Link.builder()
                        .title("Using the Elastic Stack to visualize the meetup.com reservation stream")
                        .description("This repository contains a couple of configuration files to monitor the public meetup.com reservation stream via filebeat and index data directly into the Elasticsearch, and also automatically installs a nice to watch dashboard.")
                        .url("https://github.com/spinscale/elastic-stack-meetup-stream")
                        .category("elasticsearch")
                        .createdAt(Date.from(LocalDate.of(2019, 8, 28).atStartOfDay().toInstant(ZoneOffset.UTC)))
                        .votes(1L)
                        .approved(true)
                        .submittedBy("user2")
                        .build(),
                Link.builder()
                        .title("Elasticsearch documentation alfred workflow\n")
                        .description("An alfred workflow to easily search the elastic documentation")
                        .url("https://github.com/spinscale/alfred-workflow-elastic-docs")
                        .category("elastic")
                        .createdAt(Date.from(LocalDate.of(2016, 7, 16).atStartOfDay().toInstant(ZoneOffset.UTC)))
                        .votes(10L)
                        .approved(true)
                        .submittedBy("admin")
                        .build(),
                Link.builder()
                        .title("Slow Query Logging for Elasticsearch and Elastic Cloud")
                        .description("How do you log slow queries in Elasticsearch and especially on Elastic Cloud?")
                        .url("https://xeraa.net/blog/2020_slow-query-logging-elasticsearch-elastic-cloud/")
                        .category("cloud")
                        .createdAt(Date.from(LocalDate.of(2020, 3, 22).atStartOfDay().toInstant(ZoneOffset.UTC)))
                        .votes(15L)
                        .approved(true)
                        .submittedBy("admin")
                        .build(),
                Link.builder()
                        .title("Custom Domains and Anonymous Access on Elastic Cloud")
                        .description("Two common requests for Elastic Cloud are on the one hand: Custom domain names: Rather than using https://<UUID>.<region>.<cloud-provider>.cloud.es.io:9243 you might want to access Kibana or Elasticsearch on https://mydomain.com. and on the other hand anonymous Kibana access: Read-only access to dashboards or canvas for simple sharing without needing to log in.")
                        .url("https://xeraa.net/blog/2020_custom-domains-and-anonymous-access-on-elastic-cloud/")
                        .category("cloud")
                        .createdAt(Date.from(LocalDate.of(2020, 4, 15).atStartOfDay().toInstant(ZoneOffset.UTC)))
                        .votes(22L)
                        .approved(true)
                        .submittedBy("admin")
                        .build()
        );
    }

}
