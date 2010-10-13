/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.paxxis.chime.notification;

import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.StringData;
import com.paxxis.chime.service.ChimeConfiguration;
import com.paxxis.chime.service.Tools;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.log4j.Logger;
import org.htmlparser.Parser;
import org.htmlparser.visitors.TextExtractingVisitor;

/**
 * This is the base class for all types of message notifiers.
 *
 * @author Robert Englander
 */
public abstract class MessageNotifier implements Runnable {
    private static final Logger _logger = Logger.getLogger(MessageNotifier.class);

    /**
     * Basic struct that pairs an id (a user id actually) with an email address.
     */
    protected static class Pair {
        InstanceId id;
        String email = null;
    }

    /**
     * email authenticator
     */
    private static class Authenticator extends javax.mail.Authenticator {

        private PasswordAuthentication authentication;

        public Authenticator(String username, String password) {
            authentication = new PasswordAuthentication(username, password);
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return authentication;
        }
    }

    /** the chime config that contains email notification properties */
    private ChimeConfiguration config;

    protected MessageNotifier(ChimeConfiguration config) {
        this.config = config;
    }

    void send(Pair pair, DatabaseConnection dbconn, String subject, String body) {
        List<Pair> pairs = new ArrayList<Pair>();
        pairs.add(pair);
        send(pairs, dbconn, subject, body);
    }

    void send(List<Pair> pairs, DatabaseConnection dbconn, String subject, String body) {
        Authenticator auth = new Authenticator(config.getStringValue("chime.notification.email.sender", ""),
                config.getStringValue("chime.notification.email.senderpw", ""));

        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.host", config.getStringValue("chime.notification.email.host", ""));
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.submitter", auth.getPasswordAuthentication().getUserName());
        properties.setProperty("mail.smtp.auth", "true");

        long start = System.currentTimeMillis();

        Session session = Session.getDefaultInstance(properties, auth);

        try {
            dbconn.startTransaction();

            // find the email addresses of users that are interested in this change.
            Transport tr = session.getTransport();
            tr.connect();

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(config.getStringValue("chime.notification.email.replyto", "")));
            message.setSubject(subject);
            String emailBody = body;
            emailBody = emailBody.replaceAll("<br>", "\n");

            Parser htmlparser = Parser.createParser(emailBody, null);
            TextExtractingVisitor visitor = new TextExtractingVisitor();
            htmlparser.visitAllNodesWith(visitor);
            emailBody = visitor.getExtractedText();

            message.setText(emailBody);

            String bodyText = new StringData(body).asSQLValue();
            boolean sendEmail = false;
            for (Pair pair : pairs) {
                InstanceId id = com.paxxis.chime.service.Tools.getNewId(Tools.DEFAULT_EXTID);
                String sql = "insert into Chime.MessageJournal (id,user_id,subject,message,timestamp,seen) values ('"
                        + id + "','" + pair.id.getValue() + "', '" + subject
                        + "'," + bodyText + ", CURRENT_TIMESTAMP, 'N')";
                dbconn.executeStatement(sql);

                if (pair.email != null) {
                    sendEmail = true;
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(pair.email));
                }
            }

            if (sendEmail) {
                tr.send(message); //, message.getRecipients(Message.RecipientType.TO));
            }
            
            dbconn.commitTransaction();
        } catch (Exception e) {
            _logger.error(e);
            try {
                dbconn.rollbackTransaction();
            } catch (Exception ee) {
                _logger.error(ee);
            }
        }

        long end = System.currentTimeMillis();
        _logger.info(pairs.size() + " email(s) sent in " + (end - start) + " msecs");
    }

}
