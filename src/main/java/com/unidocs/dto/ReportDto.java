package com.unidocs.dto;

import com.unidocs.domain.DocumentReportType;
import lombok.Data;

@Data
public class ReportDto {
    private Long documentId;
    private DocumentReportType reportType;
    private String message;
}
