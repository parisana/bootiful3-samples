package bootiful.elasticSearch.search.utils;

import bootiful.elasticSearch.search.request.SearchRequestDto;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.json.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author pari on 28/01/24
 */
@Slf4j
public final class ESearchUtil {

    public static SearchRequest buildSearchRequest(final String indexName, final SearchRequestDto dto) {
        try {
            final int page = dto.getPage();
            final int size = dto.getSize();
            final int from = page*size;
            final BoolQuery.Builder boolQueryBuilder = QueryBuilders.bool();
            if (dto.getDateRange() != null && dto.getDateRange().getStartDate() != null) {
                final Query rangedQuery = buildRangedQuery(dto.getDateRange());
                boolQueryBuilder.must(rangedQuery);
            }
            final Query searchQuery = buildSearchQuery(dto);
            if (searchQuery != null)
                boolQueryBuilder.must(searchQuery);
            final SearchRequest.Builder searchReqBuilder = new SearchRequest.Builder();
            searchReqBuilder.from(from).size(size).postFilter(b-> b.bool(boolQueryBuilder.build()));
            if (dto.getSortBy() != null) {
                searchReqBuilder.sort(sb-> sb.field(fsb-> fsb.field(dto.getSortBy()).order(dto.getOrder() != null ? dto.getOrder() : SortOrder.Asc)));
            }
            return searchReqBuilder.index(indexName).build();
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    private static Query buildSearchQuery(SearchRequestDto dto) {
        if (dto == null) return null;
        final List<String> fields = dto.getFields();
        if (CollectionUtils.isEmpty(fields)) return null;
        if (dto.getSearchTerm() == null || dto.getSearchTerm().isBlank()) {
            return QueryBuilders.matchAll().build()._toQuery();
        }
        if (fields.size() > 1) {
//            final Criteria.CriteriaChain criteria = new Criteria.CriteriaChain();
//            fields.forEach(f-> criteria.add(new Criteria(f).is(dto.getSearchTerm())));
//            final NativeQueryBuilder builder = NativeQuery.builder();
//            return builder.withQuery(q -> q.multiMatch(m -> m.type(TextQueryType.CrossFields).operator(Operator.And).fields(fields))).build();
            return QueryBuilders.multiMatch(q -> q.query(dto.getSearchTerm()).type(TextQueryType.CrossFields).operator(Operator.And).fields(fields));
        }
        return QueryBuilders.match(qb -> qb.field(fields.getFirst()).query(dto.getSearchTerm()).operator(Operator.And));
    }
    private static Query buildRangedQuery(SearchRequestDto.DateRange dateRange) {
        return QueryBuilders.range(rb -> rb
                .field("createdAt")
                .format("date_optional_time")
                .gte(JsonData.of(dateRange.getStartDate()))
                .lte(JsonData.of(dateRange.getEndDate()))
        );
    }
}
