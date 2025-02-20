# List Variables Path
SERVER_MAIN = ./cmd/server/main.go
WIRE_DIR = internal/wire

# List Variables Wire
GO = go
WIRE = wire

# List Variables Goose
GOOSE=goose
GOOSE_NAME ?= GO_ECOMMERCE
GOOSE_DRIVER ?= mysql
GOOSE_DB_DSN = "root:root123@tcp(127.0.0.1:3306)/go_back_end"
GOOSE_MIGRATION_DSN ?= sql/schema
GOOSE_PATH_SCHEMA ?= sql/schema
GOOSE_PATH_QUERIES = sql/queries

# Phony Targets
.PHONY: help
.PHONY: sqlc_generate cre_env run_server
.PHONY: docker_run docker_stop docker_stop_v docker_exec_mysql docker_exec_redis
.PHONY: swagger_generate
.PHONY: goose_create goose_up goose_down goose_status goose_fix goose_redo goose_reset goose_clean goose_up_by_one

# Help Command
help:
	@echo "Usage: make [command]"
	@echo "Commands:"
	@echo "\t run_server \t Start the server"
	@echo "\t cre_env \t Create .env from .yaml"
	@echo "\nSQLC Commands:"
	@echo "\t sqlc_generate \t Generate SQLC queries"
	@echo "\Docker Commands:"
	@echo "\t docker_run \t Run Docker Compose"
	@echo "\t docker_stop \t Stop Docker Compose"
	@echo "\t docker_stop_v \t Stop Docker Compose and remove volumes, networks, ..."
	@echo "\t docker_exec_mysql \t Execute MySQL Container"
	@echo "\t docker_exec_redis \t Execute Redis Container"
	@echo "\nSwagger Commands:"
	@echo "\t swagger_generate \t Generate Swagger Documentation"
	@echo "\nGoose Commands:"
	@echo "\nGoose Migration Commands:"
	@echo "\t goose_create \t Create a new migration"
	@echo "\t goose_up \t Run all available migrations"
	@echo "\t goose_up_by_one \t Run all available migrations by one"
	@echo "\t goose_down \t Rollback the most recent migration"
	@echo "\t goose_status \t Show the status of all migrations"
	@echo "\t goose_fix \t Fix the last migration"
	@echo "\t goose_redo \t Rollback and re-run the most recent migration"
	@echo "\t goose_reset \t Rollback all migrations"
	@echo "\t goose_clean \t Remove all migrations"

# Goosee - Create a new migration
goose_create:
	@echo "Creating a new migration"
	${GOOSE} -dir ${GOOSE_PATH_SCHEMA} create $(NAME) sql
	@echo "Migration created"

# Goose - Migrate the DB up by 1 
goose_up_by_one:
	@echo "Migrate the DB up by 1"
	goose -dir ${GOOSE_PATH_SCHEMA} ${GOOSE_DRIVER} $(GOOSE_DB_DSN) up-by-one
	@echo "Migrations completed"

# Goose - Run all available migrations
goose_up:
	@echo "Running all available migrations"
	goose -dir ${GOOSE_PATH_SCHEMA} ${GOOSE_DRIVER} $(GOOSE_DB_DSN) up
	@echo "Migrations completed"

# Goose - Rollback the most recent migration
goose_down:
	@echo "Rolling back the most recent migration"
	goose -dir ${GOOSE_PATH_SCHEMA} ${GOOSE_DRIVER} $(GOOSE_DB_DSN) down
	@echo "Migration rolled back"

# Goose - Show the status of all migrations
goose_status:
	@echo "Showing the status of all migrations"
	goose -dir ${GOOSE_PATH_SCHEMA} ${GOOSE_DRIVER} $(GOOSE_DB_DSN) status
	@echo "Migration status displayed"

# Goose - Fix the last migration
goose_fix:
	@echo "Fixing the last migration"
	goose -dir ${GOOSE_PATH_SCHEMA} ${GOOSE_DRIVER} $(GOOSE_DB_DSN) fix
	@echo "Migration fixed"

# Goose - Rollback and re-run the most recent migration
goose_redo:
	@echo "Rolling back and re-running the most recent migration"
	goose -dir ${GOOSE_PATH_SCHEMA} ${GOOSE_DRIVER} $(GOOSE_DB_DSN) redo
	@echo "Migration redone"

# Goose - Rollback all migrations
goose_reset:
	@echo "Rolling back all migrations"
	goose -dir ${GOOSE_PATH_SCHEMA} ${GOOSE_DRIVER} $(GOOSE_DB_DSN) reset
	@echo "Migrations reset"

# Goose - Remove all migrations
goose_clean:
	@echo "Removing all migrations"
	goose -dir ${GOOSE_PATH_SCHEMA} ${GOOSE_DRIVER} $(GOOSE_DB_DSN) clean
	@echo "Migrations removed"

# Run Server
run_server:
	@echo "Running Server"
	$(GO) run $(SERVER_MAIN)

# SQLC - Generate
sqlc_generate:
	@echo "Generating SQLC queries"
	sqlc -f ./environment/sqlc.yaml generate
	@echo "SQLC queries generated"

# Create Environment Variables
cre_env:
	@echo "Creating .env from .yaml"
	rm -rf .env
	$(GO) run cmd/cli/viper/main.vipper.go
	@echo ".env file created"

# Docker Compose
docker_run:
	@echo "Running Docker Compose"
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

# Swagger
swagger_generate:
	@echo "Generating Swagger Documentation"
	swag init -g ./cmd/server/main.go -o ./cmd/swag/docs
	@echo "Swagger Documentation generated"