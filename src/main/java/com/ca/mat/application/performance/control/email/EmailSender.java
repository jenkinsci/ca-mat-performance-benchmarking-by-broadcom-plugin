/*
 * The 3-Clause BSD License

 * Copyright © 2021 Broadcom. All rights reserved. The term “Broadcom” refers to Broadcom Inc. and/or its
 * affiliates. All authorized reproductions of this software must be marked with this language.

 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:

 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.

 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the distribution.

 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse
 * or promote products derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.ca.mat.application.performance.control.email;


import com.ca.mat.application.performance.model.SMTPConfig;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * This class sends e-mails using SMTP protocol via a default implementation of a Java SMTP Client.
 *
 * @author Arthur Pessoa
 */
public class EmailSender {

    /**
     * The content type.
     */
    private static final String CONTENT_TYPE = "text/html; charset=utf-8";
    /**
     * The mail session.
     */
    private Session session;

    /**
     * Default Email Sender constructor with authentication handler.
     *
     * @param config - the SMTP configuration containing the authentication, hostname and SMTP port.
     */
    public EmailSender(SMTPConfig config) {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", config.getAuth());
        prop.put("mail.smtp.host", config.getHostname());
        prop.put("mail.smtp.port", config.getPort());
        if (config.getAuth().equals("true")) {
            Authenticator auth = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    String username = config.getUsername();
                    String password = config.getPassword();
                    return new PasswordAuthentication(username, password);
                }
            };
            session = Session.getDefaultInstance(prop, auth);
            return;
        }
        session = Session.getInstance(prop);
    }

    /**
     * Sends an e-mail using the SMTP client with previously configured SMTP configuration
     * (host, port and authentication).
     *
     * @param msg       - the e-mail body message
     * @param sender    - the sender's e-mail
     * @param name      - the sender's name
     * @param subject   - the e-mail's subject.
     * @param toAddress - the recipients
     * @param cc        - cc recipients
     * @param bcc       - bcc recipients
     * @throws MessagingException           when the email fails to send
     * @throws UnsupportedEncodingException if an unsupported encoding were to be passed
     */
    public void sendEmail(String msg, String sender, String name,
                          String subject, String toAddress, String cc, String bcc)
            throws MessagingException, UnsupportedEncodingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(sender, name));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
        if (cc != null) {
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
        }
        if (bcc != null) {
            message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc));
        }
        message.setSubject(subject);
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(msg, CONTENT_TYPE);
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);
        message.setContent(multipart);
        Transport.send(message);
    }

}
