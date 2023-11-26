package searchengine.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.SiteEntity;

@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {
    @Transactional
    @Modifying
    @Query(value = "truncate site", nativeQuery = true)
    void truncate();

    @Transactional
    @Modifying
    @Query(value = "set FOREIGN_KEY_CHECKS = 0", nativeQuery = true)
    void disableKeyChecks();

    @Transactional
    @Modifying
    @Query(value = "set FOREIGN_KEY_CHECKS = 1", nativeQuery = true)
    void enableKeyChecks();

}
