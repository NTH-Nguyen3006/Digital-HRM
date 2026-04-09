# ATTENDANCE IP PHASE

## 1. Mục tiêu phase
Triển khai kiểm soát tự chấm công theo IP mạng hợp lệ và siết rule check-in/check-out theo hướng production:
- Nhân viên chỉ self check-in/check-out thành công khi IP hiện tại thuộc whitelist hợp lệ.
- Admin có nơi cấu hình policy IP hợp lệ cho chấm công.
- Không cho self check-in/check-out trùng nhiều lần trong cùng ngày công.
- Không khóa dây chuyền khi nhân viên quên check-in hoặc quên check-out.
- Adjustment request đi theo luồng: manager trực tiếp duyệt trước, HR duyệt sau.

## 2. Scope đã làm
### 2.1. Check IP chấm công
- Thêm policy IP riêng cho attendance, không nhét vào setting key-value.
- Backend tự resolve IP từ request context, không tin IP từ request body.
- Cho phép cấu hình nhiều IP/CIDR cho một policy.
- Policy áp dụng theo `GLOBAL` hoặc `ORG_UNIT`.
- Khi self check-in/check-out:
  - IP hợp lệ -> cho chấm công.
  - IP không hợp lệ -> chặn `403`, không tạo attendance log.

### 2.2. Rule self check-in/check-out
- Chỉ cho self chấm công khi employee ở trạng thái `ACTIVE` hoặc `PROBATION`.
- Không cho tự chấm công trước `hireDate`.
- Chỉ cho chấm công cho hôm nay hoặc hôm trước.
- Không cho check-in 2 lần trong cùng ngày công.
- Không cho check-out 2 lần trong cùng ngày công.
- Giữ duplicate guard 3 phút để chống spam request.
- Thêm time window theo ca:
  - Check-in: từ `plannedStart - 120 phút` đến `plannedStart + 240 phút`.
  - Check-out: từ `plannedEnd - 240 phút` đến `plannedEnd + 360 phút`.
- Ngoài khung giờ self-service -> báo dùng adjustment request.

### 2.3. Xử lý quên bấm
- Quên check-in:
  - vẫn cho check-out.
  - ngày công bị đánh dấu thiếu check-in / incomplete.
  - nhân viên gửi adjustment request để bổ sung.
- Quên check-out:
  - hôm sau vẫn cho check-in bình thường.
  - ngày hôm trước bị đánh dấu thiếu check-out / incomplete.
  - nhân viên gửi adjustment request để bổ sung.
- Không dùng chấm công hộ thủ công làm luồng chính.

### 2.4. Adjustment request approval flow
- Manager trực tiếp của nhân viên duyệt trước.
- HR duyệt sau / finalize sau khi manager đã approve.
- Manager scope được siết theo `employee.managerEmployee`, không chỉ theo org tree rộng.

## 3. Scope chưa làm trong phase này
- Không support SSID Wi-Fi / tên Wi-Fi. Chỉ dùng IP/CIDR hợp lệ.
- Chưa có geofence GPS làm lớp điều kiện bổ sung.
- Chưa có bypass policy riêng cho business trip / client site.
- Chưa có UI frontend, mới là backend/API.

## 4. Database migration
### 4.1. Migration mới
- `database/schema/V015__attendance_network_policy.sql`

### 4.2. Bảng mới
#### `att_network_policy`
- `network_policy_id`
- `policy_code`
- `policy_name`
- `scope_type` (`GLOBAL`, `ORG_UNIT`)
- `org_unit_id` nullable
- `allow_check_in`
- `allow_check_out`
- `effective_from`
- `effective_to`
- `status`
- `description`
- audit fields
- soft delete
- row version

#### `att_network_policy_ip`
- `network_policy_ip_id`
- `network_policy_id`
- `ip_version`
- `cidr_or_ip`
- `description`
- `status`
- audit fields
- soft delete
- row version

### 4.3. Alter bảng hiện có
#### `att_attendance_log`
Thêm:
- `client_ip`
- `forwarded_for_raw`
- `network_policy_id`
- `network_validation_status`
- `network_validation_message`

