package com.bookmark.analysis.common.util;

import com.bookmark.analysis.entity.BaseEntity;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mjm
 * @createtime 2018/7/3 12:09
 */
public class ExBeanUtil {
	private static final String[] UPDATE_IGNORE_PROPERTIES = new String[]{BaseEntity.CREATED_DATE_PROPERTY_NAME, BaseEntity.LAST_MODIFIED_DATE_PROPERTY_NAME};

	/**
	 * 将页面上传入的值覆盖到数据库查询的值当中，并忽略其中的复杂数据类型
	 *
	 * @param source           页面传入的实体
	 * @param target           数据库查询出的实体
	 * @param hasNull			是否包含null
	 * @param ignoreProperties 忽略的属性
	 * @param <T>              实体泛型
	 * @return 合并后的结果
	 */
	public static <T> T mergeBean(T source, T target, Boolean hasNull, String... ignoreProperties) {
		String[] ignoreProps = UPDATE_IGNORE_PROPERTIES;

		if (ignoreProperties != null) {
			ignoreProps = ArrayUtils.addAll(ignoreProps, ignoreProperties);
		}
		PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(source);
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			String propertyName = propertyDescriptor.getName();
			Method readMethod = propertyDescriptor.getReadMethod();
			Method writeMethod = propertyDescriptor.getWriteMethod();
			if (ArrayUtils.contains(ignoreProps, propertyName) || readMethod == null || writeMethod == null) {
				continue;
			}
			try {
				Object sourceValue = readMethod.invoke(source);
				Object targetValue = readMethod.invoke(target);
				if(sourceValue == null && hasNull && targetValue != null && (targetValue instanceof String || targetValue instanceof Number || targetValue instanceof Date || targetValue instanceof Enum || targetValue instanceof Boolean)){
					writeMethod.invoke(target, sourceValue);
				}else if (sourceValue instanceof String || sourceValue instanceof Number || sourceValue instanceof Date || sourceValue instanceof Enum || sourceValue instanceof Boolean) {
					writeMethod.invoke(target, sourceValue);
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e.getMessage(), e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e.getMessage(), e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return target;
	}

	/**
	 * 将一个实体指定字段赋值到另一个实体中
	 *
	 * @param source            原实体
	 * @param target            需要赋值的实体
	 * @param containProperties 需要赋值的属性
	 * @param <T>               实体泛型
	 * @return 赋值后的实体
	 */
	public static <T> T copyBean(T source, T target, String... containProperties) {
		PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(source);
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			String propertyName = propertyDescriptor.getName();
			Method readMethod = propertyDescriptor.getReadMethod();
			Method writeMethod = propertyDescriptor.getWriteMethod();
			if (!ArrayUtils.contains(containProperties, propertyName) || readMethod == null || writeMethod == null) {
				continue;
			}
			try {
				Object sourceValue = readMethod.invoke(source);
				writeMethod.invoke(target, sourceValue);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e.getMessage(), e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e.getMessage(), e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return target;
	}

	/**
	 * 将页面上传入的值覆盖到实体当中
	 *
	 * @param source 页面传入的键值对
	 * @param target 实体对象
	 * @param <T>    实体泛型
	 * @return 合并后的结果
	 */
	public static <T> T mergeBeanByMap(Map<String, Object> source, T target) {
		String[] ignoreProps = UPDATE_IGNORE_PROPERTIES;

		PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(target);
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			String propertyName = propertyDescriptor.getName();
			Method readMethod = propertyDescriptor.getReadMethod();
			Method writeMethod = propertyDescriptor.getWriteMethod();
			if (ArrayUtils.contains(ignoreProps, propertyName) || readMethod == null || writeMethod == null || source.get(propertyName) == null) {
				continue;
			}
			try {
				Class targetType = propertyDescriptor.getPropertyType();
				Object object = source.get(propertyName);
				if(object == null){

				}else if (targetType.equals(String.class)) {
					writeMethod.invoke(target, source.get(propertyName));
				} else if (targetType.equals(Integer.class)) {
					writeMethod.invoke(target, Integer.parseInt(source.get(propertyName).toString()));
				} else if (targetType.equals(BigDecimal.class)) {
					writeMethod.invoke(target, new BigDecimal(source.get(propertyName).toString()));
				} else if (targetType.equals(Date.class)) {
					writeMethod.invoke(target, DateUtils.parseDate(source.get(propertyName).toString(), "yyyy-MM-dd HH:mm:ss"));
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e.getMessage(), e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e.getMessage(), e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e.getMessage(), e);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return target;
	}

	/**
	 * 将页面上传入的值覆盖到数据库查询的值当中，并忽略其中的复杂数据类型
	 *
	 * @param source 页面传入的实体
	 * @param target 数据库查询出的实体
	 * @param <T>    实体泛型
	 * @return 合并后的结果
	 */
	public static <T> T mergeBean(T source, T target) {
		return mergeBean(source, target, false, new String[0]);
	}


	/**
	 * JavaBean转换为Map
	 * 只转换基础数据类型用于页面展示
	 *
	 * @param bean 目标实体
	 * @return 合并后的结果
	 */
	public static Map<String, String> beanToMap(Object bean) {
		Map<String, String> map = new HashMap<>(15);
		PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(bean);
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			String propertyName = propertyDescriptor.getName();
			Method readMethod = propertyDescriptor.getReadMethod();
			if (ArrayUtils.contains(UPDATE_IGNORE_PROPERTIES, propertyName) || readMethod == null) {
				continue;
			}
			try {
				Object sourceValue = readMethod.invoke(bean);
				if (sourceValue instanceof String || sourceValue instanceof Number || sourceValue instanceof Date) {
					map.put(propertyName, String.valueOf(sourceValue));
				}
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return map;
	}

}
