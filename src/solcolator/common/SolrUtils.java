package solcolator.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;

/**
* Helper class. Luwak MonitorQuery can get Map<String,String> metadata only, so this class will be useful for
* LuwakQuery building.
*/
public class SolrUtils {
	private static final String SEPARATOR = "<!map_separator!>";
			
	public static Map<String,String> solrParamsToMap(SolrParams params) {
		Map<String,String> map = new HashMap<>();
		for(Iterator<String> it = params.getParameterNamesIterator(); it.hasNext();) {
			String name = it.next();
			final String[] values = params.getParams(name);
			
			if (values.length == 1) {
				map.put(name, values[0]);
			} else {
				map.put(name, String.join(SEPARATOR, values));
			}
		}
		
		return map;
	}
	
	public static String solrParamsToString(SolrParams params) {
		StringBuilder builder = new StringBuilder();
		
		for(Iterator<String> it = params.getParameterNamesIterator(); it.hasNext();) {
			String name = it.next();
			final String[] values = params.getParams(name);
			
			if (values.length == 1) {
				builder.append(name);
				builder.append("=");
				builder.append(values[0]);
				builder.append("&");
			} else {
				for(String value : values) {
					builder.append(name);
					builder.append("=");
					builder.append(value);
					builder.append("&");
				}
			}
			
			builder.deleteCharAt(builder.length() - 1);
		}
		
		return builder.toString();
	}
	
	public static NamedList<Object> mapToNamedList(Map<String,String> map) {
		NamedList<Object> solrParamsNamedList = new NamedList<Object>();
		for(Entry<String,String> entry : map.entrySet()) {
			String key = entry.getKey();
			String[] values = entry.getValue().split(SEPARATOR);
			solrParamsNamedList.add(key, values);
		}
		
		return solrParamsNamedList;
	}
}
