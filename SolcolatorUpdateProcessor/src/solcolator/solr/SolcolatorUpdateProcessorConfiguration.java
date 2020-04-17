package solcolator.solr;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import solcolator.config.ConfigField;
import solcolator.config.ConfigFieldType;
import solcolator.config.SolrConfigurationInitializationException;
import solcolator.config.SolrPluginConfigurationBase;
import solcolator.io.readers.SolcolatorInputKind;
import solcolator.io.writers.SolcolatorOutputKind;
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
	private final static String STR_SEPARATOR = ",";
	
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
	
	@ConfigField(fieldName = "percolatorInputKind", fieldType = ConfigFieldType.STRING, isMandatory = true)
	private String solcolatorInputKindStr;
	
	@ConfigField(fieldName = "inputConfig", fieldType = ConfigFieldType.NAMED_LIST, isMandatory = true)
	private NamedList<?> inputConfig;
	
	@ConfigField(fieldName = "percolatorOutputKind", fieldType = ConfigFieldType.STRING, isMandatory = true)
	private String solcolatorOutputKindStr;
	
	@ConfigField(fieldName = "outputConfig", fieldType = ConfigFieldType.NAMED_LIST, isMandatory = true)
	private NamedList<?> outputConfig;
	
	private List<String> components;
	private LuwakMatcherFactory matchFactory;
	private SolcolatorInputKind solcolatorInputKind;
	private List<SolcolatorOutputKind> solcolatorOutputKinds;
	
	public SolcolatorUpdateProcessorConfiguration(NamedList<?> args) throws SolrConfigurationInitializationException {
		super(args);

		setAndValidateConfig(args);
	}
	
	public void setAndValidateConfig(NamedList<?> args) {		
		try {
			matchFactory = LuwakMatcherFactory.get(matchFactoryStr);
			solcolatorInputKind = SolcolatorInputKind.get(solcolatorInputKindStr);
			solcolatorOutputKinds = SolcolatorOutputKind.get(solcolatorOutputKindStr);
			if (componentsStr == null || componentsStr.isEmpty()) {
				components = new ArrayList<>(0);
			} else {
				components = Arrays.asList(componentsStr.split(STR_SEPARATOR));
			}
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
	
	public SolcolatorInputKind getSolcolatorInputKind() {
		return solcolatorInputKind;
	}
	
	public List<SolcolatorOutputKind> getSolcolatorOutputKinds() {
		return solcolatorOutputKinds;
	}
	
	public NamedList<?> getInputConfig() {
		return this.getNamedListParameter("inputConfig", true);
	}
	
	public NamedList<?> getOutputConfig() {
		return this.getNamedListParameter("outputConfig", true);
	}
}
