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
package edu.scripps.fl.pipeline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.commons.collections.iterators.SingletonListIterator;
import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.stage.BaseStage;

/*
 * @author Mark Southern (southern at scripps dot edu)
 */
public class GroupStage extends BaseStage {

	private int groupSize = 5000;

	public int getGroupSize() {
		return groupSize;
	}

	public void setGroupSize(int groupSize) {
		this.groupSize = groupSize;
	}

	@Override
	public void process(Object obj) throws StageException {
		Iterator<Object> iterator = null;
		if (obj instanceof Iterator) // iterator itself.
			iterator = (Iterator<Object>) obj;
		else if (obj instanceof Iterable) // collections etc.
			iterator = ((Iterable<Object>) obj).iterator();
		else if (obj.getClass().isArray()) // arrays
			iterator = new ArrayIterator(obj);
		else
			iterator = new SingletonListIterator(obj);

		List<Object> list = new ArrayList<Object>(getGroupSize());
		while (true) {
			if (!iterator.hasNext())
				break;
			if (list.size() < getGroupSize())
				list.add(iterator.next());
			else {
				emit(list);
				list = new ArrayList<Object>(getGroupSize());
			}
		}

		if (list.size() > 0)
			emit(list);
	}
}