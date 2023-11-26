package searchengine.services.indexImpl;

import lombok.extern.slf4j.Slf4j;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.repositories.IndexRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

@Slf4j
public class IndexStorage implements AutoCloseable{
    private final PageEntity page;
    private final SiteLoader siteLoader;
    private final HashSet<IndexEntity> indexes;
    private final IndexRepository indexRepository;

    public IndexStorage(PageEntity page, SiteLoader siteLoader) {
        this.page = page;
        this.siteLoader = siteLoader;
        this.indexes = new HashSet<>();
        indexRepository = siteLoader.getRepositories().indexRepository();
    }

    private LemmaEntity findLemma(String text) {
        return siteLoader.createLemmaIfNotExist(text);
    }

    private void saveLemmaAndStorageIndex(String text, int rank) {
        IndexEntity index = new IndexEntity();
        index.setPage(page);
        index.setRank(rank);
        LemmaEntity lemma = findLemma(text);
        index.setLemma(lemma);
        indexes.add(index);
    }

    public void findLemmas() throws IOException {
        if (!page.isSuccessLoad()) {
            return;
        }
        MorphologyUtils morphologyUtils = MorphologyUtils.getInstance();
        HashMap<String, Integer> lemmas = morphologyUtils.collectLemmas(page.getContent());
        lemmas.keySet().forEach(lemma ->
            saveLemmaAndStorageIndex(lemma, lemmas.get(lemma))
        );
        try {
            indexRepository.saveAll(indexes);
            indexes.clear();
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка при сохранении индексов: " + e);
        }
    }

    @Override
    public void close() {
        indexes.clear();
    }
}
