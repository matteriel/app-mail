package com.itesoft.mtp.model;

import javax.mail.Message;
import javax.mail.MessagingException;

import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;

public class EmailObject {

	private String PROTOCOL;

	private Object message;
	
	public EmailObject(String protocol) {
		this.PROTOCOL = protocol;
	}

	public EmailObject(String protocol, Object emailMessage) {
		this.PROTOCOL = protocol;
		message = emailMessage;
	}

	public Object getMessage() {
		return message;
	}

	public void setMessage(Object message) {
		this.message = message;
	}
	
	public String getProtocol() {
		return PROTOCOL;
	}
	
	public String getSubject() {
		String result = "";
		try {
			
			if("ews".equals(PROTOCOL)) {
				result = ((EmailMessage) message).getSubject();
				
			}else {
				result = ((Message) message).getSubject();
				
			}
			
		} catch (ServiceLocalException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} 
		
		return result;
	}


}
