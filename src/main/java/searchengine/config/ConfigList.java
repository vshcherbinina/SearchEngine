package searchengine.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@RequiredArgsConstructor
public class ConfigList {
    private final static int SLEEP_TIME_MS_MIN = 1_000;
    private final static int COUNT_PAGES_FOR_UPDATE_DEFAULT = 200;
    private final static int SLEEP_LOAD_PAGE_MS_DEFAULT = 1_000;
    private final static int MIN_COUNT_LEMMAS_DEFAULT = 300;

    private final SiteList sites;
    @Value("${indexing-settings.sleep-time-ms}")
    private int sleepTimeMs;
    @Value("${indexing-settings.count-pages-for-update}")
    private int countPagesForUpdate;
    @Value("${indexing-settings.load-page.sleep-load-page-ms}")
    private int sleepLoadPageMs;
    @Value("${indexing-settings.load-page.user-agent}")
    private String userAgent;
    @Value("${indexing-settings.load-page.referrer}")
    private String referrer;
    @Value("${indexing-settings.search.frequency-popular-lemma}")
    private int frequencyPopularLemma;
    @Value("${indexing-settings.search.min-count-lemmas}")
    private int minCountLemmas;
    @Value("${indexing-settings.search.snippet-max-length}")
    private int snippetMaxLength;

    public List<Site> getSites() {
        return sites.getSites();
    }

    public void setSleepTimeMs(int sleepTimeMs) {
        this.sleepTimeMs = Math.max(sleepTimeMs, SLEEP_TIME_MS_MIN);
    }

    public void setCountPagesForUpdate(int countPagesForUpdate) {
        this.countPagesForUpdate = countPagesForUpdate <= 0 ? COUNT_PAGES_FOR_UPDATE_DEFAULT : countPagesForUpdate;
    }

    public void setSleepLoadPageMs(int sleepLoadPageMs) {
        this.sleepLoadPageMs = sleepLoadPageMs <= 0 ? SLEEP_LOAD_PAGE_MS_DEFAULT : sleepLoadPageMs;
    }

    public void setSnippetMaxLength(int snippetMaxLength) {
        this.snippetMaxLength = snippetMaxLength <= 0 ? MIN_COUNT_LEMMAS_DEFAULT : snippetMaxLength;
    }
}
