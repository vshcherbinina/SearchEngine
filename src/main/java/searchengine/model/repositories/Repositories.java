package searchengine.model.repositories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.Site;
import searchengine.config.ConfigList;
import searchengine.model.SiteEntity;
import searchengine.model.UrlStructure;
import java.util.List;

@Slf4j
public record Repositories(
        SiteRepository siteRepository,
        PageRepository pageRepository,
        IndexRepository indexRepository,
        LemmaRepository lemmaRepository,
        ConfigList configList
    ) {

    @Transactional
    public void deleteAll() {
        try {
            siteRepository.disableKeyChecks();
            indexRepository.truncate();
            lemmaRepository.truncate();
            pageRepository.truncate();
            siteRepository.truncate();
            siteRepository.enableKeyChecks();
        } catch (Exception e) {
            siteRepository.enableKeyChecks();
            throw new IllegalArgumentException(e);
        }
    }

    public SiteEntity getSiteEntity(UrlStructure urlStructure) {
        List<SiteEntity> existEntityList = siteRepository.findAll();
        for (SiteEntity site : existEntityList) {
            UrlStructure existUrlStructure = new UrlStructure(site.getUrl());
            if (!urlStructure.equalsDomainName(existUrlStructure)) {
                continue;
            }
            site.setUrlStructure(urlStructure);
            log.info("Cайт найден среди индексируемых ранее: " + site);
            return site;
        }
        return null;
    }

    public SiteEntity createOrGetExistSiteEntity(String url) {
        UrlStructure urlStructure = new UrlStructure(url);
        Site config = findSiteConfig(urlStructure);
        if (config == null) {
            return null;
        }
        SiteEntity site = getSiteEntity(urlStructure);
        if (site == null) {
            site = new SiteEntity(config.getName(), urlStructure.getUrl());
            log.info("Cайт не индексировался ранее, новая запись: " + site);
        }
        return site;
    }

    public Site findSiteConfig(UrlStructure urlStructure) {
        if (urlStructure.getDomainName().isBlank()) {
            return null;
        }
        for (Site siteConfig : configList.getSites()) {
            UrlStructure configUrlStructure = new UrlStructure(siteConfig.getUrl());
            if (urlStructure.equalsDomainName(configUrlStructure)) {
                return siteConfig;
            }
        }
        return null;
    }

    @Transactional
    public synchronized void updateFrequencyForSite(int siteId) {
        lemmaRepository.updateFrequencyForSite(siteId);
    }
}
