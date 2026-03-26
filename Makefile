DOCKER_COMPOSE = docker compose

help:
	@echo "Cách dùng: make [lệnh]"
	@echo ""
	@echo "Hệ thống:"
	@echo "  up         - Khởi chạy toàn bộ services (chạy ngầm)"
	@echo "  down       - Dừng và xóa các container"
	@echo "  restart    - Khởi động lại toàn bộ services"
	@echo "  build      - Build lại các image (frontend, backend)"
	@echo "  ps         - Danh sách các container đang chạy"
	@echo "  logs       - Xem log của tất cả services"
	@echo ""
	@echo "Từng Service:"
	@echo "  db         - Chạy Database (MS SQL Server)"
	@echo "  be         - Chạy Backend (Spring Boot)"
	@echo "  fe         - Chạy Frontend (Vue/Nginx)"
	@echo ""
	@echo "Log lẻ:"
	@echo "  logs-db    - Xem log Database"
	@echo "  logs-be    - Xem log Backend"
	@echo "  logs-fe    - Xem log Frontend"
	@echo ""
	@echo "Tiện ích:"
	@echo "  clean      - Dọn dẹp container/network rác"
	@echo "  db-shell   - Truy cập sqlcmd của database"

up:
	$(DOCKER_COMPOSE) up -d

down:
	$(DOCKER_COMPOSE) down

restart:
	$(DOCKER_COMPOSE) down
	$(DOCKER_COMPOSE) up -d

build:
	$(DOCKER_COMPOSE) build

ps:
	$(DOCKER_COMPOSE) ps

logs:
	$(DOCKER_COMPOSE) logs -f

db:
	$(DOCKER_COMPOSE) up -d db

be:
	$(DOCKER_COMPOSE) up -d backend

fe:
	$(DOCKER_COMPOSE) up -d frontend

logs-db:
	$(DOCKER_COMPOSE) logs -f db

logs-be:
	$(DOCKER_COMPOSE) logs -f backend

logs-fe:
	$(DOCKER_COMPOSE) logs -f frontend

clean:
	docker system prune -f

db-shell:
	@echo "Đang kết nối vào database..."
	$(DOCKER_COMPOSE) exec db /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P $$(grep DATABASE_PASSWORD .env | cut -d '=' -f2)
