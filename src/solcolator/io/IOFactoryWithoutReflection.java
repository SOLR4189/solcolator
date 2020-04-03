package solcolator.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import solcolator.io.api.ISolcolatorResultsWriter;
import solcolator.io.api.IQueryReader;
import solcolator.io.readers.SolcolatorInputKind;
import solcolator.io.readers.QueryTxtFileReader;
import solcolator.io.writers.CollectionWriter;
import solcolator.io.writers.KafkaWriter;
import solcolator.io.writers.SolcolatorOutputKind;
import solcolator.io.writers.SolcolatorResultsFileWriter;

public class IOFactoryWithoutReflection {
	private final static IOFactoryWithoutReflection factory = new IOFactoryWithoutReflection();
	
	private final Map<SolcolatorInputKind, IQueryReader> queryReaders = new HashMap<>();
	private final Map<SolcolatorOutputKind, ISolcolatorResultsWriter> solcolatorResultsWriters = new HashMap<>();
	
	public static IOFactoryWithoutReflection instance() {
		return factory;
	}
	
	/**
	 * Registration of all solcolator players (readers, writers)
	 */
	private IOFactoryWithoutReflection() {
		registerQueryReader(SolcolatorInputKind.DIRECTORY, new QueryTxtFileReader());
		registerSolcolatorResultsWriter(SolcolatorOutputKind.FILE, new SolcolatorResultsFileWriter());
		registerSolcolatorResultsWriter(SolcolatorOutputKind.KAFKA, new KafkaWriter());
		registerSolcolatorResultsWriter(SolcolatorOutputKind.COLLECTION, new CollectionWriter());
	}
	
	private void registerQueryReader(SolcolatorInputKind name, IQueryReader queryReader) {
		queryReaders.put(name, queryReader);
	}
	
	private void registerSolcolatorResultsWriter(SolcolatorOutputKind name, ISolcolatorResultsWriter solcolatorResultsWriter) {
		solcolatorResultsWriters.put(name, solcolatorResultsWriter);
	}
	
	public IQueryReader getQueryReader(SolcolatorInputKind name) {
		return queryReaders.get(name);
	}
	
	/**
	 * Get list of writers in config order
	 * @param names of writers
	 * @return registered instances of writers
	 */
	public List<ISolcolatorResultsWriter> getSolcolatorResultsWriters(List<SolcolatorOutputKind> names) {
		List<ISolcolatorResultsWriter> writers = new ArrayList<>();
		
		for (SolcolatorOutputKind name : names) {
			writers.add(solcolatorResultsWriters.get(name));
		}
		
		return writers;
	}	
}
