package searchengine.services.indexImpl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.SiteStatus;
import searchengine.model.repositories.Repositories;

import java.time.LocalDateTime;

@Getter
@Slf4j
public class SiteLoader extends Thread implements AutoCloseable{
    private final SiteEntity site;
    private final PageLoader pageLoader;
    private final Repositories repositories;
    private final LemmaStorage lemmaStorage;

    private final int sleepLoadPageMs;
    private final String userAgent;
    private final String referrer;

    public SiteLoader(SiteEntity site, Repositories repositories) {
        this.site = site;
        this.repositories = repositories;
        this.pageLoader = new PageLoader(this, site.getUrlStructure());
        lemmaStorage = new LemmaStorage(this);
        sleepLoadPageMs = repositories.configList().getSleepLoadPageMs();
        userAgent = repositories.configList().getUserAgent();
        referrer = repositories.configList().getReferrer();
    }

    public synchronized LemmaEntity createLemmaIfNotExist(String word) {
        LemmaEntity lemma = lemmaStorage.getLemmaIfExist(word);
        if (lemma != null) {
            return lemma;
        }
        return lemmaStorage.createAndSaveLemma(word);
    }

    public void saveSite() throws IllegalArgumentException  {
        try {
            SiteEntity newSite = repositories.siteRepository().save(site);
            site.setId(newSite.getId());
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка при сохранении сайта " + site + ":\n" + e.getMessage());
        }
    }

    public void setStatusAndSaveSite(SiteStatus status) {
        site.setStatus(status);
        site.setStatusTime(LocalDateTime.now());
        saveSite();
        log.info("Текущее состояние сайта: " + site);
    }

    public void updateIndexing() {
        setStatusAndSaveSite(SiteStatus.INDEXING);
        log.info("\tколичество лемм - " + lemmaStorage.getLemmas().size());
    }

    public void startIndexing() {
        log.info("Запуск индексации сайта: " + site.getUrl());
        site.setLastError("");
        setStatusAndSaveSite(SiteStatus.INDEXING);
    }

    public void stopIndexing(String error) {
        log.warn("Остановка индексации сайта: " + site.getUrl() + ":\n" + error);
        site.setLastError(error);
        lemmaStorage.updateLemmas();
        setStatusAndSaveSite(SiteStatus.FAILED);
    }

    public void finishIndexing() {
        log.info("Завершение индексации сайта: " + site.getUrl());
        lemmaStorage.updateLemmas();
        setStatusAndSaveSite(SiteStatus.INDEXED);
    }

    public synchronized void loadPage(PageEntity page) {
        try {
            Connection connection = Jsoup.connect(page.getAbsUrl())
                    .userAgent(userAgent)
                    .referrer(referrer)
                    .ignoreHttpErrors(true);
            Connection.Response response = connection.execute();
            page.setCode(response.statusCode());
            if (page.isSuccessLoad()) {
                Document document = connection.get();
                page.setContent(document.toString());
            } else {
                page.setContent(response.body());
            }
            Thread.sleep(sleepLoadPageMs);
        } catch (Exception e) {
            page.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            page.setContent(e.getMessage());
        }
    }

    public void loadPage() {
        loadPage(pageLoader.getPage());
    }

    @Override
    public void run() {
        startIndexing();
        try {
            lemmaStorage.setLemmasLoadFromDB(true);
            pageLoader.prepareDataIfPageExist();
            pageLoader.savePage();
            pageLoader.getIndexStorage().findLemmas();
            finishIndexing();
        } catch (Exception e) {
            stopIndexing(e.getMessage());
        }
        log.info("Индексация сайта завершена: " + site.getUrlStructure().getUrl());
    }

    @Override
    public void close(){
        pageLoader.close();
        lemmaStorage.close();
    }
}
