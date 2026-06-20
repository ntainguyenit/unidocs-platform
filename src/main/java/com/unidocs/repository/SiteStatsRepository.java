package com.unidocs.repository;

import com.unidocs.domain.SiteStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteStatsRepository extends JpaRepository<SiteStats, Long> {
}
