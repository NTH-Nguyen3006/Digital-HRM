# Hướng dẫn cấu hình GitHub Actions

## 1. Workflow trong repo

Repo này có 2 workflow:

- `.github/workflows/ci.yml`: chạy kiểm tra backend/frontend cho pull request và push.
- `.github/workflows/ci-cd.yml`: build Docker image, push lên GHCR rồi deploy sang server qua SSH.

## 2. Branch kích hoạt deploy

Workflow deploy đang chạy khi:

- push lên `main`
- push lên `production`
- chạy tay bằng `workflow_dispatch`

Bạn có thể đổi branch trong file workflow nếu team dùng nhánh khác.

## 3. GitHub Secrets cần tạo

Vào `GitHub Repository -> Settings -> Secrets and variables -> Actions -> New repository secret`.

Tạo các secret sau:

- `SERVER_HOST`: IP hoặc domain SSH của server.
- `SERVER_PORT`: thường là `22`.
- `SERVER_USER`: user SSH dùng để deploy.
- `SSH_PRIVATE_KEY`: private key tương ứng public key đã thêm vào `~/.ssh/authorized_keys` trên server.
- `DEPLOY_PATH`: ví dụ `/opt/digital-hrm`.
- `GHCR_READ_USERNAME`: GitHub username có quyền `read:packages`.
- `GHCR_READ_TOKEN`: Personal Access Token có tối thiểu quyền `read:packages`.

## 4. Quyền package trên GHCR

Workflow build sử dụng `GITHUB_TOKEN` để push image lên GHCR của repo hiện tại.

Để server kéo image private, cần:

- PAT của tài khoản có quyền đọc package.
- Package visibility đúng với nhu cầu của bạn.

Nếu bạn chuyển package sang public, có thể bỏ bước login GHCR trong workflow deploy.

## 5. Quy trình deploy đang hoạt động thế nào

1. Checkout mã nguồn.
2. Chạy test backend và build frontend.
3. Build 2 image:
   - `ghcr.io/<owner>/digital-hrm-backend:sha-<git-sha>`
   - `ghcr.io/<owner>/digital-hrm-frontend:sha-<git-sha>`
4. Đồng thời gắn thêm tag `latest`.
5. Copy `docker-compose.prod.yml`, `.env.production.example` và thư mục `deploy/` lên server.
6. SSH vào server, tạo file `.env.images` chứa image tag mới nhất.
7. Chạy:
   - `docker compose pull`
   - `docker compose up -d --remove-orphans`
8. Poll `http://127.0.0.1:8088/` để xác nhận frontend đã lên.

## 6. Public key cho server

Nếu chưa có key:

```bash
ssh-keygen -t ed25519 -C "github-actions-digital-hrm"
```

Thêm public key vào server:

```bash
mkdir -p ~/.ssh
chmod 700 ~/.ssh
cat github-actions-digital-hrm.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
```

Private key tương ứng đưa vào secret `SSH_PRIVATE_KEY`.

## 7. Cách chạy deploy thủ công

Vào tab `Actions` của repo:

1. Chọn workflow `Deploy Digital HRM`.
2. Chọn `Run workflow`.
3. Chọn branch cần deploy.

## 8. Lưu ý vận hành

- Không commit `.env.production` vào repo.
- Đổi `APP_JWT_SECRET` và mật khẩu admin trước khi mở public.
- Lần đầu deploy có thể mất lâu hơn vì SQL Server cần khởi tạo volume dữ liệu và Flyway seed nhiều bản ghi.
- Nếu seed thay đổi checksum giữa các phiên bản, nên xử lý migration cẩn thận trước khi deploy vào dữ liệu thật.
