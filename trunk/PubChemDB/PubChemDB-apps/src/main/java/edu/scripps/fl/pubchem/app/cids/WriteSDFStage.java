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

import java.io.File;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.stage.BaseStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteSDFStage extends BaseStage {

	private static final Logger logger = LoggerFactory.getLogger(WriteSDFStage.class);
	
	private AtomicInteger counter = new AtomicInteger();

	private String outputFile = "";

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	@Override
	public void process(Object obj) throws StageException {
		URL url = (URL) obj;
		try {
			File outputFile = newOutputFile(counter.incrementAndGet());
			logger.info("Writing SDF file: " + outputFile);
			IOUtils.copy(new GZIPInputStream(url.openStream()), FileUtils.openOutputStream(outputFile));
			emit(outputFile);
		} catch (Exception ex) {
			throw new StageException(this, ex);
		}
	}

	public File newOutputFile(int index) {
		String outputFile = getOutputFile();
		outputFile = new File(outputFile).getAbsoluteFile().toString();
		String path = FilenameUtils.getFullPath(outputFile);
		String name = FilenameUtils.getBaseName(outputFile);
		String ext = FilenameUtils.getExtension(outputFile);
		File file = new File(path, name + "-" + index + "." + ext);
		return file;
	}
}
