package dev.dammak.notificationservice.service;


import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import dev.dammak.notificationservice.dto.NotificationDto;
import dev.dammak.notificationservice.dto.SmsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    public boolean sendSms(NotificationDto notificationDto) {
        try {
            SmsDto smsDto = SmsDto.builder()
                    .to(notificationDto.getRecipient())
                    .message(notificationDto.getContent())
                    .variables(notificationDto.getVariables())
                    .build();

            return sendSmsViaTwilio(smsDto);
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", notificationDto.getRecipient(), e.getMessage());
            return false;
        }
    }

    private boolean sendSmsViaTwilio(SmsDto smsDto) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(smsDto.getTo()),
                    new PhoneNumber(twilioPhoneNumber),
                    smsDto.getMessage()
            ).create();

            log.info("SMS sent successfully. SID: {}", message.getSid());
            return true;
        } catch (Exception e) {
            log.error("Twilio SMS sending failed: {}", e.getMessage());
            return false;
        }
    }
}