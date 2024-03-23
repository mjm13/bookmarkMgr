package com.bookmark.analysis.dto;

import lombok.Data;

@Data
public class WebsiteQueryDto extends PageQueryDto {
    private String keyword;
    private String remark;
    private String title;
    private String url;
    private String description;
    private String domain;
    private String keywords;
    private String loadResult;
    private String host = "127.0.0.1";
    private Integer proxy = 49776;
}
