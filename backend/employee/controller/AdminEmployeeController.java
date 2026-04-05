package com.company.hrm.module.employee.controller;

import com.company.hrm.common.constant.EmploymentStatus;
import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.module.employee.dto.*;
import com.company.hrm.module.employee.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/employees")
public class AdminEmployeeController {

    private final EmployeeService employeeService;

    public AdminEmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('employee.view')")
    public ApiResponse<PageResponse<EmployeeListItemResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EmploymentStatus employmentStatus,
            @RequestParam(required = false) Long orgUnitId,
            @RequestParam(required = false) Long jobTitleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success("EMPLOYEE_LIST_SUCCESS", "Lấy danh sách nhân sự thành công.", employeeService.list(keyword, employmentStatus, orgUnitId, jobTitleId, page, size), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('employee.view')")
    public ApiResponse<EmployeeDetailResponse> getDetail(@PathVariable Long employeeId) {
        return ApiResponse.success("EMPLOYEE_DETAIL_SUCCESS", "Lấy chi tiết nhân sự thành công.", employeeService.getDetail(employeeId), null, RequestTraceContext.getTraceId());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('employee.create')")
    public ApiResponse<EmployeeDetailResponse> create(@Valid @RequestBody CreateEmployeeRequest request) {
        return ApiResponse.success("EMPLOYEE_CREATE_SUCCESS", "Tạo mới nhân sự thành công.", employeeService.create(request), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('employee.update')")
    public ApiResponse<EmployeeDetailResponse> update(@PathVariable Long employeeId, @Valid @RequestBody UpdateEmployeeRequest request) {
        return ApiResponse.success("EMPLOYEE_UPDATE_SUCCESS", "Cập nhật nhân sự thành công.", employeeService.update(employeeId, request), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/{employeeId}/employment-status")
    @PreAuthorize("hasAuthority('employee.change_status')")
    public ApiResponse<EmployeeDetailResponse> changeStatus(@PathVariable Long employeeId, @Valid @RequestBody UpdateEmploymentStatusRequest request) {
        return ApiResponse.success("EMPLOYEE_STATUS_SUCCESS", "Cập nhật trạng thái lao động thành công.", employeeService.changeEmploymentStatus(employeeId, request), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/{employeeId}/transfer")
    @PreAuthorize("hasAuthority('employee.transfer')")
    public ApiResponse<EmployeeDetailResponse> transfer(@PathVariable Long employeeId, @Valid @RequestBody TransferEmployeeRequest request) {
        return ApiResponse.success("EMPLOYEE_TRANSFER_SUCCESS", "Điều chuyển nhân sự thành công.", employeeService.transfer(employeeId, request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/{employeeId}/profile")
    @PreAuthorize("hasAuthority('employee.profile.view')")
    public ApiResponse<EmployeeProfileResponse> getProfile(@PathVariable Long employeeId) {
        return ApiResponse.success("EMPLOYEE_PROFILE_DETAIL_SUCCESS", "Lấy hồ sơ mở rộng thành công.", employeeService.getProfile(employeeId), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/{employeeId}/profile")
    @PreAuthorize("hasAuthority('employee.profile.update')")
    public ApiResponse<EmployeeProfileResponse> upsertProfile(@PathVariable Long employeeId, @Valid @RequestBody EmployeeProfileRequest request) {
        return ApiResponse.success("EMPLOYEE_PROFILE_UPSERT_SUCCESS", "Cập nhật hồ sơ mở rộng thành công.", employeeService.upsertProfile(employeeId, request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/{employeeId}/addresses")
    @PreAuthorize("hasAuthority('employee.profile.view')")
    public ApiResponse<List<EmployeeAddressResponse>> listAddresses(@PathVariable Long employeeId) {
        return ApiResponse.success("EMPLOYEE_ADDRESS_LIST_SUCCESS", "Lấy danh sách địa chỉ nhân sự thành công.", employeeService.listAddresses(employeeId), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/{employeeId}/addresses")
    @PreAuthorize("hasAuthority('employee.profile.update')")
    public ApiResponse<EmployeeAddressResponse> createAddress(@PathVariable Long employeeId, @Valid @RequestBody EmployeeAddressRequest request) {
        return ApiResponse.success("EMPLOYEE_ADDRESS_CREATE_SUCCESS", "Tạo địa chỉ nhân sự thành công.", employeeService.createAddress(employeeId, request), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/{employeeId}/addresses/{addressId}")
    @PreAuthorize("hasAuthority('employee.profile.update')")
    public ApiResponse<EmployeeAddressResponse> updateAddress(@PathVariable Long employeeId, @PathVariable Long addressId, @Valid @RequestBody EmployeeAddressRequest request) {
        return ApiResponse.success("EMPLOYEE_ADDRESS_UPDATE_SUCCESS", "Cập nhật địa chỉ nhân sự thành công.", employeeService.updateAddress(employeeId, addressId, request), null, RequestTraceContext.getTraceId());
    }

    @DeleteMapping("/{employeeId}/addresses/{addressId}")
    @PreAuthorize("hasAuthority('employee.profile.update')")
    public ApiResponse<Void> deleteAddress(@PathVariable Long employeeId, @PathVariable Long addressId) {
        employeeService.deleteAddress(employeeId, addressId);
        return ApiResponse.success("EMPLOYEE_ADDRESS_DELETE_SUCCESS", "Xóa mềm địa chỉ nhân sự thành công.", RequestTraceContext.getTraceId());
    }

    @GetMapping("/{employeeId}/emergency-contacts")
    @PreAuthorize("hasAuthority('employee.profile.view')")
    public ApiResponse<List<EmergencyContactResponse>> listEmergencyContacts(@PathVariable Long employeeId) {
        return ApiResponse.success("EMPLOYEE_EMERGENCY_CONTACT_LIST_SUCCESS", "Lấy danh sách liên hệ khẩn cấp thành công.", employeeService.listEmergencyContacts(employeeId), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/{employeeId}/emergency-contacts")
    @PreAuthorize("hasAuthority('employee.profile.update')")
    public ApiResponse<EmergencyContactResponse> createEmergencyContact(@PathVariable Long employeeId, @Valid @RequestBody EmergencyContactRequest request) {
        return ApiResponse.success("EMPLOYEE_EMERGENCY_CONTACT_CREATE_SUCCESS", "Tạo liên hệ khẩn cấp thành công.", employeeService.createEmergencyContact(employeeId, request), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/{employeeId}/emergency-contacts/{emergencyContactId}")
    @PreAuthorize("hasAuthority('employee.profile.update')")
    public ApiResponse<EmergencyContactResponse> updateEmergencyContact(@PathVariable Long employeeId, @PathVariable Long emergencyContactId, @Valid @RequestBody EmergencyContactRequest request) {
        return ApiResponse.success("EMPLOYEE_EMERGENCY_CONTACT_UPDATE_SUCCESS", "Cập nhật liên hệ khẩn cấp thành công.", employeeService.updateEmergencyContact(employeeId, emergencyContactId, request), null, RequestTraceContext.getTraceId());
    }

    @DeleteMapping("/{employeeId}/emergency-contacts/{emergencyContactId}")
    @PreAuthorize("hasAuthority('employee.profile.update')")
    public ApiResponse<Void> deleteEmergencyContact(@PathVariable Long employeeId, @PathVariable Long emergencyContactId) {
        employeeService.deleteEmergencyContact(employeeId, emergencyContactId);
        return ApiResponse.success("EMPLOYEE_EMERGENCY_CONTACT_DELETE_SUCCESS", "Xóa mềm liên hệ khẩn cấp thành công.", RequestTraceContext.getTraceId());
    }

    @GetMapping("/{employeeId}/identifications")
    @PreAuthorize("hasAuthority('employee.profile.view')")
    public ApiResponse<List<EmployeeIdentificationResponse>> listIdentifications(@PathVariable Long employeeId) {
        return ApiResponse.success("EMPLOYEE_IDENTIFICATION_LIST_SUCCESS", "Lấy danh sách giấy tờ định danh thành công.", employeeService.listIdentifications(employeeId), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/{employeeId}/identifications")
    @PreAuthorize("hasAuthority('employee.profile.update')")
    public ApiResponse<EmployeeIdentificationResponse> createIdentification(@PathVariable Long employeeId, @Valid @RequestBody EmployeeIdentificationRequest request) {
        return ApiResponse.success("EMPLOYEE_IDENTIFICATION_CREATE_SUCCESS", "Tạo giấy tờ định danh thành công.", employeeService.createIdentification(employeeId, request), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/{employeeId}/identifications/{identificationId}")
    @PreAuthorize("hasAuthority('employee.profile.update')")
    public ApiResponse<EmployeeIdentificationResponse> updateIdentification(@PathVariable Long employeeId, @PathVariable Long identificationId, @Valid @RequestBody EmployeeIdentificationRequest request) {
        return ApiResponse.success("EMPLOYEE_IDENTIFICATION_UPDATE_SUCCESS", "Cập nhật giấy tờ định danh thành công.", employeeService.updateIdentification(employeeId, identificationId, request), null, RequestTraceContext.getTraceId());
    }

    @DeleteMapping("/{employeeId}/identifications/{identificationId}")
    @PreAuthorize("hasAuthority('employee.profile.update')")
    public ApiResponse<Void> deleteIdentification(@PathVariable Long employeeId, @PathVariable Long identificationId) {
        employeeService.deleteIdentification(employeeId, identificationId);
        return ApiResponse.success("EMPLOYEE_IDENTIFICATION_DELETE_SUCCESS", "Xóa mềm giấy tờ định danh thành công.", RequestTraceContext.getTraceId());
    }

    @GetMapping("/{employeeId}/bank-accounts")
    @PreAuthorize("hasAuthority('employee.profile.view')")
    public ApiResponse<List<EmployeeBankAccountResponse>> listBankAccounts(@PathVariable Long employeeId) {
        return ApiResponse.success("EMPLOYEE_BANK_ACCOUNT_LIST_SUCCESS", "Lấy danh sách tài khoản ngân hàng thành công.", employeeService.listBankAccounts(employeeId), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/{employeeId}/bank-accounts")
    @PreAuthorize("hasAuthority('employee.profile.update')")
    public ApiResponse<EmployeeBankAccountResponse> createBankAccount(@PathVariable Long employeeId, @Valid @RequestBody EmployeeBankAccountRequest request) {
        return ApiResponse.success("EMPLOYEE_BANK_ACCOUNT_CREATE_SUCCESS", "Tạo tài khoản ngân hàng thành công.", employeeService.createBankAccount(employeeId, request), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/{employeeId}/bank-accounts/{bankAccountId}")
    @PreAuthorize("hasAuthority('employee.profile.update')")
    public ApiResponse<EmployeeBankAccountResponse> updateBankAccount(@PathVariable Long employeeId, @PathVariable Long bankAccountId, @Valid @RequestBody EmployeeBankAccountRequest request) {
        return ApiResponse.success("EMPLOYEE_BANK_ACCOUNT_UPDATE_SUCCESS", "Cập nhật tài khoản ngân hàng thành công.", employeeService.updateBankAccount(employeeId, bankAccountId, request), null, RequestTraceContext.getTraceId());
    }

    @DeleteMapping("/{employeeId}/bank-accounts/{bankAccountId}")
    @PreAuthorize("hasAuthority('employee.profile.update')")
    public ApiResponse<Void> deleteBankAccount(@PathVariable Long employeeId, @PathVariable Long bankAccountId) {
        employeeService.deleteBankAccount(employeeId, bankAccountId);
        return ApiResponse.success("EMPLOYEE_BANK_ACCOUNT_DELETE_SUCCESS", "Xóa mềm tài khoản ngân hàng thành công.", RequestTraceContext.getTraceId());
    }

    @GetMapping("/{employeeId}/documents")
    @PreAuthorize("hasAuthority('employee.document.view')")
    public ApiResponse<List<EmployeeDocumentResponse>> listDocuments(@PathVariable Long employeeId) {
        return ApiResponse.success("EMPLOYEE_DOCUMENT_LIST_SUCCESS", "Lấy danh sách tài liệu nhân sự thành công.", employeeService.listDocuments(employeeId), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/{employeeId}/documents")
    @PreAuthorize("hasAuthority('employee.document.manage')")
    public ApiResponse<EmployeeDocumentResponse> createDocument(@PathVariable Long employeeId, @Valid @RequestBody EmployeeDocumentRequest request) {
        return ApiResponse.success("EMPLOYEE_DOCUMENT_CREATE_SUCCESS", "Tạo metadata tài liệu thành công.", employeeService.createDocument(employeeId, request), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/{employeeId}/documents/{documentId}")
    @PreAuthorize("hasAuthority('employee.document.manage')")
    public ApiResponse<EmployeeDocumentResponse> updateDocument(@PathVariable Long employeeId, @PathVariable Long documentId, @Valid @RequestBody EmployeeDocumentRequest request) {
        return ApiResponse.success("EMPLOYEE_DOCUMENT_UPDATE_SUCCESS", "Cập nhật metadata tài liệu thành công.", employeeService.updateDocument(employeeId, documentId, request), null, RequestTraceContext.getTraceId());
    }

    @DeleteMapping("/{employeeId}/documents/{documentId}")
    @PreAuthorize("hasAuthority('employee.document.manage')")
    public ApiResponse<Void> deleteDocument(@PathVariable Long employeeId, @PathVariable Long documentId) {
        employeeService.deleteDocument(employeeId, documentId);
        return ApiResponse.success("EMPLOYEE_DOCUMENT_DELETE_SUCCESS", "Xóa mềm metadata tài liệu thành công.", RequestTraceContext.getTraceId());
    }
}
