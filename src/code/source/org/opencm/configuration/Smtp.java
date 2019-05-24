package org.opencm.configuration;

import java.util.Map;
import java.util.LinkedList;

public class Smtp {

    private String smtp_username;
    private String from_email;
    private String subject;
    private LinkedList<String> recipients;
    private Map<String, String> properties;

    public String getSmtp_username() {
        return this.smtp_username;
    }
    public void setSmtp_username(String name) {
        this.smtp_username = name;
    }

    public String getFrom_email() {
        return this.from_email;
    }
    public void setFrom_email(String from) {
        this.from_email = from;
    }

    public String getSubject() {
        return this.subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public LinkedList<String> getRecipients() {
        return this.recipients;
    }
    public void setRecipients(LinkedList<String> recps) {
        this.recipients = recps;
    }

    public Map<String,String> getProperties() {
        return this.properties;
    }
    public void setProperties(Map<String,String> props) {
        this.properties = props;
    }
}
