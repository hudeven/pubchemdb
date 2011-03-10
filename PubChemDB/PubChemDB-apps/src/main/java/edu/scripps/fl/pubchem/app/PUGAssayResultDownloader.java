package edu.scripps.fl.pubchem.app;

import java.net.URL;
import java.util.Iterator;

import org.apache.commons.pipeline.Pipeline;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scripps.fl.pipeline.PipelineUtils;
import edu.scripps.fl.pubchem.PubChemDB;
import edu.scripps.fl.pubchem.app.results.SourceColumnsInfo;
import edu.scripps.fl.pubchem.app.util.ScrollableResultsIterator;
import edu.scripps.fl.util.CollectionUtils;

public class PUGAssayResultDownloader {

	private static final Logger log = LoggerFactory.getLogger(PUGAssayResultDownloader.class);
	
	public void process(Object feed, String listKey) throws Exception {
		URL url = PUGAssayResultDownloader.class.getClassLoader().getResource("edu/scripps/fl/pubchem/PUGBioAssayResultsPipeline.xml");
		PipelineUtils utils = new PipelineUtils();
		Pipeline pipeline = utils.createPipeline(url, feed, CollectionUtils.createMap("ListKey",listKey, "CsvResultsSource", SourceColumnsInfo.Type.PUG));
	    pipeline.run();
	    utils.logErrors(pipeline);
	}
	
	public void process() throws Exception {
		Query query = PubChemDB.getSession().createSQLQuery("select assay_aid from PCAssay where assay_version_changed = 'T' and assay_total_sid_count > 0 order by assay_aid");
		Iterator<Integer> iter = new ScrollableResultsIterator(Integer.class, query.scroll(ScrollMode.FORWARD_ONLY));
		process(iter, null);
	}
}