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

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.stage.ExtendedBaseStage;
import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scripps.fl.pubchem.web.entrez.EUtilsWebSession;

public class DocumentStage extends ExtendedBaseStage {

	private static final Logger log = LoggerFactory.getLogger(DocumentStage.class);

	@Override
	public void innerProcess(Object obj) throws StageException {
		File file = (File) obj;
		try {
			Document document = EUtilsWebSession.getInstance().getDocument(new FileInputStream(file));
			file.deleteOnExit();
			emit(document);
		} catch (Exception ex) {
			log.error(String.format("Problem with file %s: %s", file, ex.getMessage()));
			throw new StageException(this, ex);
		}
	}

	public String status() {
		return "";
	}
}