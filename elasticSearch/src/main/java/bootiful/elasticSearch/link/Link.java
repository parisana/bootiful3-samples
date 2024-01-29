package bootiful.elasticSearch.link;

import bootiful.elasticSearch.helper.Indices;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;

/**
 * @author pari on 19/01/24
 */
@NoArgsConstructor@AllArgsConstructor@Getter
@Builder(builderClassName = "Builder")
@Document(indexName = Indices.LINK_INDEX)
@Setting(settingPath = "static/es-settings.json")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Link {
    @Id
    private String id;
    @Field(type = FieldType.Keyword)
    private String category;
    private String title;
    private String description;
    @Field(type = FieldType.Keyword)
    private String url;
    @Field(name="submittedBy", type = FieldType.Keyword)
    private String submittedBy;

    @Field(name = "createdAt", type=FieldType.Date, format = DateFormat.date_optional_time)
    private Date createdAt;

    @Field(type = FieldType.Boolean)
    private boolean approved;

    @Field(type = FieldType.Rank_Feature)
    private Long votes;

    static class Builder {
        Link build() {
            if (category.length() > 100) {
                throw new IllegalArgumentException("category was more than 100 characters");
            }
            this.category = stripHTML(category.toLowerCase(Locale.ROOT));
            if (title.length() > 100) {
                throw new IllegalArgumentException("title was more than 100 characters");
            }
            this.title = stripHTML(title);
            if (url.length() > 500) {
                throw new IllegalArgumentException("url was more than 500 characters");
            }
            final String data = stripHTML(url);
            try {
                this.url = new URI(data).toURL().toString();
            } catch (MalformedURLException| URISyntaxException e) {
                throw new IllegalArgumentException("invalid url: " + url);
            }
            if (description.length() > 1000) {
                throw new IllegalArgumentException("description was more than 1000 characters");
            }
            this.description = stripHTML(description);
            return new Link(id, category, title, description, url, submittedBy, createdAt, approved, votes);
        }
    }
    public String getAgo() {
        return ago(ZonedDateTime.now(ZoneOffset.UTC) ,createdAt.toInstant().atZone(ZoneOffset.UTC));
    }

    static String ago(ZonedDateTime now, ZonedDateTime input) {
        final Duration duration = Duration.between(input, now);
        final Period period = Period.between(input.toLocalDate(), now.toLocalDate());

        if (duration.isNegative()) {
            throw new IllegalArgumentException("expected input date [" + input + "] to be later than now [" + now + "]");
        }

        if (period.getYears() > 1) {
            return period.getYears() + " years ago";
        }
        if (period.getYears() == 1) {
            return "1 year ago";
        }
        if (period.getMonths() > 1) {
            return period.getMonths() + " months ago";
        }
        if (period.getMonths() == 1) {
            return "1 month ago";
        }
        if (period.getDays() > 1) {
            return period.getDays() + " days ago";
        }
        if (period.getDays() == 1) {
            return "yesterday";
        }
        if (duration.toHours() > 1) {
            return duration.toHours() + " hours ago";
        }
        if (duration.toHours() == 1) {
            return "1 hour ago";
        }
        if (duration.toMinutes() > 5) {
            return duration.toMinutes() + " minutes ago";
        }
        return "just now";
    }
    // ensure no HTML gets into Elasticsearch
    // ex: <p>I&apos;m so <b>happy</b>!</p> ==> \nI'm so happy!\n
    private static String stripHTML(String input) {
        StringBuilder builder = new StringBuilder();
        try (HTMLStripCharFilter filter = new HTMLStripCharFilter(new StringReader(input))) {
            int ch;
            while ((ch = filter.read()) != -1) {
                builder.append((char)ch);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return builder.toString();
    }

}
