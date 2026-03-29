DC = docker compose
BE_DIR = backend
FE_DIR = frontend

.PHONY: up down restart logs ps dev-be dev-fe build clean migrate

up:
	$(DC) up -d --build

down:
	$(DC) down

restart:
	$(DC) restart

logs:
	$(DC) logs -f

ps:
	$(DC) ps

test-be:
	cd $(BE_DIR) && ./mvnw test

dev-be:
	cd $(BE_DIR) && ./mvnw spring-boot:run

dev-fe:
	cd $(FE_DIR) && npm run dev

build:
	cd $(BE_DIR) && ./mvnw clean package -DskipTests
	cd $(FE_DIR) && npm install && npm run build

migrate:
	cd $(BE_DIR) && ./mvnw flyway:migrate

clean:
	cd $(BE_DIR) && ./mvnw clean
	rm -rf $(FE_DIR)/dist
	rm -rf $(FE_DIR)/node_modules
