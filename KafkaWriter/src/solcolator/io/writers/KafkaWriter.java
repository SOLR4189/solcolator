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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solcolator.io.api.ISolcolatorResultsWriter;

/**
 * NOTE! IT'S A BASIC IMPLEMENTATION ONLY. DON'T USE THIS WRITER IN PRODUCTION ENVIRONMENT WITHOUT ADDITIONAL TESTS!
 * 
 * This writer is designed to write solcolator results to KAFKA topics.
 * Meanwhile this writer writes one item in time
 * 
 * Kafka Writer Config:
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
 * More about Kafka parameters read here: https://kafka.apache.org/11/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html
 */
public class KafkaWriter implements ISolcolatorResultsWriter {
	private static final Logger log = LoggerFactory.getLogger(KafkaWriter.class);
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

	@Override
	public void writeSolcolatorResults(Map<String, List<SolrInputDocument>> queriesToDocs) throws IOException {
		for (Entry<String, List<SolrInputDocument>> queryToDocs : queriesToDocs.entrySet()) {
			for (SolrInputDocument doc : queryToDocs.getValue()) {
				String id = doc.getFieldValue("item_id").toString();
				try {
					//The main idea here: 
					// OR write all results to a specific topic (provided in a configuration)
					// OR if the specific topic isn't provided to write each result to a topic with name equals query id
					String queueName = topicName == null ? queryToDocs.getKey() : topicName;
					producer.send(new ProducerRecord<String, String>(queueName, id));
				} catch (Exception e) {
					log.error(String.format("Doc with id %s of query %s failed to send to kafka",
							id,
							queryToDocs.getKey()),
							e);
				}
			}
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
