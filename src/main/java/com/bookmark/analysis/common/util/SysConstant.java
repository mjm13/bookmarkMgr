package com.bookmark.analysis.common.util;

import javax.persistence.Converter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author mjm
 * @createtime 2018/7/3 12:09
 */
public class SysConstant {
	public final static int INT_ZERO = 0;
	public final static int INT_ONE = 1;
	public final static int INT_TEN = 10;
	public final static int INT_19 = 19;
	public final static int INT_TWO = 2;
	public final static int INT_FOUR = 4;
	public final static int INT_SIX = 6;
	public final static int INT_24 = 24;
	public final static int INT_16 = 16;

	public final static int MONEY_DECIMAL = 2;
	public final static int PRICE_DECIMAL = 6;
	public final static int WEIGHT_DECIMAL = 3;
	public final static int NUMBER_DECIMAL = 2;
	public final static int VALUATION_DECIMAL = 2;

	public final static String STR_ZERO = "0";
	public final static String STR_ONE = "1";
	public final static String STR_TWO = "2";

	public final static BigDecimal BD100 = new BigDecimal(100);
	public final static BigDecimal BD1024 = new BigDecimal(1024);

	public final static long LONG_ZERO = 0L;
	public final static long LONG_THOUSAND = 1000L;
	public final static long LONG_1024 = 1024L;

	public final static String UNKNOWN_STR = "unknown";
	public final static String ID_STR = "id";
	public final static String SMS_SUCCESS = "2";

	public final static String EN_BACKSLASH = "/";
	public final static String EN_DOT = ",";

	public final static String DATE_FORMAT6 = "yyyyMM";
	public final static String DATE_FORMAT7 = "yyyy-MM";
	public final static String DATE_FORMAT19 = "yyyy-MM-dd HH:mm:ss";
	public final static String DATE_FORMAT16 = "yyyy-MM-dd HH:mm";
	public final static String DATE_FORMAT10 = "yyyy-MM-dd";
	public final static String DATE_FORMAT15 = "yyyyMMddHHmmss";

	public final static String INQUIRY = "INQUIRY";
	public final static String ORDER = "ORDER";

	public final static String MATERIAL = "MATERIAL";
	public final static String OPERATION = "OPERATION";

	public final static String CH_EMPTY = "空";
	public final static String CH_ALL = "所有";

	public final static String MONEY_HIDE = "***";

	public final static String TRUE = Boolean.TRUE.toString();
	public final static String FALSE = Boolean.FALSE.toString();

	/**
	 * 类型转换 - List属性
	 *
	 * @author bangmuju Team
	 * @version 5.0
	 */
	@Converter
	public static class ListConverter extends BaseAttributeConverter<List<String>> {
	}
	/**
	 * 类型转换 - Map属性
	 *
	 * @author bangmuju Team
	 * @version 5.0
	 */
	@Converter
	public static class MapConverter extends BaseAttributeConverter<Map<String, String>> {
	}
}
