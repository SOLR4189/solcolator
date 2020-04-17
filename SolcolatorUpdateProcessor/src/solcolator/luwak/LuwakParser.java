package solcolator.luwak;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.MultiMapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.servlet.SolrRequestParsers;

import com.google.common.base.Charsets;
import com.google.common.net.UrlEscapers;

import solcolator.common.SolrUtils;
import uk.co.flax.luwak.MonitorQueryParser;

public class LuwakParser implements MonitorQueryParser {
	private final SolrCore core;
	private final List<String> componentsToParser;
	
	public LuwakParser(SolrCore core, List<String> componentsToParser) {
		this.core = core;
		this.componentsToParser = componentsToParser;
	}
	
	//TODO: check if query/fq syntax is wrong (for example, time ranges)
	/**
	 * Parse query string with metadata to lucene query. For use SolrRequestParsers.parseQueryString
	 * we must get query string where 4 specific characters in query body are replaced to its %xy format
	 * 	& 	- 	%26
	 *	+	- 	%2B
	 *	=	-	%3D
	 *	%	-	%25
	 */
	@Override
	public Query parse(String query, Map<String, String> queryMetadata) throws Exception {
		NamedList<Object> solrParamsNamedList = SolrUtils.mapToNamedList(queryMetadata);	
		MultiMapSolrParams queryParams = SolrRequestParsers.parseQueryString(UrlEscapers.urlFragmentEscaper().escape(query));
		
		for (Entry<String, Object> entry : queryParams.toNamedList()) {
			solrParamsNamedList.add(
					URLDecoder.decode(entry.getKey(), Charsets.UTF_8.toString()),
					URLDecoder.decode(entry.getValue().toString(), Charsets.UTF_8.toString()));
		}
				
		SolrParams solrParams = SolrParams.toSolrParams(solrParamsNamedList);	
		SolrQueryRequest solrRequest = new LocalSolrQueryRequest(core, solrParamsNamedList);
		
		if (componentsToParser != null && componentsToParser.size() != 0) {
			List<SearchComponent> components = new ArrayList<SearchComponent>();
			for (String searchComponentName : componentsToParser) {
				components.add(core.getSearchComponent(searchComponentName));
			}
			
			ResponseBuilder rb = new ResponseBuilder(solrRequest, new SolrQueryResponse(), components);
			
			for (SearchComponent searchComponent : components) {
				searchComponent.prepare(rb);
			}
		}

		QParserPlugin qplug = core.getQueryPlugin("edismax");
		QParser parser = qplug.createParser(solrRequest.getParams().get(CommonParams.Q), null, solrParams, solrRequest);
		
		BooleanQuery bq = (BooleanQuery) parser.parse();
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		
		for (BooleanClause booleanClause : bq) {
			builder.add(booleanClause.getQuery(), booleanClause.getOccur());
		}
		
		String[] fqs = solrParams.getParams(CommonParams.FQ);
		if (fqs != null) {
			for (String fq : solrParams.getParams(CommonParams.FQ)) {
				BooleanQuery bfq = (BooleanQuery) qplug.createParser(fq, null, solrParams, solrRequest).parse();
				
				for (BooleanClause clause : bfq) {
					if (clause.getOccur().equals(Occur.MUST_NOT)) {
						builder.add(clause.getQuery(), Occur.MUST_NOT);
					} else {
						builder.add(clause.getQuery(), Occur.FILTER);
					}
			    }
			}
		}

		return new ConstantScoreQuery(builder.build());
	}
}
