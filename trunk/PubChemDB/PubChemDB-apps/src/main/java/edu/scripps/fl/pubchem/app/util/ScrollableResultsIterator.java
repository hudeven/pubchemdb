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

import java.util.Iterator;

import org.apache.commons.beanutils.ConvertUtils;
import org.hibernate.ScrollableResults;

public final class ScrollableResultsIterator<E> implements Iterator<E> {

	private final Class<E> clazz;
	private final ScrollableResults results;

	public ScrollableResultsIterator(Class<E> clazz, ScrollableResults results) {
		this.clazz = clazz;
		this.results = results;
	}

	public boolean hasNext() {
		boolean hasNext = results.next();
		return hasNext;
	}

	public E next() {
		Object[] obj = results.get();
		Object obj2 = ConvertUtils.convert(obj[0], clazz);
		return clazz.cast(obj2);
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}