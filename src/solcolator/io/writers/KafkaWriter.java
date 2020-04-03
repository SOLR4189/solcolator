package solcolator.io.writers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;

import solcolator.io.api.ISolcolatorResultsWriter;

/**
 * This writer is designed to write percolator results to KAFKA queues.
 * Meanwhile this writer writes one item in time
 * Implementation of this writer isn't checked yet
 * Parameters which must be provided: kafka parameters and fields list
 * 
 * Config for example:
 	<str name="bootstrap.servers">server</str>
	<str name="acks">all</str>
	<str name="enable.idempotence">true</str>
	<str name="batch.size">16384</str>
	<str name="linger.ms">1</str>
	<str name="buffer.memory">33554432</str>
	<str name="key.serializer">org.apache.kafka.common.serialization.StringSerializer</str>
	<str name="value.serializer">org.apache.kafka.common.serialization.StringSerializer</str>
	<str name="topicName">solr.stt-poc</str>
	<str name="kafkaFl">item_id,Item_Kind_s</str>
 * @author 
 *
 */
public class KafkaWriter implements ISolcolatorResultsWriter {
	private static final String KAFKA_FL = "kafkaFl";
	private static final String KAFKA_TOPIC = "topicName";
	
	private List<String> fl;
	private String topicName;
	private Producer<String,String> producer;
	
	@Override
	public void init(NamedList<?> outputConfig) throws IOException {
		this.fl = Arrays.asList(((String) outputConfig.get(KAFKA_FL)).split(","));
		this.topicName = (String) outputConfig.get(KAFKA_TOPIC);
		
		Properties kafkaProps = new Properties();
		for (int i = 0; i < outputConfig.size(); i++) {
			kafkaProps.put(outputConfig.getName(i), outputConfig.getVal(i));
		}
		
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(null);
		producer = new KafkaProducer<>(kafkaProps);
		Thread.currentThread().setContextClassLoader(classLoader);
	}
	
	//TODO: change write logic
	@Override
	public void writeSolcolatorResults(Map<String, SolrInputDocument> docs) throws IOException {
		for (Entry<String, SolrInputDocument> doc : docs.entrySet()) {
			String queueName = topicName == null ? doc.getKey() : topicName;
			producer.send(new ProducerRecord<String, String>(queueName, doc.getValue().getFieldValue("item_id").toString()));
		}
	}
	
	@Override
	public List<String> getFl() {
		return fl;
	}

	@Override
	public void close() {
		producer.close();
	}
}
