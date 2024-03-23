package com.bookmark.analysis.entity;

import lombok.Data;

import javax.persistence.Entity;
import java.util.Date;
import java.util.List;

/**
 * @author mjm
 * @createtime 2019/11/8-9:35
 **/
@Entity
@Data
public class WebsiteGroup extends BaseEntity<Long> {
	private String name;
	private String guid;
	private String date_added;
	private String date_last_used;
	private String date_modified;
	private List children;
}
