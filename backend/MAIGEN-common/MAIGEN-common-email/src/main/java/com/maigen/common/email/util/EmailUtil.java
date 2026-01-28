package com.maigen.common.email.util;

import com.maigen.common.email.config.EmailConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailUtil {
    final JavaMailSender mailSender;
    final EmailConfig emailConfig;

    public String getCode() {
        String str = "123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"; // no zero
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int index = (int) (Math.random() * str.length());
            code.append(str.charAt(index));
        }
        return code.toString();
    }

    public String sendCode(String to) {
        String code = getCode();
        String title = "MAIGEN注册验证码邮件";
        String text = "【MAIGEN】您的验证码为：" + code + "（有效期5分钟），本邮件由系统自动发送，请勿直接回复。";
        sendMsg(to, title, text);
        return code;
    }

    public void sendDeveloperEmail(String to){
        String title = "MAIGEN专属开发者反馈通知";
        String text = "亲爱的MAIGEN开发者,有用户新提交了他们对平台的反馈，请及时查看并为其解答";
        sendMsg(to, title, text);
    }

    public void sendMsg(String to, String title, String msg) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(emailConfig.getUsername());
        mailMessage.setTo(to);
        mailMessage.setSubject(title);
        mailMessage.setText(msg);
        mailSender.send(mailMessage);
    }

}