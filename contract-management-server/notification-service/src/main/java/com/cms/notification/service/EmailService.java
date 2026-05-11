package com.cms.notification.service;

import java.util.Map;

public interface EmailService {

    boolean sendEmail(String to, String subject, String htmlBody);

    boolean sendTemplatedEmail(String to, String templateName, Map<String, Object> variables);
}
