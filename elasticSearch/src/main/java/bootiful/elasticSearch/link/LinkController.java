package bootiful.elasticSearch.link;

import bootiful.elasticSearch.base.AdminService;
import bootiful.elasticSearch.base.BaseController;
import bootiful.elasticSearch.user.AuthUser;
import bootiful.elasticSearch.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.RefreshPolicy;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author pari on 19/01/24
 */
@Controller
@RequestMapping("link")
@Slf4j
class LinkController extends BaseController {
    private final ElasticsearchOperations elasticsearchRestTemplate;

    @Autowired
    public LinkController(ElasticsearchOperations elasticsearchTemplate, AdminService adminService) {
        super(adminService.get());
        this.elasticsearchRestTemplate = elasticsearchTemplate;
    }

    // check out single entry
    @GetMapping(path = "{id}")
    public String show(@AuthenticationPrincipal final AuthUser principal,
                       @PathVariable("id") final String id,
                       final Model model) {
        final Link link = elasticsearchRestTemplate.get(id, Link.class);
        // only admin can see an unapproved link!
        if (link != null && (link.isApproved() || isAdmin(principal))) {
            model.addAttribute("links", Collections.singletonList(link));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        enrichModelWithPrincipal(model, principal);
        return "main";
    }


    @PostMapping
    public String submitLink(@AuthenticationPrincipal final AuthUser principal,
                             @RequestParam("description") final String description,
                             @RequestParam("title") final String title,
                             @RequestParam("url") final String url,
                             @RequestParam("category") final String category) {

//        final TermQueryBuilder qb = QueryBuilders.termQuery("url", url);
        final CriteriaQuery urlCriteria = new CriteriaQuery(new Criteria("url").is(url));
        final SearchHit<Link> hit = this.elasticsearchRestTemplate.searchOne(new NativeQueryBuilder().withQuery(urlCriteria).build(), Link.class);
//        final SearchHit<Link> hit = elasticsearchRestTemplate.searchOne(new NativeSearchQuery(qb), Link.class);
        if (hit == null) {
//            final BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
//                    .must(QueryBuilders.termQuery())
//                    .must(QueryBuilders.termQuery("approved", false));
            final NativeQuery query = NativeQuery.builder()
                    .withQuery(q ->
                            q.bool(b->
                                    b.must(m -> m
                                        .term(t-> t
                                            .field("approved").value(false)
                                            .field("submittedBy").value(principal.getUsername()))))).build();
            final long linksSubmittedByUser = elasticsearchRestTemplate.count(query, Link.class);
            if (linksSubmittedByUser < 50) {
                final Link newLink = Link.builder()
                        .title(title)
                        .description(description)
                        .url(url)
                        .category(category)
                        .votes(1L)
                        .approved(false)
                        .createdAt(new Date())
                        .submittedBy(principal.getUsername())
                        .build();
                final Link saved = this.elasticsearchRestTemplate.save(newLink);
                log.info("Saved link id: {}", saved.getId());
            }
        } else {
            if (hit.getContent().isApproved()) {
                return "redirect:/link/" + hit.getContent().getId();
            }
        }

        // redirect after post baby
        return "redirect:/";
    }



    @PostMapping("{id}/vote")
    public String vote(@AuthenticationPrincipal final AuthUser principal,
                       @PathVariable("id") final String id,
                       @RequestHeader(value = "referer", required = false) final String referer) {
        String username = principal.getUsername();

        // this can be optimized A LOT by executing a count request and then run an update request for the user
        // this requires a full serialization/deserialization circle and thus is highly ineffective
        // OTOH voting will not happen super often
        User user = elasticsearchRestTemplate.get(username, User.class);
        final User.Builder builder = User.builder();
        final boolean hasUserAlreadyVoted;
        if (user == null) {
            builder.id(username);
            user= builder.build();
            hasUserAlreadyVoted = false;
        } else {
            hasUserAlreadyVoted = user.getIds().contains(id);
        }
        if (!hasUserAlreadyVoted) {
            // store new user or update
            if (user.getIds() == null) {
                builder.ids(Collections.singletonList(id));
            } else {
                final List<String> ids = user.getIds();
                ids.add(id);
                builder.ids(ids);
            }
            elasticsearchRestTemplate.save(builder.build());

            // increment vote count on link via script, so that concurrent updates would work
            final UpdateQuery updateQuery = UpdateQuery.builder(id).withLang("painless").withScript("ctx._source.votes = ctx._source.votes + 1;").withRetryOnConflict(3)
                    .withRefreshPolicy(RefreshPolicy.IMMEDIATE).build();
            elasticsearchRestTemplate.update(updateQuery, IndexCoordinates.of("links"));
        } else {
            log.info("user [{}] tried to vote a second time for id [{}]", user.getId(), id);
        }

        // redirect after post baby
        if (referer != null && referer.contains(id)) {
            return "redirect:/link/" + id;
        } else {
            return "redirect:/";
        }
    }

    @PostMapping("{id}/delete")
    public String delete(@AuthenticationPrincipal AuthUser principal, @PathVariable("id") final String id) {
        ensureAdmin(principal);
        // possibly we could refresh here, so that the document is missing immediately afer the refresh
        elasticsearchRestTemplate.delete(id, Link.class);
        return "redirect:/";
    }

    @PostMapping("{id}/approve")
    public String approve(@AuthenticationPrincipal AuthUser principal, @PathVariable("id") final String id) {
        ensureAdmin(principal);

        final UpdateQuery updateQuery = UpdateQuery.builder(id)
                .withRefreshPolicy(RefreshPolicy.IMMEDIATE)
                .withDocument(Document.from(Collections.singletonMap("approved", true)))
                .build();
        elasticsearchRestTemplate.update(updateQuery, IndexCoordinates.of("links"));

        return "redirect:/unapproved";
    }
}
