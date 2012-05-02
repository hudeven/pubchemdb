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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.pipeline.Pipeline;
import org.apache.commons.pipeline.Stage;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scripps.fl.pipeline.PipelineUtils;
import edu.scripps.fl.pubchem.PubChemDB;
import edu.scripps.fl.pubchem.PubChemFactory;
import edu.scripps.fl.pubchem.app.assayxml.ReadDocumentStage;
import edu.scripps.fl.pubchem.app.util.ScrollableResultsIterator;
import edu.scripps.fl.pubchem.util.CommandLineHandler;

public class AssayDownloader {

	private static final Logger log = LoggerFactory.getLogger(AssayDownloader.class);

	public static void main(String[] args) throws Exception {
		CommandLineHandler clh = new CommandLineHandler() {
			public void configureOptions(Options options) {
				options.addOption(OptionBuilder.withLongOpt("data_url").withType("").withValueSeparator('=').hasArg().create());
				options.addOption(OptionBuilder.withLongOpt("days").withType(0).withValueSeparator('=').hasArg().create());
				options.addOption(OptionBuilder.withLongOpt("mlpcn").withType(false).create());
				options.addOption(OptionBuilder.withLongOpt("notInDb").withType(false).create());
			}
		};
		args = clh.handle(args);
		String data_url = clh.getCommandLine().getOptionValue("data_url");
		if ( null == data_url )
			data_url = "ftp://ftp.ncbi.nlm.nih.gov/pubchem/Bioassay/CSV/";
//			data_url = "file:///C:/Home/temp/PubChemFTP/";

		AssayDownloader main = new AssayDownloader();
		main.dataUrl = new URL(data_url);
		
		if (clh.getCommandLine().hasOption("days"))
			main.days = Integer.parseInt(clh.getCommandLine().getOptionValue("days"));
		if (clh.getCommandLine().hasOption("mlpcn"))
			main.mlpcn = true;
		if (clh.getCommandLine().hasOption("notInDb"))
			main.notInDb = true;

		if (args.length == 0)
			main.process();
		else {
			Long[] list = (Long[]) ConvertUtils.convert(args, Long[].class);
			List<Long> l = (List<Long>) Arrays.asList(list);
			log.info("AID to process: " + l);
			main.process(new HashSet<Long>(Arrays.asList(list)));
		}
	}

	private URL dataUrl;
	private Integer days;
	private boolean mlpcn;
	private boolean notInDb;

	public Pipeline assaySummaries(final Set<Long> processAids) throws Exception {
		URL url = PipelineUtils.class.getClassLoader().getResource("edu/scripps/fl/pubchem/BioAssaySummaryPipeline.xml");
		Pipeline pipeline = new PipelineUtils().createPipeline(url, processAids);
		pipeline.run();
		return pipeline;
	}

	public Pipeline assayXML(final Set<Long> processAids) throws Exception {
		URL url = PipelineUtils.class.getClassLoader().getResource("edu/scripps/fl/pubchem/BioAssayXMLPipeline.xml");
		Pipeline pipeline = new PipelineUtils().createPipeline(url, processAids);
		for (Stage stage : pipeline.getStages())
			if (stage instanceof ReadDocumentStage)
				((ReadDocumentStage) stage).setDataUrl(dataUrl);
		pipeline.run();
		new PipelineUtils().logErrors(pipeline);
		return pipeline;

	}
	
	protected Set<Long> getAIDsfromLocalDB(){
		SQLQuery query = PubChemDB.getSession().createSQLQuery("select assay_aid from pcassay");
		ScrollableResults scroll = query.scroll(ScrollMode.FORWARD_ONLY);
		Iterator<Long> iterator = new ScrollableResultsIterator<Long>(Long.class, scroll);
		Set<Long> set = new HashSet();
		while(iterator.hasNext())
			set.add(iterator.next());
		return set;
	}

	public void process() throws Exception {
		if( notInDb ) {
			SQLQuery query = PubChemDB.getSession().createSQLQuery("select assay_aid from pcassay");
			ScrollableResults scroll = query.scroll(ScrollMode.FORWARD_ONLY);
			Iterator<Long> iterator = new ScrollableResultsIterator<Long>(Long.class, scroll);
			Set<Long> aids = PubChemFactory.getInstance().getAIDs();
			while(iterator.hasNext())
				aids.remove(iterator.next());
			process(aids);
		}
		else if( this.mlpcn ) {
			Set<Long> aids = PubChemFactory.getInstance().getAIDs("\"NIH Molecular Libraries Program\"[SourceCategory] OR \"hasonhold\"[Filter]");
//			for(long id = 1; id < 1788; id++)
//				aids.remove(id);
			process(aids);
		}
		else if (this.days != null)
			process(PubChemFactory.getInstance().getAIDs(this.days));
		else
			process(PubChemFactory.getInstance().getAIDs());
	}

	public void process(Collection<Long> aids) throws Exception {
		log.info(String.format("Processing %s AIDs", aids.size()));
		Set<Long> processAids = Collections.synchronizedSortedSet(new TreeSet<Long>(aids));

		List<Pipeline> pipelines = new ArrayList();
		pipelines.add(assayXML(processAids));
		pipelines.add(assaySummaries(getAIDsfromLocalDB()));

		for (Pipeline pipeline : pipelines)
			new PipelineUtils().logErrors(pipeline);

		System.exit(0);
	}

	public void setDataUrl(URL dataUrl) {
		this.dataUrl = dataUrl;
	}
}