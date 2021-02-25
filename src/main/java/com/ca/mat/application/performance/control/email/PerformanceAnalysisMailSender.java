package com.ca.mat.application.performance.control.email;

import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.MailSender;

import javax.annotation.CheckForNull;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.lang.reflect.Method;

public class PerformanceAnalysisMailSender extends MailSender {

    /**
     * The e-mail template
     */
    private final String template;

    /**
     * The content type.
     */
    private static final String CONTENT_TYPE = "text/html; charset=utf-8";

    public PerformanceAnalysisMailSender(String recipients, String template, boolean dontNotifyEveryUnstableBuild, boolean sendToIndividuals, String charset) {
        super(recipients, dontNotifyEveryUnstableBuild, sendToIndividuals, charset);
        this.template = template;
    }

    @CheckForNull
    @Override
    protected MimeMessage createMail(Run<?, ?> build, TaskListener listener) throws MessagingException {
        try {
            Method method = MailSender.class.getDeclaredMethod("createEmptyMail", Run.class, TaskListener.class);
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            MimeMessage msg = (MimeMessage) method.invoke(this, build, listener);
            msg.setSubject("PMA analyser report");
            msg.setContent(template, CONTENT_TYPE);
            return msg;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not send Performance Analysis Report template", e);
        }
    }
}
