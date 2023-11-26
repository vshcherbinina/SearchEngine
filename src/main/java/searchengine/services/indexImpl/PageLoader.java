package searchengine.services.indexImpl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import searchengine.model.PageEntity;
import searchengine.model.UrlStructure;
import searchengine.model.repositories.Repositories;

import java.io.IOException;
import java.util.List;

@Getter
@Slf4j
public class PageLoader implements AutoCloseable{
    private final SiteLoader siteLoader;
    private final PageEntity page;
    private final Repositories repositories;
    private final IndexStorage indexStorage;

    public PageLoader(SiteLoader siteLoader, UrlStructure urlStructure) {
        this.repositories = siteLoader.getRepositories();
        this.siteLoader = siteLoader;
        page = new PageEntity(siteLoader.getSite(), urlStructure);
        indexStorage = new IndexStorage(page, siteLoader);
    }

    public void savePage() throws IllegalArgumentException {
        try {
            PageEntity newPage = repositories.pageRepository().save(page);
            page.setId(newPage.getId());
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка при сохранении страницы " + page + ":\n" + e.getMessage());
        }
    }

    public void loadAndSavePage() throws IllegalArgumentException, IOException {
        siteLoader.loadPage(page);
        savePage();
    }

    public void prepareDataIfPageExist() {
        List<PageEntity> existPageList = repositories.pageRepository().findAllByPathAndSiteId(page.getPath(), page.getSite().getId());
        for (PageEntity existPage : existPageList) {
            existPage.setSite(page.getSite());
            log.info("Страница найдена среди индексируемых ранее и будет удалена: " + existPage);
            siteLoader.getLemmaStorage().loadLemmasBeforeDeletePage(existPage);
            log.info("Удаление устаревших индексов...");
            repositories.indexRepository().deleteAllByPageId(existPage.getId());
            log.info("Удаление страницы...");
            repositories.pageRepository().deleteById(existPage.getId());
        }
    }

    @Override
    public void close() {
        indexStorage.close();
    }
}
