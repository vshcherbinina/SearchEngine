package searchengine.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.LemmaEntity;
import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Integer> {

    List<LemmaEntity> findAllByLemmaAndSiteId(String lemma, int siteId);

    int countBySiteId(int siteId);

    @Query(value = "select * from lemma l inner join `index` t on l.id = t.lemma_id where t.page_id = :pageId", nativeQuery = true)
    List<LemmaEntity> findAllByPageId(int pageId);

    @Transactional
    @Modifying
    @Query(value = "update lemma m set frequency = (select count(*) from `index` n where n.lemma_id = m.id) where m.site_id = :siteId", nativeQuery = true)
    void updateFrequencyForSite(int siteId);

    @Transactional
    @Modifying
    @Query(value = "truncate lemma", nativeQuery = true)
    void truncate();

}
