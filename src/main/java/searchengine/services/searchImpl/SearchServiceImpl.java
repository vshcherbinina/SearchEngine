package searchengine.services.searchImpl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.config.ConfigList;
import searchengine.dto.search.SearchData;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.SiteEntity;
import searchengine.model.repositories.*;
import searchengine.services.SearchService;
import searchengine.services.indexImpl.MorphologyUtils;

import java.io.IOException;
import java.util.*;

@Service
@Getter
@Slf4j
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    @Autowired
    private final SiteRepository siteRepository;
    @Autowired
    private final PageRepository pageRepository;
    @Autowired
    private final IndexRepository indexRepository;
    @Autowired
    private final LemmaRepository lemmaRepository;

    private final ConfigList configList;
    private SearchResponse searchResponse;
    private Set<String> lemmaQuerySet;
    private final List<SearchPage> searchPages = new ArrayList<>();

    private void setLemmaQuerySet(String query) throws IOException {
        MorphologyUtils morphologyUtils;
        morphologyUtils = MorphologyUtils.getInstance();
        lemmaQuerySet = morphologyUtils.getLemmaSet(query);
        log.info("Список лемм для поиска страниц:");
        lemmaQuerySet.forEach(lemmaWord -> log.info("\t" + lemmaWord));
    }

    private void loadPagesForSite(String url) throws IllegalArgumentException {
        Repositories repositories = new Repositories(siteRepository, pageRepository, indexRepository, lemmaRepository, configList);
        SearchSite searchSite = new SearchSite(repositories, lemmaQuerySet);
        if (!searchSite.initialize(url)) {
            return;
        }
        searchSite.loadAndCheckPages();
        searchPages.addAll(searchSite.getSearchPages());
    }

    private void calcRelevanceAndSort() {
        searchPages.forEach(searchPage ->
                searchPage.getLemmaRanks().values().forEach(rank ->
                        searchPage.setAbsoluteRelevance(searchPage.getAbsoluteRelevance() + rank)
                ));
        float maxAbsRelevance = searchPages.stream().max(((o1, o2) ->
                        Float.compare(o1.getAbsoluteRelevance(), o2.getAbsoluteRelevance())
                )).get().getAbsoluteRelevance();
        searchPages.forEach(searchPage ->
                searchPage.setRelativeRelevance(searchPage.getAbsoluteRelevance() / maxAbsRelevance)
                );
        searchPages.sort((o1, o2) -> - Float.compare(o1.getRelativeRelevance(), o2.getRelativeRelevance()));
    }

    private void prepareResponse(int offset, int limit) {
        searchResponse.setCount(searchPages.size());
        log.info("Для ответа запрошены страницы с " + (offset + 1) + " по " + (offset + limit) + "..." );
        if (searchPages.isEmpty() || searchPages.size() <= offset) {
            clearData();
            return;
        }
        int end = Math.min(offset + limit, searchPages.size());
        for (int i = offset; i < end; i++) {
            searchResponse.getData().add(SearchData.getInstanceFromSearchPage(searchPages.get(i)));
        }
        if (end == searchPages.size()) {
            clearData();
        }
    }

    private void clearData() {
        searchPages.clear();
    }

    private List<String> prepareListUrlSite(String url) {
        List<String> urlList = new ArrayList<>();
        if (url.isBlank()) {
            List<SiteEntity> siteList = siteRepository.findAll();
            siteList.forEach((site -> urlList.add(site.getUrl())));
        } else {
            urlList.add(url);
        }
        return urlList;
    }

    private void loadSites(String site) {
        List<String> urlList = prepareListUrlSite(site);
        if (urlList.isEmpty()) {
            throw new IllegalArgumentException("Нет проиндексированных сайтов - выполните полную индексацию или отдельной страницы");
        }
        urlList.forEach(this::loadPagesForSite);
        if (searchPages.isEmpty()) {
            return;
        }
        calcRelevanceAndSort();
        log.info("Подготовлены к выдаче страницы (" + searchPages.size() + "): ");
        searchPages.forEach(searchPage -> log.info("\t" + searchPage));
    }

    private ResponseEntity<SearchResponse> resultFromCash(int offset, int limit) {
        if (searchPages.isEmpty()) {
            return searchResponse.toErrorResponse(HttpStatus.BAD_REQUEST, "Данные устарели. Выполните поиск снова");
        }
        try {
            prepareResponse(offset, limit);
        } catch (Exception e) {
            return searchResponse.toErrorResponse(e, "Ошибка формирования результатов поиска");
        }
        return searchResponse.toSuccessResponse();
    }

    @Override
    public ResponseEntity<SearchResponse> getSearchResult(String query, int offset, int limit, String site) {
        searchResponse = new SearchResponse();
        if (offset > 0) {
            return resultFromCash(offset, limit);
        }
        clearData();
        if (query.isBlank()) {
            return searchResponse.toErrorResponse(HttpStatus.BAD_REQUEST, "Задан пустой поисковый запрос");
        }
        try {
            setLemmaQuerySet(query);
            if (lemmaQuerySet.isEmpty()) {
                return searchResponse.toErrorResponse(HttpStatus.BAD_REQUEST, "Некорректный поисковый запрос");
            }
            loadSites(site);
            prepareResponse(offset, limit);
        } catch (Exception e) {
            searchResponse.toErrorResponse(e, "Ошибка поиска");
        }
        return searchResponse.toSuccessResponse();
    }


}
