# Elasticsearch Demo

## Run the Application
TODO

## Conceptual
The primary question. How to realize a performing search from a web client?

### Technologies
* elastic search .. indexed data base
* postgres db ..  canonical data base
* kibana .. rest client
* spring java service .. **synchronize & access** elastic search api

## The Best Way to Access from a Web Client
TODO

### Access Elastic Search from Java
* [Official Java Clients](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/index.html)
  * [Java Low Level Rest Client](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-low.html)
  * [Java High Level Rest Client](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html)

### Synchronization of Canonical Database and Elastic Search

#### [JDBC River](https://github.com/jprante/elasticsearch-jdbc)
* poll tabular data in a frequent slot of time

#### [Bulk API - Java Client](https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/java-docs-bulk.html)
* use for 
  * your **custom implementation**
  * mass data
* try with [Java High Level Rest Client's Bulk Api](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-document-bulk.html)

Links:
* https://www.elastic.co/blog/found-keeping-elasticsearch-in-sync
* https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/java-docs-bulk.html
* https://github.com/jprante/elasticsearch-jdbc
