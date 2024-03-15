package com.bookmark.analysis.dto;

import lombok.Data;

@Data
public class WebsiteQueryDto extends PageQueryDto{
    private String keyword;
    private String remark;
    private String title;
    private String url;
    private String description;
    private String domain;
    private String keywords;
    private String loadResult;
}
