package searchengine.services.indexImpl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.UrlStructure;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.RecursiveTask;

@Getter
@Slf4j
public class PageExecutor extends RecursiveTask<Boolean> implements AutoCloseable{

    private final PageLoader pageLoader;
    private final SiteExecutor siteExecutor;
    private final HashMap<String, PageExecutor> childrenTask;

    public PageExecutor(SiteExecutor siteExecutor, UrlStructure urlStructure) {
        this.pageLoader = new PageLoader(siteExecutor.getSiteLoader(), urlStructure);
        this.siteExecutor = siteExecutor;
        childrenTask = new HashMap<>();
    }

    private void addChildIfNotExist(UrlStructure url) {
        if (childrenTask.containsKey(url.getPath())) {
            return;
        }
        if (siteExecutor.isPageExists(url.getPath())) {
            return;
        }
        PageExecutor child = new PageExecutor(siteExecutor, url);
        child.fork();
        childrenTask.put(url.getPath(), child);
    }

    private boolean isInterrupted() {
        return siteExecutor.isInterrupted();
    }

    private void parseChildren() {
        Document document = Jsoup.parse(pageLoader.getPage().getContent());
        Elements links = document.select("a[href]");
        for (Element link: links) {
            if (isInterrupted()) {
                break;
            }
            String childPath = link.attr("href");
            if (childPath.isBlank()) {
                continue;
            }
            UrlStructure urlStructure = new UrlStructure(childPath);
            log.debug("path: " + childPath + " -> " + urlStructure.getPath());
            if (urlStructure.equalsDomainName(siteExecutor.getSiteLoader().getSite().getUrlStructure())) {
                addChildIfNotExist(urlStructure);
            }
        }
    }

    private void cancelChildren() {
        childrenTask.values().forEach(task -> {
            task.cancel(true);
            task.cancelChildren();
        });
    }

    @Override
    protected Boolean compute() throws IllegalArgumentException {
        if (isInterrupted()) {
            return false;
        }
        try {
            pageLoader.loadAndSavePage();
            pageLoader.getIndexStorage().findLemmas();
        } catch (IOException e) {
            throw new IllegalArgumentException("Ошибка загрузки страницы: " + e);
        }
        if (!pageLoader.getPage().isSuccessLoad()) {
            return true;
        }
        parseChildren();
        if (isInterrupted()) {
            cancelChildren();
            return false;
        }
        boolean result = true;
        for (PageExecutor task : childrenTask.values()) {
            result = result & task.join();
            task.close();
        }
        return result;
    }

    @Override
    public void close() {
        childrenTask.clear();
    }
}
