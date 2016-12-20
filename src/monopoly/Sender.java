package monopoly;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Sender {

    Sender(String address, String login, String password) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("MonopolyOnline2016@gmail.com", "12345678monopoly");
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("MonopolyOnline2016@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(address));
            message.setSubject("Recovery password");
            message.setText("Dear," + login + "\n\nYour password:  " + password + "\n\n\n\nMonopoly online!");
            Transport.send(message);
            System.out.println("Done");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
