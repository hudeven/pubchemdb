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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.list.GrowthList;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class StringListStringType extends ListStringType<String> {
	public List<String> getListFromString(String str) {
		List<String> list = newList(15);
		try {
			CsvReader reader = new CsvReader(new StringReader(str), ',');
			if (reader.readRecord()) {
				list = newList(reader.getColumnCount());
				for (String value : reader.getValues())
					list.add(value);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return list;
	}

	public String getStringFromList(List<String> ids) {
		if (null == ids)
			return null;
		StringWriter sw = new StringWriter();
		try {
			CsvWriter writer = new CsvWriter(sw, ',');
			for (String value : ids)
				writer.write(value);
			writer.flush();
			writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return sw.getBuffer().toString();
	}

	public List<String> newList(int initialCapacity) {
		return (List<String>) GrowthList.decorate(new ArrayList<String>(initialCapacity));
	}
}