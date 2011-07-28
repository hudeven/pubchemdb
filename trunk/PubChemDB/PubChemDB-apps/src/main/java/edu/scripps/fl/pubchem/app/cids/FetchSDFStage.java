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
package edu.scripps.fl.pubchem.app.cids;

import java.net.URL;
import java.util.List;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.stage.BaseStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scripps.fl.pubchem.web.pug.PUGSoapFactory;

public class FetchSDFStage extends BaseStage {

	private static final Logger log = LoggerFactory.getLogger(FetchSDFStage.class);

	@Override
	public void process(Object obj) throws StageException {
		List<Number> ids = (List<Number>) obj;
		try {
			int[] list = (int[]) ConvertUtils.convert(ids, int[].class);
			URL url = PUGSoapFactory.getInstance().getSDFilefromCIDs(list);
			System.out.println("SDF File URL: " + url);
			emit(url);
		} catch (Exception ex) {
			throw new StageException(this, ex);
		}
	}
}
