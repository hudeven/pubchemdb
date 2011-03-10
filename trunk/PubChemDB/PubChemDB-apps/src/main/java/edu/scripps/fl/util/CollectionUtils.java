package edu.scripps.fl.util;

import java.util.HashMap;
import java.util.Map;

public class CollectionUtils {

	public static Map createMap(Object... keyValuePairs) {
		Map map = new HashMap();
		for (int ii = 0; ii < keyValuePairs.length; ii += 2)
			map.put(keyValuePairs[ii], keyValuePairs[ii + 1]);
		return map;
	}
}
