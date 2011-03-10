/*
 * Copyright 2010 The Scripps Research Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.scripps.fl.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.list.GrowthList;
import org.apache.commons.lang.StringUtils;

public class DoubleListStringType extends ListStringType<Double> {

	public List<Double> getListFromString(String list) {
		String strs[] = list.split("\r?\n");
		List<Double> ids = newList(strs.length);
		for (int ii = 0; ii < strs.length; ii++) {
			if (null != strs[ii] && !strs[ii].equals("")) {
				Double dbl = Double.parseDouble(strs[ii]);
				ids.set(ii, dbl);
			}
		}
		return ids;
	}

	public String getStringFromList(List<Double> ids) {
		if (null == ids)
			return null;
		return StringUtils.join(ids, "\n");
	}

	public List<Double> newList(int initialCapacity) {
		return (List<Double>) GrowthList.decorate(new ArrayList<Double>(initialCapacity));
	}
}