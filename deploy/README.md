# Production Deployment

Thư mục này chứa các file phục vụ triển khai production cho Digital HRM:

- `../docker-compose.prod.yml`: stack production gồm SQL Server, backend, frontend và job khởi tạo DB.
- `mssql/init-db.sh`: script đợi SQL Server sẵn sàng rồi chạy `mssql/create_database.sql`.
- `mssql/create_database.sql`: script tạo database `DigitalHRM` trước khi backend chạy Flyway.
- `nginx/digital-hrm.conf.example`: cấu hình Nginx trên server để reverse proxy HTTPS vào container frontend đang bind tại `127.0.0.1:8088`.

Tài liệu chi tiết:

- `SERVER_SETUP.md`: cấu hình server Ubuntu, Docker, Nginx, SSL và file env.
- `GITHUB_SETUP.md`: cấu hình GitHub Actions secrets/variables và cách workflow hoạt động.
