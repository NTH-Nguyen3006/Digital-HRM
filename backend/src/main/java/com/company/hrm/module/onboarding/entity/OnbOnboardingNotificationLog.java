package com.company.hrm.module.onboarding.entity;

import com.company.hrm.common.constant.NotificationChannel;
import com.company.hrm.common.constant.OnboardingNotificationDeliveryStatus;
import com.company.hrm.module.systemconfig.entity.SysNotificationTemplate;
import com.company.hrm.module.user.entity.SecUserAccount;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "onb_onboarding_notification_log")
public class OnbOnboardingNotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "onboarding_notification_log_id", nullable = false, updatable = false)
    private Long onboardingNotificationLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "onboarding_id", nullable = false)
    private OnbOnboarding onboarding;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_template_id")
    private SysNotificationTemplate notificationTemplate;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_code", nullable = false, length = 20)
    private NotificationChannel channelCode;

    @Column(name = "recipient_name", length = 200)
    private String recipientName;

    @Column(name = "recipient_email", length = 150)
    private String recipientEmail;

    @Column(name = "subject_snapshot", length = 255)
    private String subjectSnapshot;

    @Column(name = "body_snapshot", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String bodySnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 20)
    private OnboardingNotificationDeliveryStatus deliveryStatus;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sent_by")
    private SecUserAccount sentBy;

    @Column(name = "note", length = 500)
    private String note;
}
