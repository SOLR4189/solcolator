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
<processor class="solcolator.solr.SolcolatorUpdateProcessorFactory">
	<!-- Scheduling -->
	<int name="targetHour">[hour]</int>
	<int name="targetMin">[minute]</int>
	<int name="targetSec">[second]</int>

	<!-- Components to use -->
	<str name="components"/>
	
	<!-- Factories for matching docs -->
	<str name="matchFactory">simple</str> <!-- simple/highlighting -->

	<lst name="reader">
		<str name="class">solcolator.io.readers.FileReader</str>
		<str name="filePath">[full path to file with queries]</str>
	</lst>
	
	<arr name="writers">
		<!--
		<lst>
			<str name="class">solcolator.io.writers.CollectionWriter</str>
			<str name="zookeepers">[comma separated list of zookeepers with ports]</str>
			<str name="collectionName">[collection name]</str>
			<str name="collectionFl">[comma separated list of fields are separated]</str>
		</lst>
		-->
		
		<!-- About Kafka parameters, read here: https://kafka.apache.org/11/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html
		<lst>
			<str name="class">solcolator.io.writers.KafkaWriter</str>
			<str name="bootstrap.servers">...</str>
			<str name="acks">...</str>
			<str name="enable.idempotence">...</str>
			<str name="batch.size">...</str>
			<str name="linger.ms">...</str>
			<str name="buffer.memory">...</str>
			<str name="key.serializer">...</str>
			<str name="value.serializer">...</str>
			<str name="topicName">[topic name: if this value is null, then results will be written into topic with name equals id of matched query for each matched document]</str>
			<str name="kafkaFl">[comma separated list of fields are separated]</str>
		</lst>
		-->
		
		<lst>
			<str name="class">solcolator.io.writers.FileWriter</str>
			<str name="filePath">[full file path with results (.txt or .csv)</str>
			<str name="fileFl">[comma separated list of fields are separated]</str>
		</lst>
	</arr>
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
			String errMsg = "Config validation is failed";
			log.error(errMsg, ex);
			
			throw new IllegalArgumentException(errMsg, ex);
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
