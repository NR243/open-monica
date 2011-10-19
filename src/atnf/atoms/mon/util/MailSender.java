//
// Copyright (C) CSIRO Australia Telescope National Facility
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Library General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package atnf.atoms.mon.util;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

import org.apache.log4j.Logger;

/**
 * Trivial wrapper class for JavaMail.
 * 
 * @author David Brodrick
 */
public class MailSender {
  /** The Session. */
  private static Session theirSession = Session.getDefaultInstance(new Properties(), null);

  /** Logger. */
  private static Logger theirLogger = Logger.getLogger(MailSender.class);

  /** Send an email with the specified recipient, subject and body. */
  public static void sendMail(String to, String subject, String body) {
    MimeMessage message = new MimeMessage(theirSession);
    try {
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
      message.setSubject(subject);
      message.setText(body);
      Transport.send(message);
    } catch (MessagingException ex) {
      theirLogger.error("Cannot send email: " + ex);
    }
  }
  
  public final static void main(String[] args) {
    if (args.length<3) {
      System.err.println("USAGE: Requires three arguments: recipient_email, subject, body");
      System.exit(1);
    }
    
    MailSender.sendMail(args[0], args[1], args[2]);
  }
}