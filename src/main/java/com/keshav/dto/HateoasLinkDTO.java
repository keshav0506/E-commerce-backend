package com.keshav.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HateoasLinkDTO {
    private String rel;
    private String href;
    private String method;
    private String title;
    private String description;
}
