package com.company.hrm.module.auth.service;

public interface MailService {
    void sendPasswordResetMail(String email, String username, String resetLink);

    boolean sendSimpleMail(String email, String subject, String body);
}
