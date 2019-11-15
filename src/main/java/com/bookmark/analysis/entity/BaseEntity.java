package com.bookmark.analysis.entity;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

import static com.bookmark.analysis.common.util.SysConstant.DATE_FORMAT10;
import static com.bookmark.analysis.common.util.SysConstant.DATE_FORMAT19;

/**
 * @author mjm
 * @createtime 2018/7/3 12:09
 */
@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity<ID extends Serializable> implements Serializable {

	/**
	 * "创建日期"属性名称
	 */
	public static final String CREATED_DATE_PROPERTY_NAME = "createdDate";
	/**
	 * "最后修改日期"属性名称
	 */
	public static final String LAST_MODIFIED_DATE_PROPERTY_NAME = "lastModifiedDate";

	@CreatedDate
	private Date createdDate;
	@Id
	private ID id;
	@LastModifiedDate
	private Date lastModifiedDate;

	public String getCreateDateStr() {
		String result = StringUtils.EMPTY;
		if (getCreatedDate() != null) {
			result = DateFormatUtils.format(getCreatedDate(), DATE_FORMAT10);
		}
		return result;
	}

	/**
	 * 页面状态判断使用
	 *
	 * @return
	 */
	@Transient
	public String getLastModifiedTimeStr() {
		String lastModifiedTimeStr = StringUtils.EMPTY;
		if (getLastModifiedDate() != null) {
			lastModifiedTimeStr = DateFormatUtils.format(getLastModifiedDate(), DATE_FORMAT19);
		}
		return lastModifiedTimeStr;
	}
}
