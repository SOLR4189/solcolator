package solcolator.io.readers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.solr.common.util.NamedList;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import solcolator.io.api.IQueryReader;
import solcolator.io.api.SolcolatorQuery;

/**
 * NOTE! IT'S A BASIC IMPLEMENTATION ONLY. DON'T USE THIS READER IN PRODUCTION ENVIRONMENT WITHOUT ADDITIONAL TESTS!
 * 
 * Query reader from json file
 * 
 * File Reader Config:
 * <lst name="reader">
		<str name="class">solcolator.io.readers.FileReader</str>
		<str name="filePath">[full path to file with queries]</str>
	</lst>
 * 
 * Query file for example:
 * [
		{
			"query_id": "1",
			"query_name": "test",
			"query": "q=price:[100 TO 200]"
		}
   ]
 */
public class FileReader implements IQueryReader {
	public static final String FILE_PATH = "filePath";
	private final Type type = new TypeToken<FileQueryObject[]>() { }.getType();
	private final Gson gson = new Gson();
	private File file;
	
	@Override
	public void init(NamedList<?> inputConfig) {
		String filePath = (String) inputConfig.get(FILE_PATH);	
		file = new File(filePath);
		
		if (!file.exists()) {
			throw new IllegalArgumentException(String.format("File %s doesn't exist", filePath));
		}
	}
	
	@Override
	public List<SolcolatorQuery> readAllQueries(Map<String, String> reqHandlerMetadata) throws IOException {
		if (file == null) {
			return new ArrayList<SolcolatorQuery>();
		}
		
		FileQueryObject[] queriesObjects;
		List<SolcolatorQuery> solcolatorQueries;
		String filePath = file.getAbsolutePath();
		
		try {
			String fileContent = new String(Files.readAllBytes(Paths.get(filePath)), Charsets.UTF_8);
			queriesObjects = gson.fromJson(fileContent, type);
			
			solcolatorQueries = new ArrayList<SolcolatorQuery>(queriesObjects.length);
			for (int i = 0; i < queriesObjects.length; i++) {
				FileQueryObject obj = queriesObjects[i];
				solcolatorQueries.add(new SolcolatorQuery(obj.query_id, obj.query_name, obj.query, reqHandlerMetadata));
			}			
		} catch (Exception e) {
			throw new ExceptionInInitializerError(String.format("Failed to read queries from file %s due to %s", filePath, e));
		}

		return solcolatorQueries;
	}
	
	@Override
	public SolcolatorQuery readByQueryId(String queryId, String queryName, Map<String, String> reqHandlerMetadata) throws IOException {
		List<SolcolatorQuery> queries = readAllQueries(reqHandlerMetadata);
		
		List<SolcolatorQuery> foundQueries = queries.stream().filter(x -> x.getQueryId().equals(queryId)).collect(Collectors.toList());
		
		if (foundQueries.isEmpty()) {
			String errMsg = String.format("Query with id %s wasn't found", queryId);
			
			throw new IOException(errMsg);
		}
		
		if (foundQueries.size() > 1) {
			String errMsg = String.format("Were found %d queries with id %s", foundQueries.size(), queryId);
			
			throw new IOException(errMsg);
		}

		return foundQueries.get(0);
	}
	
	private class FileQueryObject {
		public String query_id;
		public String query_name;
		public String query;
	}

	@Override
	public void close() throws Exception {
		//Nothing to close
	}
}
