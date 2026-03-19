package com.example.crabackend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@example.com}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendSimpleEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

        public void sendAccountActivationEmail(String to, String activationUrl) {
                String subject = "Bienvenue chez CBX Group - Activation de votre compte";
                try {
                        sendAccountActivationHtmlEmail(to, activationUrl, subject);
                } catch (Exception e) {
                        sendSimpleEmail(
                                        to,
                                        subject,
                                        "Bonjour,\n\n" +
                                                        "Votre compte collaborateur a ete cree.\n\n" +
                                                        "Pour finaliser votre acces, cliquez sur le lien ci-dessous puis definissez votre mot de passe :\n" +
                                                        activationUrl +
                                                        "\n\n" +
                                                        "Le compte sera actif uniquement apres validation email et autorisation admin.\n\n" +
                                                        "Cordialement,\n" +
                                                        "L'equipe Administrative"
                        );
                }
    }

        private void sendAccountActivationHtmlEmail(String to, String activationUrl, String subject) throws MessagingException {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(from);
                helper.setTo(to);
                helper.setSubject(subject);

                String html = """
                                <!doctype html>
                                <html lang="fr">
                                <head>
                                    <meta charset="UTF-8" />
                                    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                                    <title>Activation CBX Group</title>
                                </head>
                                <body style="margin:0;padding:0;background:#f3f4f6;font-family:Arial,Helvetica,sans-serif;color:#1f2937;">
                                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#f3f4f6;padding:28px 12px;">
                                        <tr>
                                            <td align="center">
                                                <table role="presentation" width="840" cellspacing="0" cellpadding="0" style="max-width:840px;width:100%%;background:#ffffff;border:1px solid #d1d5db;border-radius:12px;overflow:hidden;">
                                                    <tr>
                                                        <td align="center" style="background:#06080c;padding:34px 20px;">
                                                            <img src="cid:cbxLogo" alt="CBX Group" style="width:96px;height:auto;display:block;" />
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding:40px 38px 32px 38px;">
                                                            <h1 style="margin:0 0 20px 0;font-size:30px;line-height:1.15;color:#0b0f14;">Bienvenue !</h1>
                                                            <p style="margin:0 0 16px 0;font-size:19px;line-height:1.5;color:#374151;">Votre compte collaborateur a ete cree.</p>
                                                            <p style="margin:0 0 22px 0;font-size:20px;line-height:1.5;color:#374151;">Pour finaliser votre acces, cliquez sur le bouton ci-dessous puis definissez votre mot de passe :</p>
                                                            <p style="text-align:center;margin:0 0 36px 0;">
                                                                <a href="%s" style="display:inline-block;background:#0b0f14;color:#ffffff;text-decoration:none;font-weight:700;font-size:18px;line-height:1;padding:16px 24px;border-radius:12px;">Definir mon mot de passe</a>
                                                            </p>
                                                            <p style="margin:0 0 28px 0;font-size:18px;line-height:1.5;color:#4b5563;">Le compte sera actif uniquement apres validation email et autorisation admin.</p>
                                                            <p style="margin:0;font-size:18px;line-height:1.5;color:#6b7280;">Cordialement,<br/>L'equipe Administrative</p>
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <td align="center" style="padding:18px 12px;background:#f9fafb;border-top:1px solid #e5e7eb;color:#9ca3af;font-size:16px;">(c) 2026 CBX Group. Tous droits reserves.</td>
                                                    </tr>
                                                </table>
                                            </td>
                                        </tr>
                                    </table>
                                </body>
                                </html>
                                """.formatted(escapeHtmlAttribute(activationUrl));

                helper.setText(html, true);
                helper.addInline("cbxLogo", new ClassPathResource("static/images/CBX.png"), "image/png");
                mailSender.send(message);
        }

        private String escapeHtmlAttribute(String value) {
                if (value == null) {
                        return "";
                }
                return value
                                .replace("&", "&amp;")
                                .replace("\"", "&quot;")
                                .replace("<", "&lt;")
                                .replace(">", "&gt;");
        }

    public void sendActivationCodeEmail(String to, String code) {
        sendSimpleEmail(to, "Code d'activation CRA", "Votre code d'activation est : " + code);
    }

    public void sendCraReminderEmail(String to, String monthLabel) {
        sendSimpleEmail(
                to,
                "Rappel de saisie CRA",
                "Bonjour,\n\nMerci de completer votre CRA pour " + monthLabel + ".\n\nCordialement,"
        );
    }
}
