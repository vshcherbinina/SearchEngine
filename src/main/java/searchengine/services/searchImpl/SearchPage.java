package searchengine.services.searchImpl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.HashMap;
import java.util.Objects;

@RequiredArgsConstructor
@Getter
@Setter
@Slf4j
public class SearchPage{
    private final SiteEntity site;
    private final PageEntity page;
    private final HashMap<String, Float> lemmaRanks = new HashMap<>();
    private float absoluteRelevance = 0;
    private float relativeRelevance = 0;
    private String title;
    private String snippet;
    private final int snippetMaxLength;

    public boolean checkLemmas() {
        if (lemmaRanks.isEmpty()) {
            log.warn("Проверка результатов поиска страниц по словам (леммам): список лемм пустой");
            return false;
        }
        return true;
    }

    public boolean checkPage() {
        if (page == null || !page.isSuccessLoad()) {
            log.warn("Проверка результатов поиска страниц по словам (леммам): страница не загружена");
            return false;
        }
        return true;
    }

    private void setTitleFromPageContent() {
        if (!checkPage()) {
            return;
        }
        Document document = Jsoup.parse(page.getContent());
        title = document.title();
    }

    private void setSnippetFromPageContent() {
        SnippetEngine snippetEngine = new SnippetEngine(snippetMaxLength);
        if (snippetEngine.initialize(this)) {
            snippet = snippetEngine.getTextSnippets();
        }
    }

    public void prepareForResponse() {
        setTitleFromPageContent();
        setSnippetFromPageContent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchPage that)) return false;
        return Objects.equals(getPage(), that.getPage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPage());
    }

    @Override
    public String toString() {
        return "page: \"" + page.getPath() + "\"" +
                ", lemmaRanks: " + lemmaRanks +
                ", absoluteRelevance = " + absoluteRelevance +
                ", relativeRelevance = " + relativeRelevance
                ;
    }

}
