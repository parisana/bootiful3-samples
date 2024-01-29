package bootiful.elasticSearch.user;

import bootiful.elasticSearch.base.AdminService;
import bootiful.elasticSearch.base.BaseController;
import bootiful.elasticSearch.link.Link;
import bootiful.elasticSearch.search.request.SearchRequestDto;
import bootiful.elasticSearch.search.utils.ESearchUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author pari on 19/01/24
 */
@Controller
@RequestMapping(path = "/")
@Slf4j
class UserController extends BaseController {

    private final ElasticsearchOperations elasticsearchRestTemplate;
    private final ElasticsearchClient client;

    @Autowired
    public UserController(ElasticsearchOperations elasticsearchTemplate, AdminService adminService, ElasticsearchClient client) {
        super(adminService.get());
        this.elasticsearchRestTemplate = elasticsearchTemplate;
        this.client = client;
    }

    @GetMapping
    public String main(@AuthenticationPrincipal AuthUser principal,
                       @RequestParam(value = "q", required = false) final String q,
                       final Model model) {
        try {
            final SearchRequestDto searchRequestDto = SearchRequestDto.builder()
                    .fields(List.of("title", "description"))
                    .searchTerm(q)
                    .build();
            final SearchRequest searchRequest = ESearchUtil.buildSearchRequest("link", searchRequestDto);
            final List<Link> links = searchInternal(searchRequest, Link.class);
            model.addAttribute("links", links);
        } catch (Exception e) {
            log.error("error querying for [" + q + "]", e);
            model.addAttribute("links", Collections.<Link>emptyList());
        }
        model.addAttribute("q", q);
        enrichModelWithPrincipal(model, principal);
        return "main";
    }

    // list unapproved links, this should only be reachable by an admin
    @GetMapping("unapproved")
    public String showUnapproved(@AuthenticationPrincipal AuthUser principal, final Model model) {
        ensureAdmin(principal);
        enrichModelWithPrincipal(model, principal);
        final NativeQuery query = NativeQuery.builder()
                .withFilter(b -> b.term(t -> t.field("approved").value(false)))
                .withPageable(PageRequest.of(0, 50))
                .withSort(Sort.by(new Sort.Order(Sort.Direction.DESC, "createdAt"))).build();

        model.addAttribute("links", search(query));

        return "main";
    }

    private List<Link> search(Query query) {
        final SearchHits<Link> result = elasticsearchRestTemplate.search(query, Link.class);
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        return result.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());
    }

    private <T> List<T> searchInternal(final SearchRequest request, Class<T> documentClass) {
        if (request == null) {
            log.error("Failed to build search request");
            return Collections.emptyList();
        }

        try {
            final SearchResponse<T> response = client.search(request, documentClass);

            final List<Hit<T>> searchHits = response.hits().hits();
            final List<T> results = new ArrayList<>(searchHits.size());
            for (Hit<T> hit : searchHits) {
                results.add(hit.source());
            }

            return results;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