## 5. Backend architecture / logic chính
### 5.1. Service mới
- `AttendanceNetworkPolicyService`
  - tìm policy active phù hợp cho employee
  - match IP với whitelist
  - trả kết quả validate

### 5.2. Controller mới
- `AdminAttendanceNetworkController`
  - CRUD policy
  - quản lý IP range
  - bật/tắt policy

### 5.3. Service sửa
- `AttendanceService#createSelfLog(...)`
  - validate IP trước khi save log
  - validate rule check-in/check-out trong ngày
  - validate self-service window
  - throw lỗi nếu không hợp lệ

### 5.4. Utility sửa
- `RequestContextUtils`
  - lấy client IP từ request
  - giữ raw `X-Forwarded-For`
  - fallback `remoteAddr`

## 6. File tạo mới / sửa chính
### 6.1. Tạo mới
- `database/schema/V015__attendance_network_policy.sql`
- `database/seed/R__sprint5_attendance_network_permissions.sql`
- `src/main/java/com/company/hrm/common/constant/AttendanceNetworkPolicyScopeType.java`
- `src/main/java/com/company/hrm/common/constant/AttendanceNetworkValidationStatus.java`
- `src/main/java/com/company/hrm/module/attendance/controller/AdminAttendanceNetworkController.java`
- `src/main/java/com/company/hrm/module/attendance/dto/AttendanceNetworkPolicyDetailResponse.java`
- `src/main/java/com/company/hrm/module/attendance/dto/AttendanceNetworkPolicyIpResponse.java`
- `src/main/java/com/company/hrm/module/attendance/dto/AttendanceNetworkPolicyListItemResponse.java`
- `src/main/java/com/company/hrm/module/attendance/dto/UpsertAttendanceNetworkPolicyRequest.java`
- `src/main/java/com/company/hrm/module/attendance/dto/UpsertAttendanceNetworkPolicyIpRequest.java`
- `src/main/java/com/company/hrm/module/attendance/dto/UpdateAttendanceNetworkPolicyStatusRequest.java`
- `src/main/java/com/company/hrm/module/attendance/entity/AttNetworkPolicy.java`
- `src/main/java/com/company/hrm/module/attendance/entity/AttNetworkPolicyIp.java`
- `src/main/java/com/company/hrm/module/attendance/repository/AttNetworkPolicyRepository.java`
- `src/main/java/com/company/hrm/module/attendance/repository/AttNetworkPolicyIpRepository.java`
- `src/main/java/com/company/hrm/module/attendance/service/AttendanceNetworkPolicyService.java`

### 6.2. Sửa
- `src/main/java/com/company/hrm/common/util/RequestContextUtils.java`
- `src/main/java/com/company/hrm/module/attendance/dto/AttendanceLogResponse.java`
- `src/main/java/com/company/hrm/module/attendance/entity/AttAttendanceLog.java`
- `src/main/java/com/company/hrm/module/attendance/repository/AttAttendanceAdjustmentRequestRepository.java`
- `src/main/java/com/company/hrm/module/attendance/repository/AttAttendanceLogRepository.java`
- `src/main/java/com/company/hrm/module/attendance/repository/AttOvertimeRequestRepository.java`
- `src/main/java/com/company/hrm/module/attendance/service/AttendanceAccessScopeService.java`
- `src/main/java/com/company/hrm/module/attendance/service/AttendanceService.java`

## 7. Endpoint
### 7.1. Self attendance hiện có được siết thêm rule
- `POST /api/v1/me/attendance/check-in`
- `POST /api/v1/me/attendance/check-out`
- `GET /api/v1/me/attendance/logs`

### 7.2. Admin network policy
- `GET /api/v1/admin/attendance/network-policies`
- `GET /api/v1/admin/attendance/network-policies/{networkPolicyId}`
- `POST /api/v1/admin/attendance/network-policies`
- `PUT /api/v1/admin/attendance/network-policies/{networkPolicyId}`
- `PATCH /api/v1/admin/attendance/network-policies/{networkPolicyId}/status`
- `POST /api/v1/admin/attendance/network-policies/{networkPolicyId}/ip-ranges`
- `PUT /api/v1/admin/attendance/network-policies/{networkPolicyId}/ip-ranges/{networkPolicyIpId}`
- `DELETE /api/v1/admin/attendance/network-policies/{networkPolicyId}/ip-ranges/{networkPolicyIpId}`

