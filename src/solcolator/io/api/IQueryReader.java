package solcolator.io.api;

import java.io.IOException;

import org.apache.solr.common.util.NamedList;

import solcolator.luwak.LuwakQuery;

public interface IQueryReader {
	void init(NamedList<?> inputConfig) throws IOException;
	
	LuwakQuery[] readAllQueries() throws IOException;
}
