# Solcolator - implementation Saved Searches a la ElasticSearch Percolator

What is Solcolator
------------------

Solrcolator is a SOLR Update Processor based on the open source Luwak https://github.com/flaxsearch/luwak. Solcolator allows you to define a set of search queries and then monitor a stream of indexing documents for any that might match these queries. All matched documents then can be forwarded for further processing to filesystem, kafka, SOLR collection and etc. Wrapping Luwak into SOLR Update Processor has many advantages:
* All indexing documents go through SOLR UP - we can sure that all data went through matching processing
* Using SOLR built-in fields analyzers (morphology, language detection, etc..)
* Using SOLR built-in queries parsers (dismax, edismax, etc...)
* Using SOLR as WebService for queries managing (add/update/delete/reread/refresh)

Get the artifacts
------------------

```
https://github.com/SOLR4189/solcolator/releases
```

Using Solcolator
----------------

Can be add like a usual SOLR Update Processor.
NOTE: The preferred location of Solcolator UP in an update chain is the last one (or penultimate if you use solr.RunUpdateProcessorFactory for indexing documents to SOLR after Solcolator processing)
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

Endpoints for queries managing
```
<!-- Solcolator endpoints -->
<requestHandler name="/update_solcolator_queries" class="solcolator.solr.SolcolatorQueriesRequestHander"/>
<requestHandler name="/update_solcolator_info" class="solcolator.solr.SolcolatorInfoRequestHander"/>
```

Managing queries
--------------

ADD/UPDATE:
* Through a reader is in solrconfig (on SOLR start)
```
For example if you use FileReader (see UP config)
[
	{
		"query_id": "1",
		"query_name": "test",
		"query": "q=price:[100 TO 200]"
	},
	{
		"query_id": "2",
		"query_name": "test2",
		"query": "q=type:H%26M"
	}
]
```

* Through an endpoint 
```/update_solcolator_queries```

```

The query with id equals 3 and name equals mytest will be read from the source storage of queries (file, in the case of FileReader) and will be added to Solcolator on-the-fly
http://localhost:9001/solr/Solcolator/update_solcolator_queries?command=update&queryid=3&queryname=mytest
```

DELETE:
* Through an endpoint 
```/update_solcolator_queries```

```
The query with id equals 3 will be deleted from Solcolator, BUT not from the source storage of queries (for example, file in the case of FileReader)
http://localhost:9001/solr/Solcolator/update_solcolator_queries?command=delete&queryid=3
```

REFRESH:
* Through an endpoint 
```/update_solcolator_queries```

```
All queries in Solcolator will be updated, BUT won't be re-read from the source storage of queries.
This command will help in the case, you use dynamic objects in your queries, like 'NOW'
http://localhost:9001/solr/Solcolator/update_solcolator_queries?command=refresh
```

REREAD:
* Through an endpoint 
```/update_solcolator_queries```

```
All queries in Solcolator will be updated, and will be re-read from the source storage of queries.
http://localhost:9001/solr/Solcolator/update_solcolator_queries?command=reread
```

Matching documents
------------------

Matched documents will be forwarded to selected "storage". It depends on selected writer/s. (see UP config)

Solcolator status
------------------

For getting information about all indexed queries you can use an endpoint
```/update_solcolator_info```

```
For example
http://localhost:9001/solr/Solcolator/update_solcolator_info?
```


Customizing the existing components (readers & writers)
-------------------------------------

Solcolator allows to add custom readers and writers. For now there are one custom reader (FileReader) and three custom writers (FileWriter, KafkaWriter, CollectionWriter). 

Possible architecture
-------------------------------------
* For small documents and not massive indexing Solcolator can be a part of a main collection (like an usual UP in solrconfig.xml).

* Otherwise Solcolator should be UP in a secondary collection. In this case Solcolator collection can be optimized (disable caches, disable commits, remove warm queries, remove solr.RunUpdateProcessorFactory and etc...) and MainCollection can't be affected by Solcolator performance.
```
									  Kafka
									 /
									/
			   SolcolatorCollection -- FileSystem
			  /						\
			 /						 \ DB
Indexer  ---
			 \
			  \
			   MainCollection
```

Future releases
-------------------------------------
* New readers and writers (DB reader/writer, for example)
* Improve highlighting mode (maybe using SOLR highlighting)
* Search by query (id, name)
* Upgrade LUCENE version to the latest version








