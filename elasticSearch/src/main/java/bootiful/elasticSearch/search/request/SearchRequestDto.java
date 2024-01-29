package bootiful.elasticSearch.search.request;

import co.elastic.clients.elasticsearch._types.SortOrder;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author pari on 28/01/24
 */

@Builder
@AllArgsConstructor@NoArgsConstructor
@Getter
public final class SearchRequestDto extends PagedRequestDto {

    private List<String> fields;
    private String searchTerm;
    private String sortBy;
    private SortOrder order;
    // for ranged queries
    private DateRange dateRange;

    @Builder
    @AllArgsConstructor@NoArgsConstructor
    @Getter
    public static class DateRange {
        @JsonFormat(pattern = "yyyy-MM-dd['T'HH:mm:ss.SSSZ]")
        private String startDate;
        @JsonFormat(pattern = "yyyy-MM-dd['T'HH:mm:ss.SSSZ]")
        private String endDate;
    }
}
