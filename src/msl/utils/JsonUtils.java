package msl.utils;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;

/**
 * 当object的属性为null时，不封装
 * @author 61423
 *
 */
public class JsonUtils {

	/**
	 * 默认封装，去掉禁止反序列化的
	 * @param auth
	 * @return
	 */
	public static String toJSONString(Object auth) {
		return JSON.toJSONString(auth);
	}
	
	/**
	 * 指定属性封装，传入对应的属性
	 * @param auth
	 * @param properties
	 * @return
	 */
	public static String toJSONString(Object auth, String... properties) {
		return JSON.toJSONString(auth,new SimplePropertyPreFilter(properties));
	}
	
	/**
	 * 根据json字符串，封装指定的实体类
	 * B嵌套在A里，那么我们要声明内嵌类的static属性
	 * @param <T>
	 * @param jsonSring
	 * @param clazz
	 * @return
	 */
	public static <T> T parseObject(String jsonSring,Class<T> clazz) {
		return JSON.parseObject(jsonSring, clazz);
	}

}
