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
package edu.scripps.fl.pubchem.app.summary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.stage.ExtendedBaseStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scripps.fl.pubchem.web.entrez.EUtilsWebSession;

public class ESummaryStage extends ExtendedBaseStage {

	private static final Logger log = LoggerFactory.getLogger(ESummaryStage.class);

	@Override
	public void innerProcess(Object obj) throws StageException {
		try {
			Thread.sleep(333);
			List<Long> aids = (List<Long>) obj;
			InputStream in = EUtilsWebSession.getInstance().getSummaries(aids, "pcassay");
			File file = File.createTempFile("pubchem", "summary.xml");
			log.info(String.format("Downloaded %s summaries to %s", aids.size(), file));
			IOUtils.copy(in, new FileOutputStream(file));
			emit(file);
		} catch (Exception ex) {
			throw new StageException(this, ex);
		}
	}

	public String status() {
		return "";
	}
}