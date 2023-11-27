package searchengine.services.indexImpl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.ConfigList;
import searchengine.config.SiteList;
import searchengine.dto.GeneralResponse;
import searchengine.model.SiteEntity;
import searchengine.model.repositories.*;
import searchengine.services.IndexService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Getter
@Slf4j
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {
    @Autowired
    private final SiteRepository siteRepository;
    @Autowired
    private final PageRepository pageRepository;
    @Autowired
    private final IndexRepository indexRepository;
    @Autowired
    private final LemmaRepository lemmaRepository;
    @Autowired
    private final ConfigList configList;
    private final HashSet<Thread> tasks;
    private ThreadPoolExecutor executorService;

    @Override
    public ResponseEntity<GeneralResponse> startIndexing() {
        log.info("### Запуск полной индексации сайтов ###");
        if (isExecutorServiceActive()) {
            return GeneralResponse.newErrorResponse(HttpStatus.BAD_REQUEST, "Индексация уже запущена");
        }
        try {
            Repositories repositories = getInstanceRepositories();
            repositories.deleteAll();
            for (Site siteConfig : configList.getSites()) {
                SiteEntity site = new SiteEntity(siteConfig.getName(), siteConfig.getUrl());
                tasks.add(new SiteExecutor(site, repositories));
            }
            startExecutorService();
        } catch (Exception e) {
            return GeneralResponse.newErrorResponse(e, "Ошибка индексации сайтов");
        }
        return GeneralResponse.newSuccessResponse();
    }

    @Override
    public ResponseEntity<GeneralResponse> indexPage(String url) {
        log.info("### Запуск индексации страницы: " + url + " ###");
        if (isExecutorServiceActive()) {
            return GeneralResponse.newErrorResponse(HttpStatus.BAD_REQUEST, "Индексация уже запущена");
        }
        try {
            Repositories repositories = getInstanceRepositories();
            SiteEntity site = repositories.createOrGetExistSiteEntity(url);
            if (site == null) {
                return GeneralResponse.newErrorResponse(HttpStatus.BAD_REQUEST,
                        "Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
            }
            SiteLoader siteLoader = new SiteLoader(site, repositories);
            siteLoader.loadPage();
            if (!siteLoader.getPageLoader().getPage().isSuccessLoad()) {
                HttpStatus httpStatus = HttpStatus.valueOf(siteLoader.getPageLoader().getPage().getCode());
                return GeneralResponse.newErrorResponse(httpStatus, "Ошибка загрузки страницы: " + httpStatus.getReasonPhrase());
            }
            tasks.add(siteLoader);
            startExecutorService();
        } catch (Exception e) {
            return GeneralResponse.newErrorResponse(e, "Ошибка индексации страницы");
        }
        return GeneralResponse.newSuccessResponse();
    }

    @Override
    public ResponseEntity<GeneralResponse> stopIndexing() {
        if (!isExecutorServiceActive()) {
            return GeneralResponse.newErrorResponse(HttpStatus.BAD_REQUEST, "Индексация не запущена");
        }
        log.info("Остановка текущей индексации...");
        tasks.forEach(Thread::interrupt);
        return GeneralResponse.newSuccessResponse();
    }

    private boolean isExecutorServiceActive() {
        log.info("Проверка состояния текущих процессов индексации:");
        if (executorService == null) {
            log.info("\tпроцессы не запущены");
            return false;
        }
        if (executorService.getActiveCount() == 0) {
            log.info("\tнет активных процессов");
            return false;
        }
        log.info("\tколичество активных процессов = " + executorService.getActiveCount());
        return true;
    }

    private void controlExecutorService() {
        while (true) {
            if (!isExecutorServiceActive()) {
                break;
            }
            try {
                Thread.sleep(configList.getSleepTimeMs());
            } catch (Exception e) {
                log.error("Ошибка процедуры контроля текущих процессов индексации:\n" + e.getMessage());
                break;
            }
        }
        stopExecutorService();
        log.info("### Индексация завершена ###");
    }

    public void stopExecutorService() {
        executorService.shutdown();
        tasks.clear();
    }

    public void startExecutorService() throws IllegalArgumentException {
        if (isExecutorServiceActive()) {
            throw new IllegalArgumentException("Индексация уже запущена");
        }
        new Thread(this::controlExecutorService).start();
        executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(Math.min(tasks.size(), Runtime.getRuntime().availableProcessors()-1));
        for (Thread task : tasks) {
            executorService.submit(task);
        }
    }

    private Repositories getInstanceRepositories() {
        return new Repositories(siteRepository, pageRepository, indexRepository, lemmaRepository, configList);
    }

}