## 8. Request/response mẫu
### 8.1. Tạo policy
```json
{
  "policyCode": "OFFICE-HCM-HQ",
  "policyName": "WiFi văn phòng HCM",
  "scopeType": "GLOBAL",
  "allowCheckIn": true,
  "allowCheckOut": true,
  "effectiveFrom": "2026-04-09",
  "description": "Policy IP chấm công văn phòng chính",
  "status": "ACTIVE"
}
```

### 8.2. Thêm IP range
```json
{
  "cidrOrIp": "113.161.10.0/24",
  "description": "Public IP wifi văn phòng",
  "status": "ACTIVE"
}
```

### 8.3. Self check-in thất bại do IP sai
```json
{
  "success": false,
  "code": "ATTENDANCE_IP_NOT_ALLOWED",
  "message": "IP mạng hiện tại không nằm trong danh sách được phép chấm công."
}
```

## 9. Permission
Thêm:
- `attendance.network_policy.view`
- `attendance.network_policy.manage`

Gán:
- `ADMIN`: view + manage
- `HR`: có thể mở rộng view nếu muốn, nhưng mặc định quản lý chính là ADMIN

## 10. Cách test
### Case 1. IP đúng check-in thành công
- Tạo policy active
- Thêm IP/CIDR chứa IP hiện tại
- Gọi `POST /api/v1/me/attendance/check-in`
- Kỳ vọng: success, log được tạo

### Case 2. IP sai check-in thất bại
- Xóa IP hợp lệ hoặc dùng IP ngoài whitelist
- Gọi `POST /api/v1/me/attendance/check-in`
- Kỳ vọng: `403`, code `ATTENDANCE_IP_NOT_ALLOWED`

### Case 3. Check-in trùng trong ngày
- Check-in thành công lần 1
- Check-in lại cùng ngày công
- Kỳ vọng: fail do đã có check-in

### Case 4. Check-out trùng trong ngày
- Check-out thành công lần 1
- Check-out lại cùng ngày công
- Kỳ vọng: fail do đã có check-out

### Case 5. Quên check-in nhưng vẫn check-out được
- Không check-in
- Gọi check-out trong window hợp lệ
- Kỳ vọng: success, ngày công đánh dấu incomplete / missing check-in

### Case 6. Quên check-out hôm trước nhưng hôm sau vẫn check-in được
- Hôm trước không check-out
- Hôm sau check-in bình thường
- Kỳ vọng: success, ngày trước incomplete / ngày mới không bị khóa

### Case 7. Adjustment pending scope
- Tạo adjustment request của employee A
- Login manager trực tiếp của A
- Kỳ vọng: manager thấy request cần duyệt
- Manager không trực tiếp -> không thấy / không duyệt được

## 11. Điều kiện freeze phase
Freeze khi đạt:
- CRUD policy IP chạy đúng
- Self check-in/check-out có validate IP
- Không cho check-in/check-out trùng trong ngày
- Quên check-in/quên check-out không khóa dây chuyền
- Manager approval scope đi theo manager trực tiếp
- Migration chạy sạch
- FE có thể bám API để làm màn policy + self attendance

## 12. Hạ tầng cần lưu ý
- Nếu app chạy sau reverse proxy/load balancer, phải cấu hình forward headers/trusted proxy đúng.
- Nếu không, client IP có thể bị sai hoặc bị spoof.
- Phase này chỉ dựa trên IP/CIDR, chưa kiểm theo SSID Wi-Fi.

## 13. Commit message gợi ý
```bash
feat(attendance): add self-attendance IP whitelist policy and direct-manager approval scope
```

## 14. Link artifact source đã patch
- `hrm-backend-attendance-ip-patch.zip`

## 15. Hướng mở rộng sau phase này
- Business trip approved -> bypass IP theo policy riêng
- Client site / remote policy
- Geofence GPS bổ sung
- UI highlight incomplete attendance + shortcut tạo adjustment request
