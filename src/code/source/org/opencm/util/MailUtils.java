package org.opencm.util;

import java.util.Properties;
import java.util.Iterator;

import org.opencm.configuration.Configuration;

import javax.mail.Session;
import javax.mail.Message;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.mail.Transport;
import javax.mail.MessagingException;

public class MailUtils {

	public static boolean sendMessage(Configuration opencmConfig, String smtpPassword, String bodyMsg) {
		
		String to_email = "";
		for (int i = 0; i < opencmConfig.getSmtp().getRecipients().size(); i++) {
			to_email += opencmConfig.getSmtp().getRecipients().get(i);
			if (i+1 < opencmConfig.getSmtp().getRecipients().size()) {
				to_email += ",";
			}
		}
		opencmConfig.getSmtp().getRecipients().getFirst();
		String sender_email = opencmConfig.getSmtp().getFrom_email();
		final String username = opencmConfig.getSmtp().getSmtp_username();

		Properties props = new Properties();
		Iterator<String> it = opencmConfig.getSmtp().getProperties().keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			String val = opencmConfig.getSmtp().getProperties().get(key);
		    props.put(key, val);
		}
    
	    // Get the Session object.
	    Session session = Session.getInstance(props, new Authenticator() {
	    	protected PasswordAuthentication getPasswordAuthentication() {
	    		return new PasswordAuthentication(username, smtpPassword);
            }
	    });
	    
	    try {
	    	// Create a default MimeMessage object.
	    	Message message = new MimeMessage(session);
	   	   
	    	// Set From: header field of the header.
	    	message.setFrom(new InternetAddress(sender_email));

	    	// Set To: header field of the header.
	    	message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to_email));

	    	// Set Subject: header field
	    	message.setSubject(opencmConfig.getSmtp().getSubject());

	    	// Send the actual HTML message, as big as you like
	    	message.setContent(bodyMsg,"text/html");
	    	// "<b>Whatever</b>, dude <br/><br/> and whatever goes here ... ",

	    	// Send message
	    	Transport.send(message);
		   
	    } catch (MessagingException mex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"MailUtils Exception: " + mex);
	    }
		    
		return true;
	}

}

