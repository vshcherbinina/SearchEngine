package searchengine.dto.search;

import lombok.Builder;
import lombok.Data;
import searchengine.services.searchImpl.SearchPage;

@Builder
@Data
public class SearchData {
    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private float relevance;

    public static SearchData getInstanceFromSearchPage(SearchPage searchPage) {
        return builder()
                .site(searchPage.getSite().getUrl())
                .siteName(searchPage.getSite().getName())
                .uri(searchPage.getPage().getPath())
                .relevance(searchPage.getRelativeRelevance())
                .title(searchPage.getTitle())
                .snippet(searchPage.getSnippet())
                .build();
    }
}
