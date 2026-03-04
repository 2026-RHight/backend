package com.reverse.email;

import com.reverse.core.event.EmailSendEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailEventListener {

    private final JavaMailSender mailSender;

    @Async
    @EventListener
    public void handleEmailEvent(EmailSendEvent event) {
        log.info("메일 발송 시작: 수신자={}, 제목={}", event.to(), event.subject());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            // true는 멀티파트 메세지(첨부파일, HTML 등)를 지원함을 의미합니다.
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(event.to());
            helper.setSubject(event.subject());
            // event.body()에 HTML 태그가 포함될 수 있으므로 true로 설정합니다.
            helper.setText(event.body(), true);

            // 발신자 이름 설정 (구글 계정 이메일을 입력하세요)
            helper.setFrom("rhight.official@gmail.com");

            mailSender.send(message);
            log.info("메일 발송 완료: {}", event.to());

        } catch (MessagingException e) {
            log.error("메일 발송 중 오류 발생: {}", e.getMessage());
            // 비동기 로직이므로 여기서 예외 처리를 확실히 해주거나
            // 재시도(Retry) 로직을 추가하는 것이 좋습니다.
        }
    }
}