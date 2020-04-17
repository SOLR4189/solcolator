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
import solcolator.luwak.LuwakQuery;
import solcolator.percolator.common.FileUtils;

/**
 * Query reader from json file
 * Config formar for example:
 * <lst name="inputConfig">
 * 	<str name="dirPath">/opt/solr/solcolator</str>
 * </lst>
 *
 */
public class QueryTxtFileReader implements IQueryReader {
	public static final String DIR_PATH = "dirPath";
	private final Type type = new TypeToken<FileQueryObject[]>() { }.getType();
	private final Gson gson = new Gson();
	private File file;
	
	@Override
	public void init(NamedList<?> inputConfig) {
		String dirPath = (String) inputConfig.get(DIR_PATH);
		
		if (!FileUtils.dirExist(dirPath)) {
			throw new IllegalArgumentException(String.format("Directory %s doesn't exist or empty", dirPath));
		}
				
		file = FileUtils.getLatestModifiedFileInDir(dirPath);
	}
	
	@Override
	public List<LuwakQuery> readAllQueries(Map<String, String> reqHandlerMetadata) throws IOException {
		if (file == null) {
			return new ArrayList<LuwakQuery>();
		}
		
		FileQueryObject[] queriesObjects;
		List<LuwakQuery> solcolatorQueries;
		String filePath = file.getAbsolutePath();
		
		try {
			String fileContent = new String(Files.readAllBytes(Paths.get(filePath)), Charsets.UTF_8);
			queriesObjects = gson.fromJson(fileContent, type);
			
			solcolatorQueries = new ArrayList<LuwakQuery>(queriesObjects.length);
			for (int i = 0; i < queriesObjects.length; i++) {
				FileQueryObject obj = queriesObjects[i];
				solcolatorQueries.add(new LuwakQuery(obj.query_id, obj.query_name, obj.query, reqHandlerMetadata));
			}			
		} catch (Exception e) {
			throw new ExceptionInInitializerError(String.format("Failed to read queries from file %s due to %s", filePath, e));
		}

		return solcolatorQueries;
	}
	
	@Override
	public LuwakQuery readByQueryId(String queryId, String queryName, Map<String, String> reqHandlerMetadata) throws IOException {
		List<LuwakQuery> queries = readAllQueries(reqHandlerMetadata);
		
		List<LuwakQuery> foundQueries = queries.stream().filter(x -> x.getId().equals(queryId)).collect(Collectors.toList());
		
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
