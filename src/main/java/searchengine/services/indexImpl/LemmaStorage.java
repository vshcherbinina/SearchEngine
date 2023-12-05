package searchengine.services.indexImpl;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.repositories.LemmaRepository;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Getter
public class LemmaStorage implements AutoCloseable{
    private final SiteLoader siteLoader;
    private final HashMap<String, LemmaEntity> lemmas;
    private final LemmaRepository lemmaRepository;
    @Setter
    private boolean isLemmasLoadFromDB;
    public LemmaStorage(SiteLoader siteLoader) {
        this.siteLoader = siteLoader;
        this.lemmas = new HashMap<>();
        lemmaRepository = siteLoader.getRepositories().lemmaRepository();
        isLemmasLoadFromDB = false;
    }

    public synchronized void saveLemma(LemmaEntity lemma) throws IllegalArgumentException  {
        try {
            LemmaEntity newLemma = lemmaRepository.save(lemma);
            lemma.setId(newLemma.getId());
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка при сохранении леммы '" + lemma.getLemma() + "': " + e.getMessage());
        }
    }

    public synchronized LemmaEntity createAndSaveLemma(String text) {
        LemmaEntity lemma = new LemmaEntity();
        lemma.setSite(siteLoader.getSite());
        lemma.setLemma(text);
        lemma.setFrequency(1);
        saveLemma(lemma);
        lemmas.put(text, lemma);
        return lemma;
    }

    public synchronized LemmaEntity getLemmaIfExist(String word) {
        if (lemmas.containsKey(word)) {
            return lemmas.get(word);
        }
        if (isLemmasLoadFromDB) {
            List<LemmaEntity> lemmaList = lemmaRepository.findAllByLemmaAndSiteId(word, siteLoader.getSite().getId());
            log.info("Поиск леммы \"" + word + "\" в базе данных, найдено: " + lemmaList.size());
            for (LemmaEntity lemma : lemmaList) {
                if (!word.equals(lemma.getLemma())) {
                    continue;
                }
                lemma.setSite(siteLoader.getSite());
                lemmas.put(word, lemma);
                return lemma;
            }
        }
        return null;
    }

    public void loadLemmasBeforeDeletePage(PageEntity page) {
        List<LemmaEntity> list = lemmaRepository.findAllByPageId(page.getId());
        list.forEach(lemma -> {
            lemma.setSite(page.getSite());
            lemmas.put(lemma.getLemma(), lemma);
        });
        log.info("Загружено из базы данных лемм, найденных на странице ранее (количество): " + lemmas.size());
    }

    public void updateLemmas(){
        if (lemmas.isEmpty()) {
            return;
        }
        log.info("Обновление количества лемм для сайта " + siteLoader.getSite());
        siteLoader.getRepositories().updateFrequencyForSite(siteLoader.getSite().getId());
        lemmas.clear();
    }

    @Override
    public void close() {
        lemmas.clear();
    }
}
