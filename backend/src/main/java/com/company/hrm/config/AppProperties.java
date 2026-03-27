package com.company.hrm.config;

import com.company.hrm.common.constant.UserStatus;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Auth auth = new Auth();
    private Bootstrap bootstrap = new Bootstrap();
    private Mail mail = new Mail();
    private RoleManagement roleManagement = new RoleManagement();

    @Data
    public static class Auth {
        private String jwtSecret = "digital-hrm-demo-secret-key-digital-hrm-demo-secret-key";
        private long accessTokenMinutes = 30;
        private long refreshTokenDays = 7;
        private long passwordResetMinutes = 15;
        private int maxFailedLoginAttempts = 5;
        private long autoLockMinutes = 30;
        private UserStatus defaultNewUserStatus = UserStatus.ACTIVE;
    }

    @Data
    public static class Bootstrap {
        private boolean seedDefaultAdmin = true;
        private String adminUsername = "admin";
        private String adminEmail = "admin@digitalhrm.local";
        private String adminPassword = "Admin@123456";
        private String adminPhoneNumber = "0900000000";
    }

    @Data
    public static class Mail {
        private boolean mockEnabled = true;
        private String fromAddress = "no-reply@digitalhrm.local";
        private String resetUrlBase = "http://localhost:5173/auth/reset-password";
    }

    @Data
    public static class RoleManagement {
        private boolean allowCustomRole = false;
    }
}
