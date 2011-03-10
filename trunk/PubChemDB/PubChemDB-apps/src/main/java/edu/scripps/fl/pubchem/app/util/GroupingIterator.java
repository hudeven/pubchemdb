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
package edu.scripps.fl.pubchem.app.util;

import java.lang.reflect.Array;
import java.util.Iterator;

import org.apache.commons.beanutils.ConvertUtils;

public class GroupingIterator implements Iterator {
	private Iterator iterator;
	private int groupSize = 3;
	private Class clazz;
	private Object array;

	public GroupingIterator(Iterator iterator, Class clazz, int groupSize) {
		this.iterator = iterator;
		this.groupSize = groupSize;
		this.clazz = clazz;
	}

	public boolean hasNext() {
		array = Array.newInstance(clazz, groupSize);
		int ii = 0;
		for (ii = 0; ii < groupSize; ii++)
			if (iterator.hasNext()) {
				Object obj = iterator.next();
				obj = ConvertUtils.convert(obj, clazz);
				Array.set(array, ii, obj);
			} else
				break;
		if (ii < groupSize) {
			Object dest = Array.newInstance(clazz, ii);
			System.arraycopy(array, 0, dest, 0, ii);
			array = dest;
		}
		return ii > 0;
	}

	public Object next() {
		return array;
	}

	public void remove() {
	}
}