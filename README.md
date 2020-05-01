# Solcolator - implementation Saved Searches a la ElasticSearch Percolator

What is Solcolator
------------------

Solrcolator is a SOLR Update Processor based on the open source Luwak https://github.com/flaxsearch/luwak. Solcolator allows you to define a set of search queries and then monitor a stream of indexing documents for any that might match these queries. All matched documents then can be forward for further processing to filesystem, kafka, SOLR collection and etc. 

Get the artifacts
------------------

```
https://github.com/SOLR4189/solcolator/releases
```

Using Solcolator
----------------

Can be add like a usual SOLR Update Processor
```
<processor class="solcolator.solr.SolcolatorUpdateProcessorFactory">
	<!-- Scheduling -->
	<int name="targetHour">8</int>
	<int name="targetMin">0</int>
	<int name="targetSec">0</int>

	<!-- Components to use -->
	<str name="components"/>
	
	<!-- Factories for matching docs:
		SimpleMatcher (simple) - reports which queries matched the InputDocument
		HighlightingMatcher (highlighting) - reports which queries matched, with the individual matches for each query
	 -->
	<str name="matchFactory">simple</str> <!-- simple/highlighting -->
	
	<!--
	<lst name="reader">
		<str name="class">solcolator.io.readers.FileReader</str>
		<str name="filePath">[full path to file with queries]</str>
	</lst>
	-->
	
	<lst name="reader">
		<str name="class">solcolator.io.readers.FileReader</str>
		<str name="filePath">...</str>
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
		
		<!--
		<lst>
			<str name="class">solcolator.io.writers.FileWriter</str>
			<str name="filePath">[full file path with results (.txt or .csv)</str>
			<str name="fileFl">[comma separated list of fields are separated]</str>
		</lst>
		-->
		
		<lst>
			<str name="class">solcolator.io.writers.FileWriter</str>
			<str name="filePath">...</str>
			<str name="fileFl">*</str>
		</lst>
	</arr>
</processor>
```
