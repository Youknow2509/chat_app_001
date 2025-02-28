# Phony Targets
.PHONY: docker_run docker_stop docker_stop_v docker_deploy docker_exec_mysql docker_exec_redis

# Help Command
help:
	@echo "Usage: make [command]"
	@echo "Commands:"
	@echo "\Docker Commands:"
	@echo "\t docker_run \t Run Docker Compose"
	@echo "\t docker_stop \t Stop Docker Compose"
	@echo "\t docker_stop_v \t Stop Docker Compose and remove volumes, networks, ..."
	@echo "\t docker_deploy \t Deploy Back-End ChatApp"
	@echo "\t docker_exec_mysql \t Execute MySQL Container"
	@echo "\t docker_exec_redis \t Execute Redis Container"

# Docker Compose
docker_run:
	@echo "Running Docker Compose dev"
	docker compose -f ./environment/docker-compose.yml up -d
	@echo "Docker Compose running"

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
	docker-compose -f environment/docker-compose.prod.yml up -d --build
	@echo "Back-End ChatApp deployed"