package searchengine.services.searchImpl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.config.Site;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;
import searchengine.model.UrlStructure;
import searchengine.model.repositories.Repositories;

import java.util.*;

@Slf4j
@Getter
public class SearchSite {

    public final int frequencyPopularLemma;
    public final int minCountLemmas;

    private final Repositories repositories;
    private SiteEntity site;
    private final Set<String> lemmaQuerySet;
    private final List<LemmaEntity> lemmas = new ArrayList<>();
    private final List<SearchPage> searchPages = new ArrayList<>();

    public SearchSite(Repositories repositories, Set<String> lemmaQuerySet) {
        this.repositories = repositories;
        this.lemmaQuerySet = lemmaQuerySet;
        frequencyPopularLemma = repositories.configList().getFrequencyPopularLemma();
        minCountLemmas = repositories.configList().getMinCountLemmas();
    }

    private void loadSite(String url) throws IllegalArgumentException {
        UrlStructure urlStructure = new UrlStructure(url);
        Site config = repositories.findSiteConfig(urlStructure);
        if (config == null) {
            throw new IllegalArgumentException("Данный сайт не указан в конфигурационном файле");
        }
        site = repositories.getSiteEntity(urlStructure);
        if (site == null) {
            throw new IllegalArgumentException("Сайт не индексирован. Выполните полную индексацию и повторите поиск");
        }
    }

    private boolean loadLemmas() {
        lemmaQuerySet.forEach(word -> {
            List<LemmaEntity> lemmaEntities = repositories.lemmaRepository().findAllByLemmaAndSiteId(word, site.getId());
            for (LemmaEntity lemma : lemmaEntities) {
                lemma.setSite(site);
                lemmas.add(lemma);
                break;
            }
        });
        log.info("Найденные леммы в базе данных:");
        lemmas.forEach(lemma -> log.info("\t" + lemma));
        if (lemmaQuerySet.size() != lemmas.size()) {
            log.warn("Не все слова найдены в базе данных - поиск остановлен");
            return false;
        }
        return true;
    }

    private void removePopularLemmas() {
        log.info("Исключение популярных лемм из поиска, с частотой повторов на страницах > " + frequencyPopularLemma + "....");
        if (frequencyPopularLemma <= 0) {
            log.info("\tзначение атрибута = 0 или не задано <frequency-popular-lemma>, исключение не будет производиться");
            return;
        }
        if (lemmas.size() <= minCountLemmas) {
            log.info("\tколичество лемм не превышает минимальное количество (" + minCountLemmas + "), исключение не будет производиться");
            return;
        }
        lemmas.sort((o1, o2) -> - Integer.compare(o1.getFrequency(), o2.getFrequency()));
        Iterator<LemmaEntity> iterator = lemmas.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            LemmaEntity lemma = iterator.next();
            if (lemma.getFrequency() > frequencyPopularLemma) {
                iterator.remove();
            }
            if (lemmas.size() <= minCountLemmas) {
                break;
            }
        }
    }

    private void filterPages() {
        if (lemmas.size() == 1 || searchPages.isEmpty()) {
            return;
        }
        log.info("Проверка наличия остальных лемм на найденных страницах...");
        for (int i = 1; i < lemmas.size(); i++) {
            if (searchPages.isEmpty()) {
                log.info("Все страницы исключены из результатов поиска");
                break;
            }
            LemmaEntity lemma = lemmas.get(i);
            Iterator<SearchPage> iterator = searchPages.iterator();
            while (iterator.hasNext()) {
                SearchPage searchPage = iterator.next();
                List<IndexEntity> indexes = repositories.indexRepository()
                        .findCustomByLemmaIdAndPageId(lemma.getId(), searchPage.getPage().getId());
                if (indexes.isEmpty()) {
                    log.info("\tлемма \"" + lemma.getLemma()+ "\" не найдена на странице \"" + searchPage.getPage().getPath() + "\" - страница исключается из результатов поиска");
                    iterator.remove();
                }
                indexes.forEach(index -> searchPage.getLemmaRanks().put(lemma.getLemma(), index.getRank()));
            }
        }
    }

    private void loadPagesForRareLemma() {
        if (lemmas.isEmpty()) {
            return;
        }
        LemmaEntity firstLemma = lemmas.get(0);
        List<IndexEntity> firstLemmaIndexes = repositories.indexRepository().findCustomByLemmaId(firstLemma.getId());
        log.info("Загрузка страниц для самой редкой леммы \"" + firstLemma.getLemma() + "\", найдено (страниц): " + firstLemmaIndexes.size());
        if (firstLemmaIndexes.isEmpty()) {
            return;
        }
        firstLemmaIndexes.forEach(index -> {
            SearchPage searchPage = new SearchPage(site, index.getPage(), repositories.configList().getSnippetMaxLength());
            searchPage.getLemmaRanks().put(firstLemma.getLemma(), index.getRank());
            searchPages.add(searchPage);
        });
        searchPages.forEach(searchPage -> log.info("\t\"" + searchPage.getPage().getPath() + "\": количество повторов = " + searchPage.getLemmaRanks()));
    }

    private void lemmasSort() {
        lemmas.sort(Comparator.comparingInt(LemmaEntity::getFrequency));
        log.info("Сортированный список лемм для поиска страниц: ");
        lemmas.forEach(lemma -> log.info("\t" + lemma));
    }

    public void loadAndCheckPages() {
        if (lemmas.isEmpty()) {
            return;
        }
        removePopularLemmas();
        lemmasSort();
        loadPagesForRareLemma();
        filterPages();
    }

    public boolean initialize(String url) throws IllegalArgumentException {
        loadSite(url);
        return loadLemmas();
    }

}
