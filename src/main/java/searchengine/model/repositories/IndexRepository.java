package searchengine.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.IndexEntity;
import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<IndexEntity, Integer> {
    @Query(value = "select * from `index` where lemma_id = :lemmaId", nativeQuery = true)
    List<IndexEntity> findCustomByLemmaId(int lemmaId);

    @Query(value = "select * from `index` where lemma_id = :lemmaId and page_id = :pageId", nativeQuery = true)
    List<IndexEntity> findCustomByLemmaIdAndPageId(int lemmaId, int pageId);

    @Transactional
    @Modifying
    @Query(value = "delete from `index` where page_id = :pageId", nativeQuery = true)
    void deleteAllByPageId(int pageId);

    @Transactional
    @Modifying
    @Query(value = "truncate `index`", nativeQuery = true)
    void truncate();

}
