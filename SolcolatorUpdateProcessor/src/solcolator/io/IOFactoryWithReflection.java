package solcolator.io;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.util.NamedList;

import solcolator.io.api.ISolcolatorResultsWriter;
import solcolator.io.api.IQueryReader;

public class IOFactoryWithReflection {
	private final static String CLASS_PROP = "class";
	private IQueryReader queryReader;
	private List<ISolcolatorResultsWriter> solcolatorResultsWriters = new ArrayList<>();

	/**
	 * Registration of all solcolator players (readers, writers)
	 * @throws Exception 
	 */
	public IOFactoryWithReflection(NamedList<?> readerConfig, List<?> writersConfig) throws Exception {
		registerQueryReader(readerConfig);
		registerWriters(writersConfig);
	}
	
	private void registerQueryReader(NamedList<?> readerConfig) throws Exception {
		try {
			String readerType = (String)readerConfig.get(CLASS_PROP);
			Class<?> readerClass = Class.forName(readerType);
			Constructor<?> constructor = readerClass.getConstructor();
			queryReader = (IQueryReader) constructor.newInstance();
			queryReader.init(readerConfig);
		} catch(Exception ex) {
			throw new Exception("Reader registration is failed", ex);
		} 
	}
	
	public IQueryReader getQueryReader() {
		return queryReader;
	}
	
	private void registerWriters(List<?> writersConfig) throws Exception {
		try {
			for (Object writerObject : writersConfig) {
				NamedList<?> writerObjectConfig = (NamedList<?>) writerObject;
				String writerType = (String)writerObjectConfig.get(CLASS_PROP);
				Class<?> writerClass = Class.forName(writerType);
				Constructor<?> constructor = writerClass.getConstructor();
		        ISolcolatorResultsWriter writer = (ISolcolatorResultsWriter) constructor.newInstance();
		        writer.init(writerObjectConfig);
		        
		        solcolatorResultsWriters.add(writer);
			}
		} catch(Exception ex) {
			throw new Exception("Writers registration is failed", ex);
		} 
	}
	
	/**
	 * Get list of writers
	 * @param names of writers
	 * @return registered instances of writers
	 */
	public List<ISolcolatorResultsWriter> getWriters() {
		return solcolatorResultsWriters;
	}	
}
