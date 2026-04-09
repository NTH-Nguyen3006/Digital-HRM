package com.company.hrm.config;

import com.company.hrm.common.constant.UserStatus;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Auth auth = new Auth();
    private Bootstrap bootstrap = new Bootstrap();
    private Mail mail = new Mail();
    private RoleManagement roleManagement = new RoleManagement();
    private Storage storage = new Storage();
    private Reporting reporting = new Reporting();
    private Ocr ocr = new Ocr();

    @Data
    public static class Auth {
        private String jwtSecret = "digital-hrm-demo-secret-key-digital-hrm-demo-secret-key";
        private long accessTokenMinutes = 30;
        private long refreshTokenDays = 7;
        private long passwordResetMinutes = 15;
        private int maxFailedLoginAttempts = 5;
        private long autoLockMinutes = 30;
        private UserStatus defaultNewUserStatus = UserStatus.ACTIVE;
        private String accessTokenCookieName = "hrm_access_token";
        private String refreshTokenCookieName = "hrm_refresh_token";
        private String accessTokenCookiePath = "/";
        private String refreshTokenCookiePath = "/api/v1/auth";
        private String cookieSameSite = "Lax";
        private boolean cookieSecure = false;
        private String cookieDomain;
        private List<String> corsAllowedOrigins = new ArrayList<>(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));
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

    @Data
    public static class Storage {
        private String baseDir = "./storage";
        private int maxFileSizeMb = 20;
        private boolean autoCreateBaseDir = true;
    }


    @Data
    public static class Reporting {
        private boolean schedulerEnabled = true;
        private long schedulerFixedDelayMs = 300000;
    }

    @Data
    public static class Ocr {
        private boolean enabled = false;
        private String tesseractBinary = "tesseract";
        private String language = "vie+eng";
        private int pageSegMode = 6;
        private int pdfRenderDpi = 300;
        private int maxPdfPages = 10;
        private int processTimeoutSeconds = 90;
    }

}
