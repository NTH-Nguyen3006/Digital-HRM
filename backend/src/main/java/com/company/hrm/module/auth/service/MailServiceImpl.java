package com.company.hrm.module.auth.service;

import com.company.hrm.config.AppProperties;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MailServiceImpl implements MailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final AppProperties appProperties;

    public MailServiceImpl(ObjectProvider<JavaMailSender> mailSenderProvider, AppProperties appProperties) {
        this.mailSenderProvider = mailSenderProvider;
        this.appProperties = appProperties;
    }

    @Override
    public void sendPasswordResetMail(String email, String username, String resetLink) {
        if (appProperties.getMail().isMockEnabled()) {
            log.info("[MOCK_MAIL] Send password reset to email={} username={} resetLink={}", email, username, resetLink);
            return;
        }

        JavaMailSender mailSender = Optional.ofNullable(mailSenderProvider.getIfAvailable())
                .orElseThrow(() -> new IllegalStateException("JavaMailSender is not configured."));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(appProperties.getMail().getFromAddress());
        message.setTo(email);
        message.setSubject("Digital HRM - Password reset");
        message.setText("""
                Xin chào %s,

                Bạn vừa yêu cầu đặt lại mật khẩu cho Digital HRM.
                Vui lòng truy cập liên kết sau để đặt mật khẩu mới:

                %s

                Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email.
                """.formatted(username, resetLink));

        mailSender.send(message);
    }
}
