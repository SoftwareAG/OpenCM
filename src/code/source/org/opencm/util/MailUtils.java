package org.opencm.util;

import java.util.Properties;

import javax.mail.Session;
import javax.mail.Message;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.mail.Transport;
import javax.mail.MessagingException;

import org.opencm.smtp.SmtpConfiguration;
import org.opencm.smtp.Property;

public class MailUtils {

	private static String JAVAX_MAIL_PROPERTY_HOSTNAME		= "mail.smtp.host";
	private static String JAVAX_MAIL_PROPERTY_PORT			= "mail.smtp.port";
	private static String JAVAX_MAIL_PROPERTY_STARTTLS		= "mail.smtp.starttls.enable";
	
	
	public static boolean sendMessage(SmtpConfiguration smtpConfig) {
		
		Properties properties = new Properties();
        for (Property property : smtpConfig.getProperties()) {
			LogUtils.logDebug("MailUtils :: Property: " + property.getKey() + " - " + property.getValue());
        	properties.setProperty(property.getKey(), property.getValue());
        }
		
        // Add properties
        properties.setProperty(JAVAX_MAIL_PROPERTY_HOSTNAME, smtpConfig.getHostname());
        properties.setProperty(JAVAX_MAIL_PROPERTY_PORT, smtpConfig.getPort());
        properties.setProperty(JAVAX_MAIL_PROPERTY_STARTTLS, smtpConfig.getStartTLS());

	    // Get the Session object.
	    Session session = Session.getInstance(properties, new Authenticator() {
	    	protected PasswordAuthentication getPasswordAuthentication() {
	    		return new PasswordAuthentication(smtpConfig.getUsername(), smtpConfig.getPassword());
            }
	    });
	    
	    try {
	    	// Create a default MimeMessage object.
	    	Message message = new MimeMessage(session);
	   	   
	    	// Set From: header field of the header.
	    	message.setFrom(new InternetAddress(smtpConfig.getFromEmail()));

	    	// Set To: header field of the header.
	    	message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(String.join(",", smtpConfig.getRecipients())));

	    	// Set Subject: header field
	    	message.setSubject(smtpConfig.getSubject());

	    	// Send the actual HTML message, as big as you like
	    	message.setContent(smtpConfig.getBody(),"text/html");

			LogUtils.logDebug("MailUtils :: Sending from " + smtpConfig.getFromEmail());
			LogUtils.logDebug("MailUtils :: Sending to " + smtpConfig.getRecipients().toString());
			LogUtils.logDebug("MailUtils :: Subject " + smtpConfig.getSubject());
			LogUtils.logDebug("MailUtils :: Using username: " + smtpConfig.getUsername());
			LogUtils.logDebug("MailUtils :: Using password: " + smtpConfig.getPassword());
			
			LogUtils.logDebug("MailUtils :: Body " + smtpConfig.getBody());
 
	    	// Send message
	    	Transport.send(message);
		   
	    } catch (MessagingException mex) {
			LogUtils.logError("MailUtils Exception: " + mex);
	    }
		    
		return true;
	}

}

