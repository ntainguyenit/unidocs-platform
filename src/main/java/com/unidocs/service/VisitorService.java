package com.unidocs.service;

import com.unidocs.domain.SiteStats;
import com.unidocs.repository.SiteStatsRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class VisitorService {

    private final SiteStatsRepository siteStatsRepository;

    // Track session ID -> Last ping time (ms)
    private final Map<String, Long> activeSessions = new ConcurrentHashMap<>();
    
    // In-memory counter for fast access
    private final AtomicLong totalVisitors = new AtomicLong(0);
    
    // Keep track of the last saved value to avoid unnecessary DB writes
    private long lastSavedTotalVisitors = 0;

    // Heartbeat timeout (30 seconds)
    private static final long TIMEOUT_MS = 30000;

    public VisitorService(SiteStatsRepository siteStatsRepository) {
        this.siteStatsRepository = siteStatsRepository;
    }

    @PostConstruct
    public void init() {
        SiteStats stats = siteStatsRepository.findById(1L).orElse(null);
        if (stats == null) {
            stats = new SiteStats(1L, 1000L); // Start with an initial number or 0
            siteStatsRepository.save(stats);
        }
        totalVisitors.set(stats.getTotalVisitors());
        lastSavedTotalVisitors = stats.getTotalVisitors();
    }

    public void ping(String sessionId, boolean newVisitor) {
        long now = System.currentTimeMillis();
        activeSessions.put(sessionId, now);
        
        if (newVisitor) {
            totalVisitors.incrementAndGet();
        }
    }

    public long getOnlineCount() {
        return activeSessions.size();
    }

    public long getTotalVisitors() {
        return totalVisitors.get();
    }

    // Run every 10 seconds to clean up inactive sessions and optionally save stats
    @Scheduled(fixedRate = 10000)
    public void cleanupAndSave() {
        long now = System.currentTimeMillis();
        
        // Remove sessions that haven't pinged in 30 seconds
        activeSessions.entrySet().removeIf(entry -> (now - entry.getValue()) > TIMEOUT_MS);
        
        // Save total visitors to DB if changed
        long currentTotal = totalVisitors.get();
        if (currentTotal > lastSavedTotalVisitors) {
            SiteStats stats = siteStatsRepository.findById(1L).orElse(new SiteStats(1L, currentTotal));
            stats.setTotalVisitors(currentTotal);
            siteStatsRepository.save(stats);
            lastSavedTotalVisitors = currentTotal;
        }
    }
}
