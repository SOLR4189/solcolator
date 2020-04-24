package solcolator.io.writers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import solcolator.io.api.ISolcolatorResultsWriter;

/**
 * NOTE! IT'S A BASIC IMPLEMENTATION ONLY. DON'T USE THIS WRITER IN PRODUCTION ENVIRONMENT WITHOUT ADDITIONAL TESTS!
 * 
 * This writer is designed to write solcolator results to a file
 * This writer can be used for testing and integration purposes
 * 
 * File Writer Config:
 	<lst>
		<str name="class">solcolator.io.writers.FileWriter</str>
		<str name="filePath">[full file path with results (.txt or .csv)</str>
		<str name="fileFl">[comma separated list of fields are separated]</str>
	</lst>
 */
public class FileWriter implements ISolcolatorResultsWriter, AutoCloseable {
	private static final Logger log = LoggerFactory.getLogger(FileWriter.class);
	private static final String FILE_PATH = "filePath";
	private static final String FILE_FL = "fileFl";
	private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

	private List<String> fl;
	private BufferedWriter bw;
	
	@Override
	public void init(NamedList<?> outputConfig) throws IOException {
		this.fl = Arrays.asList(((String) outputConfig.get(FILE_FL)).split(","));
		String filePath = (String) outputConfig.get(FILE_PATH);
		File file = new File(filePath);
		
		if (!file.exists()) {
			try {
				new File(filePath).createNewFile();
			} catch (IOException e) {
				throw new IOException(String.format("File %s doesn't exist and can't be created", filePath));
			}
		}
				
		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath, false), StandardCharsets.UTF_8));
	}
	
	@Override
	public void writeSolcolatorResults(Map<String, List<SolrInputDocument>> queriesToDocs) {
		try {
			bw.append(String.format("%s\n", gson.toJson(queriesToDocs)));
			bw.flush();
		} catch (IOException ex) {
			log.error("Writing results to file is failed", ex);
		}
	}
	
	@Override
	public List<String> getFl() {
		return fl;
	}

	@Override
	public void close() throws IOException {
		if (bw != null) {
			bw.close();
		}
	}
}