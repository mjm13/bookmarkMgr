package com.bookmark.analysis.entity;

import lombok.Data;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author mjm
 * @createtime 2019/11/8-9:35
 **/
@Indexed(index = "website")
@Entity
@Data
//@Analyzer(impl = HanLPAnalyzer.class)
@Table(name = "website")
public class Website extends BaseEntity<String> {
	@Field
	private String description;
	@Field
	private String keywords;
	@Field
	private String remark;
	@Field
	private String title;
	@Field
	private String url;

	private String domain;

	private String domainTitle;

	private String icon;

	private Date pageDate;
}
