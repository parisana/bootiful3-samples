package bootiful.elasticSearch.user;

import bootiful.elasticSearch.helper.Indices;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

/**
 * @author pari on 19/01/24
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(builderClassName = "Builder")
@Document(indexName = Indices.USER_INDEX, versionType = Document.VersionType.INTERNAL)
public class User {
    @Id
    private String id;

    @Field(type = FieldType.Keyword, name="userId")
    private String userId;

    @Field(type = FieldType.Keyword, name="ids")
    private List<String> ids;
}
