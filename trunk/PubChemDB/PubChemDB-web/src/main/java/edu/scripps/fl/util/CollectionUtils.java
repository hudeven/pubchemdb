package edu.scripps.fl.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

public class CollectionUtils {
	
	public static Map toMap(String property, Iterable values) {
		return toMap(new HashMap(), property, values);
	}
	
	public static Map toMap(Map map, String property, Iterable values) {
		try{
			for (Object obj : values) {
				Object key = BeanUtils.getProperty(obj, property);
				map.put(key, obj);
			}
		}
		finally {
			return map;
		}
	}
}
