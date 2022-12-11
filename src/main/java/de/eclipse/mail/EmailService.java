package de.eclipse.mail;

import de.eclipse.mail.exception.EmailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

@Service
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    @Value("${email.username}")
    private String from;

    @Value("${email.sendTo}")
    private String to;

    @Value("${email.template.subject}")
    private String subject;

    @Value("${email.template.path}")
    private String templatePath;

    @Value("${email.template.name}")
    private String name;

    private final SpringTemplateEngine templateEngine;

    private final Session session;


    @Autowired
    public EmailService(SpringTemplateEngine templateEngine, Session session) {
        this.templateEngine = templateEngine;
        this.session = session;
    }

    public void sendEmail() {
        Message message = new MimeMessage(session);
        Context context = new org.thymeleaf.context.Context();
        context.setVariable("name", name);

        String html = templateEngine.process(templatePath, context);

        try {
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setHeader("content-type", "text/html");

            BodyPart bodyPart = new MimeBodyPart();
            bodyPart.setText(html);
            bodyPart.setContent(html, "text/html;charset=utf-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(bodyPart);

            message.setContent(multipart);
            Transport.send(message);
            LOGGER.info("E-Mail sent to: {}", to);

        } catch (MessagingException e) {
            LOGGER.error("Failed to send e-mail: ", e);
            throw new EmailException(e);
        }
    }
}