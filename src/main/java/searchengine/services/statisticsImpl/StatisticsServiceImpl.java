package searchengine.services.statisticsImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteEntity;
import searchengine.model.repositories.LemmaRepository;
import searchengine.model.repositories.PageRepository;
import searchengine.model.repositories.SiteRepository;
import searchengine.services.StatisticsService;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;

    private StatisticsResponse statisticsResponse;

    @Override
    public ResponseEntity<StatisticsResponse> getStatistics() {
        try {
            statisticsResponse = new StatisticsResponse();
            List<DetailedStatisticsItem> details = new ArrayList<>();
            TotalStatistics total = new TotalStatistics();
            List<SiteEntity> sites = siteRepository.findAll();
            sites.forEach(site -> {
                DetailedStatisticsItem detail = toDetail(site);
                details.add(detail);
                total.addSites(1);
                total.addPages(detail.getPages());
                total.addLemmas(detail.getLemmas());
            });
            statisticsResponse.getStatistics().setDetailed(details);
            statisticsResponse.getStatistics().setTotal(total);
            return statisticsResponse.toSuccessResponse();
        } catch (Exception e) {
            return statisticsResponse.toErrorResponse(e, "Ошибка получения статистики");
        }
    }

    private DetailedStatisticsItem toDetail(SiteEntity site) {
        int pages = pageRepository.countBySiteId(site.getId());
        int lemmas = lemmaRepository.countBySiteId(site.getId());
        DetailedStatisticsItem detail = new DetailedStatisticsItem();
        detail.setUrl(site.getUrl());
        detail.setName(site.getName());
        detail.setStatus(site.getStatus().toString());
        detail.setStatusTime(site.getStatusTime().atZone(ZoneId.systemDefault()).toEpochSecond() * 1_000);
        detail.setError(site.getLastError());
        detail.setPages(pages);
        detail.setLemmas(lemmas);
        return detail;
    }

}
