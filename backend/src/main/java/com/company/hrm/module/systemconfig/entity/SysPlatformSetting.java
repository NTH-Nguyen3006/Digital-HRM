package com.company.hrm.module.systemconfig.entity;

import com.company.hrm.common.constant.PlatformSettingValueType;
import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sys_platform_setting")
public class SysPlatformSetting extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "platform_setting_id", nullable = false, updatable = false)
    private Long platformSettingId;

    @Column(name = "setting_key", nullable = false, length = 100)
    private String settingKey;

    @Column(name = "setting_name", nullable = false, length = 200)
    private String settingName;

    @Column(name = "setting_value", columnDefinition = "NVARCHAR(MAX)")
    private String settingValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 20)
    private PlatformSettingValueType valueType;

    @Column(name = "is_editable", nullable = false)
    private boolean editable = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status;

    @Column(name = "description", length = 500)
    private String description;
}
