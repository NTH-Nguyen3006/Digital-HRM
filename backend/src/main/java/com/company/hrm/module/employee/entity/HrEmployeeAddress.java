package com.company.hrm.module.employee.entity;

import com.company.hrm.common.constant.AddressType;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "hr_employee_address")
public class HrEmployeeAddress extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_address_id", nullable = false, updatable = false)
    private Long employeeAddressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private HrEmployee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 20)
    private AddressType addressType;

    @Column(name = "country_name", length = 100)
    private String countryName;

    @Column(name = "province_name", length = 100)
    private String provinceName;

    @Column(name = "district_name", length = 100)
    private String districtName;

    @Column(name = "ward_name", length = 100)
    private String wardName;

    @Column(name = "address_line", nullable = false, length = 300)
    private String addressLine;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;
}
