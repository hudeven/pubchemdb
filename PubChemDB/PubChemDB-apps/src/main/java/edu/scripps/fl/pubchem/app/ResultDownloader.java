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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.pipeline.Pipeline;
import org.apache.commons.pipeline.Stage;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scripps.fl.pipeline.PipelineUtils;
import edu.scripps.fl.pubchem.PubChemDB;
import edu.scripps.fl.pubchem.app.results.DownloadCsvStage;
import edu.scripps.fl.pubchem.app.results.SourceColumnsInfo;
import edu.scripps.fl.pubchem.app.util.ScrollableResultsIterator;
import edu.scripps.fl.pubchem.util.CommandLineHandler;
import edu.scripps.fl.util.CollectionUtils;

public class ResultDownloader {

	private static final Logger log = LoggerFactory.getLogger(ResultDownloader.class);

	private URL dataUrl;

	protected void process(Iterator<Integer> processAids) throws Exception {
		URL url = PipelineUtils.class.getClassLoader().getResource("edu/scripps/fl/pubchem/BioAssayResultsPipeline.xml");
		Pipeline pipeline = new PipelineUtils().createPipeline(url, processAids, CollectionUtils.createMap("CsvResultsSource", SourceColumnsInfo.Type.FTP));
		for (Stage stage : pipeline.getStages())
			if (stage instanceof DownloadCsvStage)
				((DownloadCsvStage) stage).setDataUrl(dataUrl);
		pipeline.run();
		new PipelineUtils().logErrors(pipeline);
	}

	protected void process() throws Exception {
		Query query = PubChemDB.getSession().createSQLQuery(
				"select assay_aid from PCAssay where assay_version_changed = 'T' and assay_total_sid_count > 0 order by assay_aid");
		Iterator<Integer> iter = new ScrollableResultsIterator(Integer.class, query.scroll(ScrollMode.FORWARD_ONLY));
		process(iter);
	}

	public URL getDataUrl() {
		return dataUrl;
	}

	public void setDataUrl(URL dataUrl) {
		this.dataUrl = dataUrl;
	}

	public static void main(String[] args) throws Exception {
		CommandLineHandler clh = new CommandLineHandler() {
			public void configureOptions(Options options) {
				options.addOption(OptionBuilder.withLongOpt("data_url").withType("").withValueSeparator('=').hasArg().create());
			}
		};
		args = clh.handle(args);
		String data_url = clh.getCommandLine().getOptionValue("data_url");
		ResultDownloader rd = new ResultDownloader();

		if (data_url != null)
			rd.setDataUrl(new URL(data_url));
		else
			rd.setDataUrl(new URL("ftp://ftp.ncbi.nlm.nih.gov/pubchem/Bioassay/CSV/"));
		if (args.length == 0)
			rd.process();
		else {
			Integer[] list = (Integer[]) ConvertUtils.convert(args, Integer[].class);
			rd.process(((List<Integer>) Arrays.asList(list)).iterator());
		}
	}
}
