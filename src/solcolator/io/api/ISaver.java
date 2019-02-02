package solcolator.io.api;

import java.io.IOException;
import java.util.Map;

import org.apache.solr.common.util.NamedList;

import solcolator.luwak.LuwakQuery;

public interface ISaver {
	void init(NamedList<?> saveConfig) throws IOException;
	
	void saveTo(Map<String, LuwakQuery> queries) throws IOException;
}
