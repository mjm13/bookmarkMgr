package com.bookmark.analysis.common.util;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Utils - JSON
 *
 * @author bangmuju Team
 * @version 5.0
 */
public final class JsonUtils {

	/**
	 * ObjectMapper
	 */
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	/**
	 * 不可实例化
	 */
	private JsonUtils() {
	}

	/**
	 * 将对象转换为JSON字符串
	 *
	 * @param value 对象
	 * @return JSON字符串
	 */
	public static String toJson(Object value) {

		try {
			return OBJECT_MAPPER.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 将对象转换为JSON对象
	 *
	 * @param value 对象
	 * @return JSON字符串
	 */
	public static JsonNode toJsonNode(Object value) {

		try {
			return OBJECT_MAPPER.convertValue(value, JsonNode.class);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static <T> T toObject(JsonNode json, Class<T> valueType) {
		if(json == null || json.isNull()){
			return null;
		}
		return OBJECT_MAPPER.convertValue(json, valueType);
	}


	/**
	 * 将JSON字符串转换为对象
	 *
	 * @param json      JSON字符串
	 * @param valueType 类型
	 * @return 对象
	 */
	public static <T> T toObject(String json, Class<T> valueType) {

		try {
			return OBJECT_MAPPER.readValue(json, valueType);
		} catch (JsonParseException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (JsonMappingException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 将字符串转list对象
	 *
	 * @param <T>
	 * @param jsonStr
	 * @param cls
	 * @return
	 */
	public static <T> List<T> str2list(String jsonStr, Class<T> cls) {
		ObjectMapper mapper = new ObjectMapper();
		List<T> objList = null;
		try {
			JavaType t = mapper.getTypeFactory().constructParametricType(
					List.class, cls);
			objList = mapper.readValue(jsonStr, t);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return objList;
	}


	/**
	 * 将JSON字符串转换为对象
	 *
	 * @param json          JSON字符串
	 * @param typeReference 类型
	 * @return 对象
	 */
	public static <T> T toObject(String json, TypeReference<?> typeReference) {

		try {
			return (T) OBJECT_MAPPER.readValue(json, typeReference);
		} catch (JsonParseException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (JsonMappingException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 将JSON字符串转换为对象
	 *
	 * @param json     JSON字符串
	 * @param javaType 类型
	 * @return 对象
	 */
	public static <T> T toObject(String json, JavaType javaType) {

		try {
			return OBJECT_MAPPER.readValue(json, javaType);
		} catch (JsonParseException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (JsonMappingException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 将JSON字符串转换为树
	 *
	 * @param json JSON字符串
	 * @return 树
	 */
	public static JsonNode toTree(String json) {
		try {
			return OBJECT_MAPPER.readTree(json);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 将对象转换为JSON流
	 *
	 * @param writer Writer
	 * @param value  对象
	 */
	public static void writeValue(Writer writer, Object value) {

		try {
			OBJECT_MAPPER.writeValue(writer, value);
		} catch (JsonGenerationException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (JsonMappingException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 构造类型
	 *
	 * @param type 类型
	 * @return 类型
	 */
	public static JavaType constructType(Type type) {

		return TypeFactory.defaultInstance().constructType(type);
	}

	/**
	 * 构造类型
	 *
	 * @param typeReference 类型
	 * @return 类型
	 */
	public static JavaType constructType(TypeReference<?> typeReference) {
		return TypeFactory.defaultInstance().constructType(typeReference);
	}

}