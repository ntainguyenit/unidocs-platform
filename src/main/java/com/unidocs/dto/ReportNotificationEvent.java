package com.unidocs.dto;

import com.unidocs.domain.DocumentReportType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReportNotificationEvent {
    private Long reportId;
    private Long documentId;
    private String documentTitle;
    private DocumentReportType reportType;
    private String priority; // "HIGH", "MEDIUM", "LOW"
    private long totalPendingReports;
}
