package com.oauth2cloud.server.rest.util;

import com.oauth2cloud.server.rest.Environment;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.codemonkey.simplejavamail.Mailer;

import javax.mail.Message;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailSender {
    private static final Logger LOG = Logger.getLogger(EmailSender.class.getName());

    /**
     * Send an e-mail using the template in the applications templates.email package
     *
     * @param replyTo  who to send from
     * @param to       who to send to
     * @param subject  of the email
     * @param template to build the e-mail
     * @param model    object to pass into template
     */
    public static void sendTemplateEmail(
            final Mailer mailer, final Configuration cfg,
            final String replyTo, final String to, final String subject,
            final String template, final Object model
    ) {
        try {
            final org.codemonkey.simplejavamail.email.Email email = new org.codemonkey.simplejavamail.email.Email();
            email.setFromAddress("OAuth2Cloud Admin", Environment.SEND_EMAILS_FROM);
            email.setSubject(subject);
            email.addRecipient(to, to, Message.RecipientType.TO);
            if (replyTo != null) {
                email.setReplyToAddress(replyTo, replyTo);
            }
            email.setTextHTML(processTemplate(cfg, template, model));
            try {
                mailer.sendMail(email);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to send e-mail", e);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to send e-mail", e);
        }
    }

    /**
     * Process a mail template
     *
     * @param template template to process
     * @param model    object to inject into template
     * @return String of the processed template
     * @throws IOException
     * @throws TemplateException
     */
    private static String processTemplate(final Configuration cfg, final String template, final Object model)
            throws IOException, TemplateException {
        final StringWriter sw = new StringWriter();
        cfg.getTemplate(template).process(model, sw);
        return sw.toString();
    }
}
