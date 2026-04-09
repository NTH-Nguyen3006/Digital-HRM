# Hướng dẫn cấu hình server

## 1. Chuẩn bị máy chủ

Khuyến nghị tối thiểu:

- Ubuntu 22.04 LTS
- 2 vCPU
- 4 GB RAM
- 40 GB SSD
- Domain trỏ về IP public của server

## 2. Cài Docker và Docker Compose

```bash
sudo apt update
sudo apt install -y ca-certificates curl gnupg nginx certbot python3-certbot-nginx
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo \"$VERSION_CODENAME\") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker $USER
```

Đăng xuất rồi đăng nhập lại để user hiện tại dùng được Docker không cần `sudo`.

## 3. Tạo thư mục triển khai

```bash
sudo mkdir -p /opt/digital-hrm
sudo chown -R $USER:$USER /opt/digital-hrm
mkdir -p /opt/digital-hrm
```

Workflow GitHub sẽ copy `docker-compose.prod.yml` và thư mục `deploy/` vào đây.

## 4. Tạo file biến môi trường production

Tạo file `/opt/digital-hrm/.env.production` dựa trên mẫu `.env.production.example`.

```bash
cp /opt/digital-hrm/.env.production.example /opt/digital-hrm/.env.production
nano /opt/digital-hrm/.env.production
```

Giá trị quan trọng cần thay:

- `BACKEND_IMAGE`, `FRONTEND_IMAGE`: có thể để placeholder, workflow sẽ ghi đè bằng tag SHA mới nhất lúc deploy.
- `MSSQL_SA_PASSWORD`: mật khẩu mạnh cho tài khoản `sa`.
- `APP_JWT_SECRET`: chuỗi dài, ngẫu nhiên, khó đoán.
- `APP_CORS_ALLOWED_ORIGINS`: ví dụ `https://hrm.example.com`.
- `APP_COOKIE_DOMAIN`: ví dụ `hrm.example.com`.
- `APP_BOOTSTRAP_ADMIN_EMAIL`, `APP_BOOTSTRAP_ADMIN_PASSWORD`: tài khoản admin khởi tạo ban đầu.
- `APP_MAIL_*`: nếu chưa có SMTP thật thì để `APP_MAIL_MOCK_ENABLED=true`.

Lưu ý:

- Backend image đã đóng gói sẵn thư mục `database/schema` và `database/seed`, nên khi container khởi động Flyway sẽ tự migrate/seed dữ liệu.
- Service `db-init` chỉ có nhiệm vụ tạo database `DigitalHRM` từ `deploy/mssql/create_database.sql` trước khi backend start.
- Dữ liệu SQL Server và file upload được giữ qua volume Docker, nên redeploy không làm mất dữ liệu.

## 5. Cấu hình Nginx trên server

Copy file mẫu:

```bash
sudo cp /opt/digital-hrm/deploy/nginx/digital-hrm.conf.example /etc/nginx/sites-available/digital-hrm
sudo nano /etc/nginx/sites-available/digital-hrm
```

Thay các giá trị:

- `hrm.example.com` bằng domain thật.
- Giữ `proxy_pass http://127.0.0.1:8088;` nếu `FRONTEND_BIND_PORT=8088`.

Kích hoạt site:

```bash
sudo ln -s /etc/nginx/sites-available/digital-hrm /etc/nginx/sites-enabled/digital-hrm
sudo nginx -t
sudo systemctl reload nginx
```

## 6. Cấp SSL bằng Let's Encrypt

```bash
sudo certbot --nginx -d hrm.example.com
```

Sau khi certbot chạy xong, kiểm tra lại:

```bash
sudo nginx -t
sudo systemctl reload nginx
```

## 7. Chuẩn bị quyền kéo image từ GHCR

Nếu package để private, trên server cần login GHCR:

```bash
echo "YOUR_GHCR_READ_TOKEN" | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin
```

Workflow deploy cũng sẽ thực hiện bước này bằng secrets GitHub.

## 8. Kiểm tra sau deploy

```bash
cd /opt/digital-hrm
docker compose --env-file .env.production --env-file .env.images -f docker-compose.prod.yml ps
docker compose --env-file .env.production --env-file .env.images -f docker-compose.prod.yml logs backend --tail=200
docker compose --env-file .env.production --env-file .env.images -f docker-compose.prod.yml logs db-init
```

Bạn cần thấy:

- `db` healthy
- `db-init` exit code `0`
- `backend` chạy bình thường và Flyway migrate/seed thành công
- `frontend` healthy

## 9. Lần deploy đầu tiên

Sau khi GitHub Actions chạy thành công, truy cập domain:

- Frontend: `https://hrm.example.com`
- Health check: `https://hrm.example.com/api/actuator/health`

Tài khoản mặc định sẽ là giá trị trong:

- `APP_BOOTSTRAP_ADMIN_USERNAME`
- `APP_BOOTSTRAP_ADMIN_PASSWORD`
