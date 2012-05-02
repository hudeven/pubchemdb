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

import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.io.LineIterator;
import org.apache.commons.pipeline.Pipeline;
import org.apache.commons.pipeline.Stage;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.scripps.fl.pipeline.PipelineUtils;
import edu.scripps.fl.pubchem.PubChemDB;
import edu.scripps.fl.pubchem.app.util.ScrollableResultsIterator;
import edu.scripps.fl.pubchem.util.CommandLineHandler;

public class CIDDownloader {

	private static final Logger log = LoggerFactory.getLogger(CIDDownloader.class);

	private String outputFile;

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public static void main(String[] args) throws Exception {
		CIDDownloader fetcher = new CIDDownloader();

		CommandLineHandler clh = new CommandLineHandler() {
			public void configureOptions(Options options) {
				options.addOption(OptionBuilder.withLongOpt("input_file").withType("").withValueSeparator('=').hasArg().create());
				options.addOption(OptionBuilder.withLongOpt("output_file").withType("").withValueSeparator('=').hasArg().isRequired().create());
			}
		};
		args = clh.handle(args);
		String inputFile = clh.getCommandLine().getOptionValue("input_file");
		String outputFile = clh.getCommandLine().getOptionValue("output_file");

		fetcher.setOutputFile(outputFile);
		Iterator<?> iterator;
		if (null == inputFile ) {
			if ( args.length == 0 ) {
				log.info("Running query to find CIDs in PCAssayResult but not in PCCompound");
				SQLQuery query = PubChemDB.getSession().createSQLQuery(
								"select distinct r.cid from pcassay_result r left join pccompound c on r.cid = c.cid where (r.cid is not null and r.cid > 0 ) and c.cid is null order by r.cid");
				ScrollableResults scroll = query.scroll(ScrollMode.FORWARD_ONLY);
				iterator = new ScrollableResultsIterator<Integer>(Integer.class, scroll);
			}
			else {
				iterator = Arrays.asList(args).iterator();
			}
		} else if ("-".equals(inputFile)) {
			log.info("Reading CIDs (one per line) from STDIN");
			iterator = new LineIterator(new InputStreamReader(System.in));
		} else {
			log.info("Reading CIDs (one per line) from " + inputFile);
			iterator = new LineIterator(new FileReader(inputFile));
		}
		fetcher.process(iterator);
		System.exit(0);
	}

	public void process(Iterator<?> iterator) throws Exception {
		URL url = getClass().getResource("/edu/scripps/fl/pubchem/SDFDownloadPipeline.xml");
		Pipeline pipeline = new PipelineUtils().createPipeline(url, iterator);
		List<Stage> stages = pipeline.getStages();
		pipeline.run();
		new PipelineUtils().logErrors(pipeline);
	}
}