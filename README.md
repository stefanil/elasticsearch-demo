# Elasticsearch Demo

## Do

### Run
* start docker composition: `[...]\elasticsearch-demo\compose\src\main\resources$ docker-compose up -d`
* see kibana in action: `http://localhost:5601`
  * you may use kibana's dev tools to run some sample queries

### Compose
* [elastic docker images](https://www.docker.elastic.co/)
  * [Install Elasticsearch 6.0 with Docker](https://www.elastic.co/guide/en/elasticsearch/reference/6.0/docker.html)
  * [Install Kibana 6.0 with Docker](https://www.elastic.co/guide/en/kibana/6.0/docker.html)

## Think
The primary question. How to realize a performing search from a web client?

### Technologies
* **elastic search** .. indexed data base
* **postgres db** ..  canonical data base
* **kibana** .. rest client
* **spring boot** .. **synchronize & access** elastic search api
* **docker compose** .. compose containers
* **gradle** .. start / stop composition

### Access Elastic Search from Java
* [Official Java Clients](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/index.html)
  * [Java Low Level Rest Client](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-low.html)
  * [Java High Level Rest Client](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html)
    * *added with 6.0.0-beta1: available with current release 6.0.0?*

### ES in the Cloud
* [AWS Elasticsearch-Service](https://aws.amazon.com/de/elasticsearch-service/)
  * *synchronization mechanisms?*

### Synchronization of Canonical Database and Elastic Search

#### [Bulk API - Java Client](https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/java-docs-bulk.html)
* use for 
  * your **custom implementation**
  * mass data
* try with [Java High Level Rest Client's Bulk Api](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-document-bulk.html)
* 2 approaches:
  * **event driven** on  saving and when initializing the database
  * **scheduled** from a custom JSON table, which holds tabular data in JSON format

#### [JDBC River](https://github.com/jprante/elasticsearch-jdbc)
* poll tabular data in a frequent slot of time
* connection via JDBC layer does not conform to microservice approach
* only for simple table with no deep relations
  * customization must be maintained

### Access from a Web Client
* *separate ES endpoint vs. custom endpoint for adaption of result?*
  * *adaption of ES search response via service layer needed?*

### Further Links
* https://www.elastic.co/blog/found-keeping-elasticsearch-in-sync
* https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/java-docs-bulk.html
* https://github.com/jprante/elasticsearch-jdbc
