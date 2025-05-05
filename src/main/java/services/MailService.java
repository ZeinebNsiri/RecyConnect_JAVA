package services;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

import java.io.InputStream;
import java.util.Properties;

public class MailService {

    private static final String username = "recyconnectapp2425@gmail.com"; // Remplacer par ton email
    private static final String password = "kqfn xmcd aquh gbpe"; // Attention à la sécurité !



    public static void sendMail(String toEmail, String subject, String bodyHtml) {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            // Prepare multipart email (HTML + Image)
            MimeMultipart multipart = new MimeMultipart("related");

            // 1. HTML Part (replace image src with cid)
            BodyPart htmlPart = new MimeBodyPart();
            String modifiedHtml = bodyHtml.replace("src='src/main/resources/images/mainlogo.png'", "src='cid:logo'");
            htmlPart.setContent(modifiedHtml, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            // 2. Image Part
            MimeBodyPart imagePart = new MimeBodyPart();
            InputStream logoStream = MailService.class.getResourceAsStream("/images/mainlogo.png"); // Make sure image is in `resources`
            if (logoStream == null) {
                System.out.println("❌ Logo introuvable !");
                return;
            }

            DataSource fds = new ByteArrayDataSource(logoStream, "image/png");
            imagePart.setDataHandler(new DataHandler(fds));
            imagePart.setHeader("Content-ID", "<logo>");
            imagePart.setDisposition(MimeBodyPart.INLINE);
            multipart.addBodyPart(imagePart);

            // Attach the multipart content to the message
            message.setContent(multipart);

            Transport.send(message);

            System.out.println("✅ Email envoyé avec succès !");
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

