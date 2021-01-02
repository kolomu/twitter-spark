# Twitter-Kafka

Collect tweets and store it in kafka.
Later the tweets can be analysed with spark.

docker-exec params:
- i `Keep STDIN open even if not attached`
- t `Allocate pseudo TTY`

running a command inside a docker container
`docker exec -it kafka-stack-docker-compose_kafka1_1 kafka-topics --zookeeper zoo1:2181 --create --topic Twitter-Kafka --partitions 3 --replication-factor 1`

verify the created topic:
`docker exec -it kafka-stack-docker-compose_kafka1_1 kafka-topics --zookeeper zoo1:2181 --list`

## System Variables
For using the Twitter API the following system variables are used:
- TWITTER_CONSUMER_TOKEN_KEY
- TWITTER_CONSUMER_TOKEN_SECRET
- TWITTER_ACCESS_TOKEN_KEY
- TWITTER_ACCESS_TOKEN_SECRET

