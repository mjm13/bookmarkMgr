package com.bookmark.analysis.dto;

import lombok.Data;

@Data
public class PageQueryDto {
    private Integer page = 0 ;
    private Integer limit = 10 ;
}
