package searchengine.services.indexImpl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import searchengine.model.SiteEntity;
import searchengine.model.repositories.Repositories;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ForkJoinPool;

@Getter
@Slf4j
public class SiteExecutor extends Thread implements AutoCloseable{
    private final SiteLoader siteLoader;
    private final HashSet<String> pages;
    private final int countPagesForUpdate;

    public SiteExecutor(SiteEntity site, Repositories repositories) {
        this.siteLoader = new SiteLoader(site, repositories);
        pages = new HashSet<>();
        countPagesForUpdate = repositories.configList().getCountPagesForUpdate();
    }

    public synchronized boolean isPageExists(String path) {
        if (pages.contains(path)) {
            return true;
        }
        pages.add(path);
        if (pages.size() % countPagesForUpdate == 0) {
            siteLoader.updateIndexing();
            log.info("\tколичество страниц - " + pages.size());
        }
        return false;
    }

    @Override
    public void run() {
        siteLoader.startIndexing();
        pages.add(siteLoader.getPageLoader().getPage().getPath());
        PageExecutor pageExecutor = new PageExecutor(this, siteLoader.getSite().getUrlStructure());
        try {
            Boolean result = new ForkJoinPool().invoke(pageExecutor);
            if (result) {
                siteLoader.finishIndexing();
            } else {
                siteLoader.stopIndexing("Индексация остановлена пользователем");
            }
        } catch (Exception e) {
            Arrays.stream(e.getStackTrace()).toList().forEach(error -> log.error('\t' + error.toString()));
            interrupt();
            siteLoader.stopIndexing(e.getMessage());
        } finally {
            log.info("Индексация сайта завершена: " + siteLoader.getSite().getUrlStructure().getUrl());
            close();
        }

    }

    @Override
    public void close() {
        siteLoader.close();
        pages.clear();
    }
}
