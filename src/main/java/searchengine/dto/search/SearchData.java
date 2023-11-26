package searchengine.dto.search;

import lombok.Data;
import searchengine.services.searchImpl.SearchPage;

@Data
public class SearchData {
    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private float relevance;

    public static SearchData getInstanceFromSearchPage(SearchPage searchPage) {
        searchPage.prepareForResponse();
        SearchData searchData = new SearchData();
        searchData.setSite(searchPage.getSite().getUrl());
        searchData.setSiteName(searchPage.getSite().getName());
        searchData.setUri(searchPage.getPage().getPath());
        searchData.setRelevance(searchPage.getRelativeRelevance());
        searchData.setTitle(searchPage.getTitle());
        searchData.setSnippet(searchPage.getSnippet());
        return searchData;
    }
}
