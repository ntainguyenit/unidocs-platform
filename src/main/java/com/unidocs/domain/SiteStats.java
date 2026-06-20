package com.unidocs.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "site_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteStats {
    @Id
    private Long id;
    
    private Long totalVisitors;
}
