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

import java.net.URL;
import java.util.Map;

import org.apache.commons.pipeline.Pipeline;
import org.apache.commons.pipeline.PipelineCreationException;
import org.apache.commons.pipeline.ProcessingException;
import org.apache.commons.pipeline.StageDriver;
import org.apache.commons.pipeline.config.DigesterPipelineFactory;
import org.apache.commons.pipeline.driver.AbstractStageDriver;
import org.apache.commons.pipeline.driver.FaultTolerance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipelineUtils {

	private static final Logger log = LoggerFactory.getLogger(PipelineUtils.class);

	public Pipeline createPipeline(URL url, Object feed) throws PipelineCreationException {
		DigesterPipelineFactory factory = new DigesterPipelineFactory(url);
		Pipeline pipeline = factory.createPipeline();
		for (StageDriver driver : pipeline.getStageDrivers())
			((AbstractStageDriver) driver).setFaultTolerance(FaultTolerance.CHECKED);
		pipeline.getSourceFeeder().feed(feed);
		return pipeline;
	}
	
	public Pipeline createPipeline(URL url, Object feed, Map<String,Object> globals) throws PipelineCreationException {
		Pipeline pipeline = createPipeline(url, feed);
		for(Map.Entry<String, Object> entry: globals.entrySet())
			pipeline.setEnv(entry.getKey(), entry.getValue());
		return pipeline;
	}

	public void logErrors(Pipeline pipeline) {
		for (StageDriver driver : pipeline.getStageDrivers()) {
			for (ProcessingException pe : driver.getProcessingExceptions()) {
				log.error(String.format("%s\t%s", pe.getData(), pe.getCause().getMessage()));
			}
		}
	}
}