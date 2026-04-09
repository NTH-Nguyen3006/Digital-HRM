package com.company.hrm.module.attendance.service;

import com.company.hrm.common.constant.AttendanceLogEventType;
import com.company.hrm.common.constant.AttendanceNetworkPolicyScopeType;
import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.exception.BusinessException;
import com.company.hrm.common.exception.NotFoundException;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.common.util.RequestContextUtils;
import com.company.hrm.module.attendance.dto.*;
import com.company.hrm.module.attendance.entity.AttNetworkPolicy;
import com.company.hrm.module.attendance.entity.AttNetworkPolicyIp;
import com.company.hrm.module.attendance.repository.AttNetworkPolicyIpRepository;
import com.company.hrm.module.attendance.repository.AttNetworkPolicyRepository;
import com.company.hrm.module.orgunit.entity.HrOrgUnit;
import com.company.hrm.module.orgunit.repository.HrOrgUnitRepository;
import com.company.hrm.module.employee.entity.HrEmployee;
import java.math.BigInteger;
import java.net.InetAddress;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceNetworkPolicyService {

    private final AttNetworkPolicyRepository networkPolicyRepository;
    private final AttNetworkPolicyIpRepository networkPolicyIpRepository;
    private final HrOrgUnitRepository orgUnitRepository;

    public AttendanceNetworkPolicyService(
            AttNetworkPolicyRepository networkPolicyRepository,
            AttNetworkPolicyIpRepository networkPolicyIpRepository,
            HrOrgUnitRepository orgUnitRepository
    ) {
        this.networkPolicyRepository = networkPolicyRepository;
        this.networkPolicyIpRepository = networkPolicyIpRepository;
        this.orgUnitRepository = orgUnitRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<AttendanceNetworkPolicyListItemResponse> listPolicies(String keyword, RecordStatus status, Long orgUnitId, int page, int size) {
        Specification<AttNetworkPolicy> specification = (root, query, builder) -> builder.isFalse(root.get("deleted"));
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("policyCode")), like),
                    builder.like(builder.lower(root.get("policyName")), like)
            ));
        }
        if (status != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("status"), status));
        }
        if (orgUnitId != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.join("orgUnit").get("orgUnitId"), orgUnitId));
        }
        Page<AttNetworkPolicy> result = networkPolicyRepository.findAll(
                specification,
                PageRequest.of(page, size, Sort.by("scopeType").ascending().and(Sort.by("policyCode").ascending()))
        );
        List<AttendanceNetworkPolicyListItemResponse> items = result.getContent().stream()
                .map(this::toPolicyListItem)
                .toList();
        return new PageResponse<>(items, page, size, result.getTotalElements(), result.getTotalPages(), result.hasNext(), result.hasPrevious());
    }

    @Transactional(readOnly = true)
    public AttendanceNetworkPolicyDetailResponse getPolicyDetail(Long networkPolicyId) {
        return toPolicyDetail(getPolicy(networkPolicyId));
    }

    @Transactional(readOnly = true)
    public AttNetworkPolicy getPolicyReference(Long networkPolicyId) {
        return getPolicy(networkPolicyId);
    }

    @Transactional
    public AttendanceNetworkPolicyDetailResponse createPolicy(UpsertAttendanceNetworkPolicyRequest request) {
        validatePolicyRequest(null, request);
        if (networkPolicyRepository.existsByPolicyCodeIgnoreCaseAndDeletedFalse(request.policyCode().trim())) {
            throw new BusinessException("ATTENDANCE_NETWORK_POLICY_CODE_EXISTS", "Mã policy IP chấm công đã tồn tại.", HttpStatus.CONFLICT);
        }
        AttNetworkPolicy entity = new AttNetworkPolicy();
        applyPolicy(entity, request);
        entity = networkPolicyRepository.save(entity);
        return toPolicyDetail(entity);
    }

    @Transactional
    public AttendanceNetworkPolicyDetailResponse updatePolicy(Long networkPolicyId, UpsertAttendanceNetworkPolicyRequest request) {
        validatePolicyRequest(networkPolicyId, request);
        if (networkPolicyRepository.existsByPolicyCodeIgnoreCaseAndDeletedFalseAndNetworkPolicyIdNot(request.policyCode().trim(), networkPolicyId)) {
            throw new BusinessException("ATTENDANCE_NETWORK_POLICY_CODE_EXISTS", "Mã policy IP chấm công đã tồn tại.", HttpStatus.CONFLICT);
        }
        AttNetworkPolicy entity = getPolicy(networkPolicyId);
        applyPolicy(entity, request);
        entity = networkPolicyRepository.save(entity);
        return toPolicyDetail(entity);
    }

    @Transactional
    public AttendanceNetworkPolicyDetailResponse updatePolicyStatus(Long networkPolicyId, UpdateAttendanceNetworkPolicyStatusRequest request) {
        AttNetworkPolicy entity = getPolicy(networkPolicyId);
        entity.setStatus(request.status());
        entity = networkPolicyRepository.save(entity);
        return toPolicyDetail(entity);
    }

    @Transactional
    public AttendanceNetworkPolicyDetailResponse addIpRange(Long networkPolicyId, UpsertAttendanceNetworkPolicyIpRequest request) {
        AttNetworkPolicy policy = getPolicy(networkPolicyId);
        validateIpRange(request.cidrOrIp());
        AttNetworkPolicyIp entity = new AttNetworkPolicyIp();
        entity.setNetworkPolicy(policy);
        entity.setCidrOrIp(normalizeNetworkValue(request.cidrOrIp()));
        entity.setDescription(trimToNull(request.description()));
        entity.setStatus(request.status() == null ? RecordStatus.ACTIVE : request.status());
        networkPolicyIpRepository.save(entity);
        return toPolicyDetail(policy);
    }

    @Transactional
    public AttendanceNetworkPolicyDetailResponse updateIpRange(Long networkPolicyId, Long networkPolicyIpId, UpsertAttendanceNetworkPolicyIpRequest request) {
        validateIpRange(request.cidrOrIp());
        AttNetworkPolicy policy = getPolicy(networkPolicyId);
        AttNetworkPolicyIp entity = getPolicyIp(networkPolicyIpId);
        if (!entity.getNetworkPolicy().getNetworkPolicyId().equals(policy.getNetworkPolicyId())) {
            throw new BusinessException("ATTENDANCE_NETWORK_POLICY_IP_SCOPE_INVALID", "IP range không thuộc policy được chỉ định.", HttpStatus.BAD_REQUEST);
        }
        entity.setCidrOrIp(normalizeNetworkValue(request.cidrOrIp()));
        entity.setDescription(trimToNull(request.description()));
        entity.setStatus(request.status() == null ? RecordStatus.ACTIVE : request.status());
        networkPolicyIpRepository.save(entity);
        return toPolicyDetail(policy);
    }

    @Transactional
    public void deleteIpRange(Long networkPolicyId, Long networkPolicyIpId) {
        AttNetworkPolicy policy = getPolicy(networkPolicyId);
        AttNetworkPolicyIp entity = getPolicyIp(networkPolicyIpId);
        if (!entity.getNetworkPolicy().getNetworkPolicyId().equals(policy.getNetworkPolicyId())) {
            throw new BusinessException("ATTENDANCE_NETWORK_POLICY_IP_SCOPE_INVALID", "IP range không thuộc policy được chỉ định.", HttpStatus.BAD_REQUEST);
        }
        entity.setDeleted(true);
        networkPolicyIpRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public AttendanceNetworkValidationResult validateSelfAttendanceNetwork(HrEmployee employee, AttendanceLogEventType eventType, LocalDate attendanceDate) {
        String clientIp = RequestContextUtils.getClientIp();
        String forwardedForRaw = RequestContextUtils.getForwardedForRaw();
        if (clientIp == null || clientIp.isBlank()) {
            return AttendanceNetworkValidationResult.failed(null, forwardedForRaw, null, null,
                    "Không xác định được IP hiện tại của request chấm công.");
        }

        List<AttNetworkPolicy> candidatePolicies = findCandidatePolicies(employee, attendanceDate);
        for (AttNetworkPolicy policy : candidatePolicies) {
            if (eventType == AttendanceLogEventType.CHECK_IN && !policy.isAllowCheckIn()) {
                continue;
            }
            if (eventType == AttendanceLogEventType.CHECK_OUT && !policy.isAllowCheckOut()) {
                continue;
            }
            List<AttNetworkPolicyIp> ipRanges = networkPolicyIpRepository.findAllByNetworkPolicyNetworkPolicyIdAndStatusAndDeletedFalseOrderByNetworkPolicyIpIdAsc(
                    policy.getNetworkPolicyId(), RecordStatus.ACTIVE);
            for (AttNetworkPolicyIp ipRange : ipRanges) {
                if (matches(clientIp, ipRange.getCidrOrIp())) {
                    return AttendanceNetworkValidationResult.passed(clientIp, forwardedForRaw, policy.getNetworkPolicyId(), policy.getPolicyCode(), ipRange.getCidrOrIp());
                }
            }
        }

        return AttendanceNetworkValidationResult.failed(clientIp, forwardedForRaw, null, null,
                "IP mạng hiện tại không nằm trong danh sách được phép chấm công.");
    }

    private List<AttNetworkPolicy> findCandidatePolicies(HrEmployee employee, LocalDate attendanceDate) {
        Long orgUnitId = employee.getOrgUnit() == null ? null : employee.getOrgUnit().getOrgUnitId();
        Specification<AttNetworkPolicy> specification = (root, query, builder) -> builder.and(
                builder.isFalse(root.get("deleted")),
                builder.equal(root.get("status"), RecordStatus.ACTIVE),
                builder.or(
                        builder.isNull(root.get("effectiveFrom")),
                        builder.lessThanOrEqualTo(root.get("effectiveFrom"), attendanceDate)
                ),
                builder.or(
                        builder.isNull(root.get("effectiveTo")),
                        builder.greaterThanOrEqualTo(root.get("effectiveTo"), attendanceDate)
                )
        );
        if (orgUnitId == null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("scopeType"), AttendanceNetworkPolicyScopeType.GLOBAL));
        } else {
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.equal(root.get("scopeType"), AttendanceNetworkPolicyScopeType.GLOBAL),
                    builder.and(
                            builder.equal(root.get("scopeType"), AttendanceNetworkPolicyScopeType.ORG_UNIT),
                            builder.equal(root.join("orgUnit").get("orgUnitId"), orgUnitId)
                    )
            ));
        }
        List<AttNetworkPolicy> policies = networkPolicyRepository.findAll(specification, Sort.by("scopeType").ascending().and(Sort.by("networkPolicyId").ascending()));
        return policies.stream()
                .sorted(Comparator.comparing((AttNetworkPolicy p) -> p.getScopeType() == AttendanceNetworkPolicyScopeType.ORG_UNIT ? 0 : 1)
                        .thenComparing(AttNetworkPolicy::getNetworkPolicyId))
                .toList();
    }

    private void validatePolicyRequest(Long networkPolicyId, UpsertAttendanceNetworkPolicyRequest request) {
        if (request.effectiveFrom() != null && request.effectiveTo() != null && request.effectiveTo().isBefore(request.effectiveFrom())) {
            throw new BusinessException("ATTENDANCE_NETWORK_POLICY_EFFECTIVE_INVALID", "effectiveTo không được nhỏ hơn effectiveFrom.", HttpStatus.BAD_REQUEST);
        }
        if (request.scopeType() == AttendanceNetworkPolicyScopeType.ORG_UNIT && request.orgUnitId() == null) {
            throw new BusinessException("ATTENDANCE_NETWORK_POLICY_ORG_UNIT_REQUIRED", "Policy scope ORG_UNIT bắt buộc có orgUnitId.", HttpStatus.BAD_REQUEST);
        }
        if (request.scopeType() == AttendanceNetworkPolicyScopeType.GLOBAL && request.orgUnitId() != null) {
            throw new BusinessException("ATTENDANCE_NETWORK_POLICY_GLOBAL_ORG_UNIT_INVALID", "Policy GLOBAL không được gắn orgUnitId.", HttpStatus.BAD_REQUEST);
        }
        if (Boolean.FALSE.equals(request.allowCheckIn()) && Boolean.FALSE.equals(request.allowCheckOut())) {
            throw new BusinessException("ATTENDANCE_NETWORK_POLICY_ACTION_REQUIRED", "Policy phải cho phép ít nhất check-in hoặc check-out.", HttpStatus.BAD_REQUEST);
        }
        if (networkPolicyId != null && request.scopeType() == AttendanceNetworkPolicyScopeType.ORG_UNIT) {
            getOrgUnit(request.orgUnitId());
        }
        if (networkPolicyId == null && request.scopeType() == AttendanceNetworkPolicyScopeType.ORG_UNIT) {
            getOrgUnit(request.orgUnitId());
        }
    }

    private void applyPolicy(AttNetworkPolicy entity, UpsertAttendanceNetworkPolicyRequest request) {
        entity.setPolicyCode(request.policyCode().trim().toUpperCase(Locale.ROOT));
        entity.setPolicyName(request.policyName().trim());
        entity.setScopeType(request.scopeType());
        entity.setOrgUnit(request.scopeType() == AttendanceNetworkPolicyScopeType.ORG_UNIT ? getOrgUnit(request.orgUnitId()) : null);
        entity.setAllowCheckIn(request.allowCheckIn() == null || request.allowCheckIn());
        entity.setAllowCheckOut(request.allowCheckOut() == null || request.allowCheckOut());
        entity.setEffectiveFrom(request.effectiveFrom());
        entity.setEffectiveTo(request.effectiveTo());
        entity.setDescription(trimToNull(request.description()));
        entity.setStatus(request.status() == null ? RecordStatus.ACTIVE : request.status());
    }

    private AttNetworkPolicy getPolicy(Long networkPolicyId) {
        return networkPolicyRepository.findByNetworkPolicyIdAndDeletedFalse(networkPolicyId)
                .orElseThrow(() -> new NotFoundException("ATTENDANCE_NETWORK_POLICY_NOT_FOUND", "Không tìm thấy policy IP chấm công."));
    }

    private AttNetworkPolicyIp getPolicyIp(Long networkPolicyIpId) {
        return networkPolicyIpRepository.findByNetworkPolicyIpIdAndDeletedFalse(networkPolicyIpId)
                .orElseThrow(() -> new NotFoundException("ATTENDANCE_NETWORK_POLICY_IP_NOT_FOUND", "Không tìm thấy IP hợp lệ của policy chấm công."));
    }

    private HrOrgUnit getOrgUnit(Long orgUnitId) {
        return orgUnitRepository.findByOrgUnitIdAndDeletedFalse(orgUnitId)
                .orElseThrow(() -> new NotFoundException("ORG_UNIT_NOT_FOUND", "Không tìm thấy đơn vị tổ chức."));
    }

    private AttendanceNetworkPolicyListItemResponse toPolicyListItem(AttNetworkPolicy entity) {
        int ipRangeCount = networkPolicyIpRepository.findAllByNetworkPolicyNetworkPolicyIdAndDeletedFalseOrderByNetworkPolicyIpIdAsc(entity.getNetworkPolicyId()).size();
        return new AttendanceNetworkPolicyListItemResponse(
                entity.getNetworkPolicyId(),
                entity.getPolicyCode(),
                entity.getPolicyName(),
                entity.getScopeType().name(),
                entity.getOrgUnit() == null ? null : entity.getOrgUnit().getOrgUnitId(),
                entity.getOrgUnit() == null ? null : entity.getOrgUnit().getOrgUnitName(),
                entity.isAllowCheckIn(),
                entity.isAllowCheckOut(),
                entity.getEffectiveFrom(),
                entity.getEffectiveTo(),
                entity.getStatus().name(),
                ipRangeCount,
                entity.getDescription()
        );
    }

    private AttendanceNetworkPolicyDetailResponse toPolicyDetail(AttNetworkPolicy entity) {
        List<AttendanceNetworkPolicyIpResponse> ipRanges = networkPolicyIpRepository
                .findAllByNetworkPolicyNetworkPolicyIdAndDeletedFalseOrderByNetworkPolicyIpIdAsc(entity.getNetworkPolicyId())
                .stream()
                .map(this::toPolicyIpResponse)
                .toList();
        return new AttendanceNetworkPolicyDetailResponse(
                entity.getNetworkPolicyId(),
                entity.getPolicyCode(),
                entity.getPolicyName(),
                entity.getScopeType().name(),
                entity.getOrgUnit() == null ? null : entity.getOrgUnit().getOrgUnitId(),
                entity.getOrgUnit() == null ? null : entity.getOrgUnit().getOrgUnitCode(),
                entity.getOrgUnit() == null ? null : entity.getOrgUnit().getOrgUnitName(),
                entity.isAllowCheckIn(),
                entity.isAllowCheckOut(),
                entity.getEffectiveFrom(),
                entity.getEffectiveTo(),
                entity.getStatus().name(),
                entity.getDescription(),
                ipRanges,
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }

    private AttendanceNetworkPolicyIpResponse toPolicyIpResponse(AttNetworkPolicyIp entity) {
        return new AttendanceNetworkPolicyIpResponse(
                entity.getNetworkPolicyIpId(),
                entity.getCidrOrIp(),
                entity.getDescription(),
                entity.getStatus().name(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void validateIpRange(String cidrOrIp) {
        String normalized = normalizeNetworkValue(cidrOrIp);
        try {
            if (normalized.contains("/")) {
                String[] parts = normalized.split("/");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("CIDR format invalid");
                }
                InetAddress address = InetAddress.getByName(parts[0]);
                int prefix = Integer.parseInt(parts[1]);
                int maxPrefix = address.getAddress().length * 8;
                if (prefix < 0 || prefix > maxPrefix) {
                    throw new IllegalArgumentException("CIDR prefix invalid");
                }
            } else {
                InetAddress.getByName(normalized);
            }
        } catch (Exception exception) {
            throw new BusinessException("ATTENDANCE_NETWORK_POLICY_IP_INVALID", "IP hoặc CIDR không hợp lệ.", HttpStatus.BAD_REQUEST);
        }
    }

    private boolean matches(String clientIp, String cidrOrIp) {
        try {
            if (!cidrOrIp.contains("/")) {
                InetAddress left = InetAddress.getByName(clientIp);
                InetAddress right = InetAddress.getByName(cidrOrIp);
                return left.equals(right);
            }
            String[] parts = cidrOrIp.split("/");
            InetAddress client = InetAddress.getByName(clientIp);
            InetAddress network = InetAddress.getByName(parts[0]);
            int prefix = Integer.parseInt(parts[1]);
            byte[] clientBytes = client.getAddress();
            byte[] networkBytes = network.getAddress();
            if (clientBytes.length != networkBytes.length) {
                return false;
            }
            BigInteger clientInt = new BigInteger(1, clientBytes);
            BigInteger networkInt = new BigInteger(1, networkBytes);
            int totalBits = clientBytes.length * 8;
            BigInteger mask = prefix == 0
                    ? BigInteger.ZERO
                    : BigInteger.ONE.shiftLeft(totalBits).subtract(BigInteger.ONE).xor(BigInteger.ONE.shiftLeft(totalBits - prefix).subtract(BigInteger.ONE));
            return clientInt.and(mask).equals(networkInt.and(mask));
        } catch (Exception exception) {
            return false;
        }
    }

    private String normalizeNetworkValue(String value) {
        return value == null ? null : value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record AttendanceNetworkValidationResult(
            boolean allowed,
            String clientIp,
            String forwardedForRaw,
            Long networkPolicyId,
            String policyCode,
            String matchedCidrOrIp,
            String message
    ) {
        public static AttendanceNetworkValidationResult passed(
                String clientIp,
                String forwardedForRaw,
                Long networkPolicyId,
                String policyCode,
                String matchedCidrOrIp
        ) {
            return new AttendanceNetworkValidationResult(true, clientIp, forwardedForRaw, networkPolicyId, policyCode, matchedCidrOrIp, null);
        }

        public static AttendanceNetworkValidationResult failed(
                String clientIp,
                String forwardedForRaw,
                Long networkPolicyId,
                String policyCode,
                String message
        ) {
            return new AttendanceNetworkValidationResult(false, clientIp, forwardedForRaw, networkPolicyId, policyCode, null, message);
        }
    }
}
