package bootiful.elasticSearch.search.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author pari on 28/01/24
 */
@NoArgsConstructor@AllArgsConstructor@Getter
public class PagedRequestDto {
    private int page=0;
    private int size=100;
}
