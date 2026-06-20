package com.unidocs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityDto {
    private Long id;
    private String name;
    private String shortName;
    private String description;
    private String logoUrl;
    private String color;
}
