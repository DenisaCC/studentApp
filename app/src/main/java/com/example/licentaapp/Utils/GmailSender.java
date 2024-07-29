package com.example.licentaapp.Utils;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class GmailSender {
    public static void sendEmail(String recipientEmail, String subject, String body) throws MessagingException {
        String username = "studentappuniversitate@gmail.com";
        String password = "akpl gznk rqaf rhad";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

            System.out.println("E-mail sent successfully!");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
