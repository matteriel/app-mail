package com.itesoft.mtp.consumer.email;

import java.io.IOException;
import java.nio.file.Path;

import javax.mail.MessagingException;

import com.itesoft.mtp.consumer.email.utils.MailWriter;
import com.itesoft.mtp.model.EmailObject;

public class MailsManager {

	
	/**
	 * Save the Mail to universal format : EML
	 * @param emailObject	mail to save
	 * @param path			path where to save it
	 * @param name			name of the file to create
	 */
	public static void saveMailToEMLformat(final EmailObject emailObject, final Path path, String name) throws MessagingException, IOException
	{
		String fileName = path + name + ".eml";
		try {
			MailWriter.writeEMLformat(emailObject, fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Save the Subject and the Content in a TKT file
	 * @param emailObject	mail to save
	 * @param path			path where to save it
	 * @param name			name of the file to create
	 */
	public static void saveMailToTXTformat(EmailObject emailObject, final Path path, String name) throws MessagingException, IOException
	{
		String fileName = path + name + ".txt";
		try {
			MailWriter.writeSubjectAndContent(emailObject, fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Save attachments to the mail
	 * @param emailObject	mail
	 * @param path			path where to save them
	 * @param name			name of the attachment
	 */
	public static void saveAttachments(final EmailObject emailObject, final Path path, String name) throws MessagingException, IOException
	{
		String fileName = path + name + "_";

		try {
			MailWriter.saveMessageAttachments(emailObject, fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}