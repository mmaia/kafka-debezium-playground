## Kafka Debezium Playground

Project to demonstrate use of Debezium and how to postprocess messages from a source connector 
and parse the Avro Key and Envelop used by Debezium.

### Rekey for KTable

This can be useful to rekey the avro type used by debezium to a primitive 
when using a KTable on top of a topic pupulated by a Debezium Source connector.

-> https://stackoverflow.com/questions/49841008/kafka-streams-how-to-set-a-new-key-for-ktable

### Kafka Streams Join behaviour

This article is extremely useful and explains how Kafka Streams joins work.

https://www.confluent.io/blog/crossing-streams-joins-apache-kafka/

### Branches in this project

This project contains multiple branches, the idea is to have a branch representing a specific
approach to processing debeizum generated messages from source connectors. 

- `join-with-deb-envelop`: This branch demonstrate processing the Debezium messages relying in the default debezium message
format for value(Envelop) and key(Key) and using the `join` between a GlobalKTable(Users) and a KStream(Pets).

- 