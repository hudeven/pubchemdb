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
package edu.scripps.fl.pubchem.app;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.pipeline.Pipeline;
import org.apache.commons.pipeline.Stage;

import edu.scripps.fl.pipeline.PipelineUtils;
import edu.scripps.fl.pubchem.util.CommandLineHandler;

public class RelatedCurveProcessor {

	public static void main(String[] args) throws Exception {
		CommandLineHandler clh = new CommandLineHandler();
		args = clh.handle(args);

		Integer[] list = (Integer[]) ConvertUtils.convert(args, Integer[].class);
		Integer referenceAid = list[0];
		Integer[] relatedAids = new Integer[list.length - 1];
		System.arraycopy(list, 1, relatedAids, 0, list.length - 1);

		URL url = PipelineUtils.class.getClassLoader().getResource("edu/scripps/fl/pubchem/RelatedCurvesPipeline.xml");
		Pipeline pipeline = new PipelineUtils().createPipeline(url, Arrays.asList(relatedAids));
		List<Stage> stages = pipeline.getStages();
		BeanUtils.setProperty(stages.get(1), "referenceAID", referenceAid);
		pipeline.run();
		new PipelineUtils().logErrors(pipeline);
	}
}
