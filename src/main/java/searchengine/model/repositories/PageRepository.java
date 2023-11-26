package searchengine.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.PageEntity;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer> {
    List<PageEntity> findAllByPathAndSiteId(String path, int siteId);

    int countBySiteId(int siteId);

    @Transactional
    @Modifying
    @Query(value = "truncate page;", nativeQuery = true)
    void truncate();

}