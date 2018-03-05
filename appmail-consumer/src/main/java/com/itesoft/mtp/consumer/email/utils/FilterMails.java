package com.itesoft.mtp.consumer.email.utils;

import java.io.IOException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.search.AndTerm;
import javax.mail.search.FromTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import org.apache.commons.lang.StringUtils;

import com.itesoft.mtp.model.Filter;

public class FilterMails {

	
	public static SearchTerm getSearchWithCriteria(Filter filter) throws Exception {
		SearchTerm criterion = null;

        FromTerm fromTerm = new FromTerm(new InternetAddress(filter.getSender()));
        SubjectTerm subjectTerm = null;
        if(StringUtils.isNotBlank(filter.getWordInSubjectField()))
        {
        	subjectTerm = new SubjectTerm(filter.getWordInSubjectField());
        	criterion = new AndTerm(fromTerm, subjectTerm);
        }else {
        	criterion = fromTerm;
        }

        return criterion;
    }
	
	public static boolean matchAttachmentFilterCriteria(Message message, Filter filter) throws MessagingException, IOException {
		boolean match = true;
		
		
		//The attachment filter is ON, let's check
		if(filter.getAttachments() != null) {
			boolean temp = hasAttachments(message);
			
			match = (filter.getAttachments() && temp) || (!filter.getAttachments() && !temp);
		}
		
		return match;
    }
	

 	public static boolean hasAttachments(Part p) throws MessagingException, IOException {
		boolean hasAttchmnt = false;
		
		String contentType = p.getContentType();
		
		if(contentType.toLowerCase().contains("multipart/")) {
			// content may contain attachments
			Multipart multiPart = (Multipart) p.getContent();
			int numberOfParts = multiPart.getCount();
			for (int partCount = 0; partCount < numberOfParts; partCount++) {
				MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
				if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
					hasAttchmnt = true;
					break;
				}else {
					hasAttchmnt = hasAttachments(part);
				}
			}

		}
		
		return hasAttchmnt;
 	}
 	
}
