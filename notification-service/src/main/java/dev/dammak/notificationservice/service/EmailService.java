package dev.dammak.notificationservice.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import dev.dammak.notificationservice.dto.EmailDto;
import dev.dammak.notificationservice.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final SendGrid sendGrid;

    @Value("${spring.mail.from:noreply@ecommerce.com}")
    private String fromEmail;

    @Value("${notification.email.provider:smtp}")
    private String emailProvider;

    public boolean sendEmail(NotificationDto notificationDto) {
        try {
            EmailDto emailDto = EmailDto.builder()
                    .to(notificationDto.getRecipient())
                    .subject(notificationDto.getSubject())
                    .content(notificationDto.getContent())
                    .isHtml(true)
                    .variables(notificationDto.getVariables())
                    .build();

            return "sendgrid".equals(emailProvider) ?
                    sendEmailViaSendGrid(emailDto) :
                    sendEmailViaSmtp(emailDto);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", notificationDto.getRecipient(), e.getMessage());
            return false;
        }
    }

    private boolean sendEmailViaSendGrid(EmailDto emailDto) {
        try {
            Email from = new Email(fromEmail);
            Email to = new Email(emailDto.getTo());
            Content content = new Content("text/html", emailDto.getContent());
            Mail mail = new Mail(from, emailDto.getSubject(), to, content);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);
            log.info("SendGrid response status: {}", response.getStatusCode());

            return response.getStatusCode() >= 200 && response.getStatusCode() < 300;
        } catch (Exception e) {
            log.error("SendGrid email sending failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean sendEmailViaSmtp(EmailDto emailDto) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(emailDto.getTo());
            helper.setSubject(emailDto.getSubject());
            helper.setText(emailDto.getContent(), emailDto.getIsHtml());

            javaMailSender.send(message);
            log.info("SMTP email sent successfully to {}", emailDto.getTo());
            return true;
        } catch (Exception e) {
            log.error("SMTP email sending failed: {}", e.getMessage());
            return false;
        }
    }
}