# Phony Targets
.PHONY: docker_run docker_check_dev docker_stop_deploy docker_check_deploy docker_stop docker_stop_v docker_deploy docker_exec_mysql docker_exec_redis
.PHONY: kafka_create_topic kafka_list_topic_9091 kafka_9091_create_a_topic
.PHONY: ui_redis ui_mysql ui_kafka
 
# Default command 
all: help

# Help Command
help: 
	@echo "Usage: make [command]"
	@echo "Commands:"
	@echo "- Docker Commands:"
	@echo "\t docker_run \t Run Docker Compose"
	@echo "\t docker_check_dev \t Check Docker Compose Dev"
	@echo "\t docker_stop \t Stop Docker Compose"
	@echo "\t docker_stop_v \t Stop Docker Compose and remove volumes, networks, ..."
	@echo "\t docker_deploy \t Deploy Back-End ChatApp"
	@echo "\t docker_check_deploy \t Check Docker Compose Deploy"
	@echo "\t docker_stop_deploy \t Stop Docker Compose Deploy"
	@echo "\t docker_exec_mysql \t Execute MySQL Container"
	@echo "\t docker_exec_redis \t Execute Redis Container"
	@echo "- Kafka Commands:"
	@echo "\t kafka_list_topic_9091 \t List topic kafka of bootstrap server localhost:9091"
	@echo "\t kafka_create_topic \t Auto Create all Kafka Topic Use"
	@echo "\t kafka_9091_create_a_topic \t Create a topic kafka of bootstrap server localhost:9091 with name"
	@echo "- UI View Container:"
	@echo "\t ui_redis \t Open UI Redis Container with redisinsight"
	# @echo "\t ui_mysql \t Open UI MySQL Container with adminer"
	@echo "\t ui_kafka \t Open UI Kafka Container with kafka-ui"

# UI View Container
ui_redis:
	@echo "Open UI Redis Container with redisinsight"
	open http://localhost:15540
	@echo "UI Redis Container opened"

ui_kafka:
	@echo "Open UI Kafka Container with kafka-ui"
	open http://localhost:8083
	@echo "UI Kafka Container opened"

# Kafka Commands
kafka_list_topic_9091:
	@echo "List topic kafka of bootstrap server localhost:9091"
	docker exec -u 0 -it kafka_container /opt/bitnami/kafka/bin/kafka-topics.sh --list --bootstrap-server localhost:9091

kafka_create_topic:
	@echo "Creating Kafka Topic"
	docker exec -u 0 -it kafka_container /opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists --bootstrap-server localhost:9091 --topic go-service-send-mail-otp --partitions 1 --replication-factor 1
	
	@echo -e "\n"
	@echo "Kafka Topic created topic: go-service-send-mail-otp"
	@echo -e "\n"
	
	docker exec -u 0 -it kafka_container /opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists --bootstrap-server localhost:9091 --topic user-request-notification --partitions 1 --replication-factor 1
	
	@echo -e "\n"
	@echo "Kafka Topic created topic: user-request-notification"
	@echo -e "\n"

	docker exec -u 0 -it kafka_container /opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists --bootstrap-server localhost:9091 --topic go-service-send-mail-new-password --partitions 1 --replication-factor 1
	
	@echo -e "\n"
	@echo "Kafka Topic created topic: go-service-send-mail-new-password"
	@echo -e "\n"

kafka_9091_create_a_topic:
	@echo "Creating a topic kafka of bootstrap server localhost:9091 with name"
	@echo "Enter name of topic: "
	@read topic; \
	docker exec -u 0 -it kafka_container /opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists --bootstrap-server localhost:9091 --topic $$topic --partitions 1 --replication-factor 1
	@echo -e "\n"
	@echo "Kafka Topic created topic: $$topic"
	@echo -e "\n"

# Docker Compose
docker_run:
	@echo "Running Docker Compose dev"
	docker compose -f ./environment/docker-compose.yml -p chatapp up -d
	@echo "Docker Compose running"

docker_stop_deploy:
	@echo "Stopping Docker Compose deploy"
	docker compose -f ./environment/docker-compose.prod.yml -p chatapp down
	@echo "Docker Compose stopped"

docker_check_deploy:
	@echo "Checking Docker Compose deploy"
	docker compose -f ./environment/docker-compose.prod.yml ps -a
	@echo "Docker Compose checked"

docker_check_dev:
	@echo "Checking Docker Compose dev"
	docker compose -f ./environment/docker-compose.yml ps -a
	@echo "Docker Compose checked"

docker_stop:
	@echo "Stopping Docker Compose"
	docker compose -f ./environment/docker-compose.yml down
	@echo "Docker Compose stopped"

docker_stop_v:
	@echo "Stopping Docker Compose and remove volumes, networks, ..."
	docker compose -f ./environment/docker-compose.yml down -v
	@echo "Docker Compose stopped and removed volumes, networks, ..."

docker_exec_mysql:
	@echo "Executing MySQL Container"
	docker exec -it mysql_v8_container mysql -u root -p
	@echo "MySQL Container executed"

docker_exec_redis:
	@echo "Executing Redis Container"
	docker exec -it redis_container redis-cli
	@echo "Redis Container

docker_deploy:
	@echo "Note change config productiom before deploy - be/internal/initialize/run.go:15"
	@echo "Deploying Back-End ChatApp"
	docker-compose -f environment/docker-compose.prod.yml -p chatapp up -d --build
	@echo "Back-End ChatApp deployed"