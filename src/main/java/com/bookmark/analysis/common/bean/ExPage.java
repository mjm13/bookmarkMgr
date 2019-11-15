package com.bookmark.analysis.common.bean;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author mjm
 * @createtime 2018/7/3 12:09
 */
@Data
public class ExPage {
	/**
	 * 页码
	 */
	private int page;
	/**
	 * 每页多少条
	 */
	private int limit;
	private String keyword;

	public String getKeyword(){
		String result = StringUtils.EMPTY;
		if(StringUtils.isNotBlank(keyword)){
			result = keyword.trim();
		}
		return result;
	}
}
