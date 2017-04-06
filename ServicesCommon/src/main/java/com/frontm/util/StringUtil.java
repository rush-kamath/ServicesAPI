package com.frontm.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringUtil {
	private static final String URL_QUERY_PARAM_DELIM = "==";
	private static final String URL_QUERY_STRING_DELIM = "&&";

	public static boolean isEmpty(String string) {
		return string == null || string.trim().isEmpty();
	}
	
	public static boolean isEmpty(List<? extends Object> list) {
		return list == null || list.isEmpty();
	}
	
	public static boolean isNotEmpty(String string) {
		return !isEmpty(string);
	}
	
	public static boolean isNotEmpty(List<? extends Object> list) {
		return !isEmpty(list);
	}
	
	public static Map<String, String> processQueryString(String queryString) {
		final Map<String, String> queryMap = new HashMap<>();
		if(isEmpty(queryString)) {
			return queryMap;
		}

		final String[] queryStrings = queryString.split(URL_QUERY_STRING_DELIM);
		for (String qryStr : queryStrings) {
			final String[] split = qryStr.split(URL_QUERY_PARAM_DELIM);
			if (split.length < 2) {
				continue;
			}
			queryMap.put(split[0].trim(), split[1].trim());
		}
		return queryMap;
	}
}
