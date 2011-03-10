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
package edu.scripps.fl.pubchem.app.results;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.stage.BaseStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scripps.fl.pubchem.PubChemFactory;

public class DownloadCsvStage extends BaseStage {

	private static final Logger log = LoggerFactory.getLogger(DownloadCsvStage.class);

	private URL dataUrl;

	public URL getDataUrl() {
		return dataUrl;
	}

	public void setDataUrl(URL dataUrl) {
		this.dataUrl = dataUrl;
	}

	@Override
	public void process(Object obj) throws StageException {
		Integer aid = (Integer) obj;
		try {
			InputStream is = PubChemFactory.getInstance().getCsv(getDataUrl(), aid);
			emit(new Object[]{aid, is});
		} catch (IOException ex) {
			throw new StageException(this, ex);
		}
	}
}