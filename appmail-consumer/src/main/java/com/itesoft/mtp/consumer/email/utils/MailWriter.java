package com.itesoft.mtp.consumer.email.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;

import org.apache.commons.lang3.StringUtils;

import com.itesoft.mtp.model.EmailObject;

import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.property.complex.Attachment;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import microsoft.exchange.webservices.data.property.complex.MimeContent;

public class MailWriter {


	/**
	 * Write the Subject and the Body of the mail
	 * @param emailObject	mail to write
	 * @param name			name of the file to write
	 */
	public static void writeSubjectAndContent(EmailObject emailObject, String fileName) throws Exception {

		Path path = Paths.get(fileName);
		BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
		try {
			writer.write("Subject: ");
			writer.newLine();
			writer.write(emailObject.getSubject());
			writer.newLine();
			writer.write("Content: ");
			writer.newLine();

			//Manage the writing of the mail's content
			if(StringUtils.equalsIgnoreCase(emailObject.getProtocol(), "ews")){
				writeContentEWS(emailObject, writer);
			}else {
				writeContent(emailObject, writer);
			}
		}
		finally {
			if (writer != null) { writer.flush(); writer.close(); }
		}

	}

	/**
	 * Write the MimeType to EML file
	 * @param emailObject	mail to write
	 * @param name			name of the file to write
	 */
	public static void writeEMLformat(EmailObject emailObject, String fileName) throws Exception {

		FileOutputStream writer = new FileOutputStream(new File(fileName));
		try {
			
			if(StringUtils.equalsIgnoreCase(emailObject.getProtocol(), "ews")){
				writeEMLfileGettingBytesEWS(emailObject, writer);
			}else {
				writeEMLfileGettingBytes(emailObject, writer);
			}
		}
		finally {
			if (writer != null) { writer.flush(); writer.close(); }
		}
	}



	/**
	 * Save attachments
	 * @param emailObject	mail to write
	 * @param name			name of the file to write
	 */
	public static void saveMessageAttachments(EmailObject emailObject, String fileName) throws Exception {

		if(StringUtils.equalsIgnoreCase(emailObject.getProtocol(), "ews")){
			saveAttachmentsEWS(emailObject, fileName);

		}else {
			saveAttachments(emailObject, fileName);
		}

	}

	private static void writeContent(EmailObject emailObject, BufferedWriter writer) throws Exception {
		Message message = (Message) emailObject.getMessage();

		findAndWriteContentInMimeBodyPart(message, writer);
	}

	/*
	 * This method write the content of the message for EWS Protocol
	 */
	private static void writeContentEWS(EmailObject emailObject, BufferedWriter writer) throws Exception {
		EmailMessage message = (EmailMessage)emailObject.getMessage();
		MessageBody body = message.getBody();
		writer.write(body.toString());

	}

	/*
	 * This method write the content of the message
	 */
	private static void findAndWriteContentInMimeBodyPart(Part message, BufferedWriter writer) throws Exception {
		String contentType = message.getContentType();

		if(contentType.toLowerCase().contains("multipart/")) {

			// content may contain attachments
			Multipart multiPart = (Multipart) message.getContent();
			int numberOfParts = multiPart.getCount();

			for (int partCount = 0; partCount < numberOfParts; partCount++) {
				MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);

				//If it's an attachment, we skip it and continue the search
				if (!Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
					findAndWriteContentInMimeBodyPart(part, writer);
				}
			}

		}else if (contentType.toLowerCase().contains("text/plain")) {
			Object content = message.getContent();
			if (message.getContent() != null) {
				writer.write(content.toString());
			}
		}

	}

	private static void writeEMLfileGettingBytes(EmailObject emailObject, OutputStream out) throws Exception {
		Message message = (Message) emailObject.getMessage();
		message.writeTo(out);
	}

	/*
	 * This method write the content of the message for EWS Protocol
	 */
	private static void writeEMLfileGettingBytesEWS(EmailObject emailObject, FileOutputStream fileStream) throws Exception {
		EmailMessage message = (EmailMessage)emailObject.getMessage();
		MimeContent mimeContent = message.getMimeContent();

		fileStream.write(mimeContent.getContent());

	}

	private static List<MimeBodyPart> getAttachments(Part p) throws MessagingException, IOException {
		List<MimeBodyPart> attachments = new ArrayList<MimeBodyPart>();

		String contentType = p.getContentType();

		if(contentType.toLowerCase().contains("multipart/")) {
			// content may contain attachments
			Multipart multiPart = (Multipart) p.getContent();
			int numberOfParts = multiPart.getCount();
			for (int partCount = 0; partCount < numberOfParts; partCount++) {
				MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
				if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
					attachments.add(part);
				}else {
					attachments.addAll(getAttachments(part));
				}
			}

		}

		return attachments;
	}

	/*
	 * This method save the attachment
	 */
	private static void saveAttachments(EmailObject emailObject, String fileName) throws Exception {
		Message message = (Message) emailObject.getMessage();

		List<MimeBodyPart> attachments = getAttachments(message);

		for (int i = 0; i < attachments.size(); i++) {
			MimeBodyPart attachment = attachments.get(i);
			if (Part.ATTACHMENT.equalsIgnoreCase(attachment.getDisposition()))
				attachment.saveFile(fileName+ attachment.getFileName());
		}

	}

	/*
	 * This method save the attachment for EWS protocol
	 */
	private static void saveAttachmentsEWS(EmailObject emailObject, String fileName) throws Exception {
		EmailMessage message = (EmailMessage)emailObject.getMessage();
		for (Attachment it : message.getAttachments()) {
			FileAttachment iAttachment = (FileAttachment) it;
			iAttachment.load( fileName + iAttachment .getName());
		}


	}
}
