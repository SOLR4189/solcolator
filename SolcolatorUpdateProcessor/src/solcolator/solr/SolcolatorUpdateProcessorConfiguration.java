package solcolator.solr;

import java.util.List;

import solcolator.config.ConfigField;
import solcolator.config.ConfigFieldType;
import solcolator.config.SolrConfigurationInitializationException;
import solcolator.config.SolrPluginConfigurationBase;
import solcolator.luwak.LuwakMatcherFactory;

import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 	<processor class="SolrcolatorUpdateProcessorFactory">
		<!-- Scheduling -->
		<int name="targetHour">8</int>
		<int name="targetMin">0</int>
		<int name="targetSec">0</int>
	
		<!-- Components to use -->
		<str name="components">rewriter</str>
		
		<!-- Factories for matching docs -->
		<str name="matchFactory">simple</str> <!-- simple/highlighting -->
		
		<!-- I/O -->
		<str name="percolatorInputKind">directory</str> <!-- directory -->
		<str name="percolatorOutputKind">collection,tagger</str> <!-- file/kafka/collection/tagger -->
		
		<lst name="inputConfig">
			<str name="dirPath">C:\\Solrs\\solr-6.5.1\\solr-6.5.1\\example\\cloud\\percolator\\queries</str>
		</lst>
		
		<lst name="outputConfig">
			<!-- for file writer -->
			<str name="filePath">C:\\Solrs\\solr-6.5.1\\solr-6.5.1\\example\\cloud\\percolator\\out\\out.csv</str>
			<str name="fileFl">item_id,Item_Kind_s</str>
			
			<!-- for kafka writer -->
			<str name="bootstrap.servers">user</str>
			<str name="acks">all</str>
			<str name="enable.idempotence">true</str>
			<str name="batch.size">16384</str>
			<str name="linger.ms">1</str>
			<str name="buffer.memory">33554432</str>
			<str name="key.serializer">org.apache.kafka.common.serialization.StringSerializer</str>
			<str name="value.serializer">org.apache.kafka.common.serialization.StringSerializer</str>
			<str name="kafkaFl">item_id,Item_Kind_s</str>
			
			<!-- for collection writer -->
			<str name="zookeepers">zootest01:2181,zootest03:2181,zootest04:2181</str>
			<str name="collectionName">PercolatorTmp</str>
			<str name="collectionFl">*</str>
			
			<!-- for tagger writer -->
			<str name="url">http://taglife/Tagger/Bulk</str>
			<str name="httpFl">item_id,timestamp</str>
		</lst>
	</processor>
 */
public class SolcolatorUpdateProcessorConfiguration extends SolrPluginConfigurationBase  {
	private final static Logger log = LoggerFactory.getLogger(SolcolatorUpdateProcessorConfiguration.class);
	
	@ConfigField(fieldName = "targetHour", fieldType = ConfigFieldType.INT, isMandatory = true)
	private int targetHour;
	
	@ConfigField(fieldName = "targetMin", fieldType = ConfigFieldType.INT, isMandatory = true)
	private int targetMin;
	
	@ConfigField(fieldName = "targetSec", fieldType = ConfigFieldType.INT, isMandatory = true)
	private int targetSec;
	
	@ConfigField(fieldName = "components", fieldType = ConfigFieldType.STRING, isMandatory = true)
	private String componentsStr;
	
	@ConfigField(fieldName = "matchFactory", fieldType = ConfigFieldType.STRING, isMandatory = true)
	private String matchFactoryStr;
	
	@ConfigField(fieldName = "reader", fieldType = ConfigFieldType.NAMED_LIST, isMandatory = true)
	private NamedList<?> reader;
	
	@ConfigField(fieldName = "writers", fieldType = ConfigFieldType.ARRAY, isMandatory = true)
	private List<?> writers;
	
	private List<String> components;
	private LuwakMatcherFactory matchFactory;
	
	public SolcolatorUpdateProcessorConfiguration(NamedList<?> args) throws SolrConfigurationInitializationException {
		super(args);

		setAndValidateConfig(args);
	}
	
	public void setAndValidateConfig(NamedList<?> args) {		
		try {
			matchFactory = LuwakMatcherFactory.get(matchFactoryStr);
		} catch(Exception ex) {
			String errMsg = String.format("Config validation is failed due to %s", ex);
			log.error(errMsg);
			throw new IllegalArgumentException(errMsg);
		}	
	}
	
	public int getTargetHour() {
		return targetHour;
	}
	
	public int getTargetMin() {
		return targetMin;
	}
	
	public int getTargetSec() {
		return targetSec;
	}
	
	public List<String> getComponents() {
		return components;
	}
	
	public LuwakMatcherFactory getMatcherFactory() {
		return matchFactory;
	}
	
	public NamedList<?> getReader() {
		return this.getNamedListParameter("reader", true);
	}
	
	public List<?> getWriters() {
		return this.getArrParameter("writers", true);
	}
}
