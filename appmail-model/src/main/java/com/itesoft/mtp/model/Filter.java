package com.itesoft.mtp.model;

import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;

public class Filter {
	
	public Filter(String sender, Boolean attachments, String word) {
		this.sender = sender;
		this.attachments = attachments;
		this.wordInSubjectField = word;
	}
	
	public Filter(String sender, Boolean attachments) {
		this.sender = sender;
		this.attachments = attachments;
	}
	
	public Filter(String sender, String word) {
		this.sender = sender;
		this.wordInSubjectField = word;
	}
	
	public Filter(String sender) {
		this.sender = sender;
	}

	public Filter() {
		
	}

	@NotBlank(message = "Sender's filter cannot be null")
	private String sender;
	
	private Boolean attachments;
	
	private String wordInSubjectField;

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public Boolean getAttachments() {
		return attachments;
	}

	public void setAttachments(Boolean attachments) {
		this.attachments = attachments;
	}
	
	public void setAttachments(String filterOnAttachments) {
		
		if (StringUtils.equalsIgnoreCase(filterOnAttachments, "false"))
			attachments = false;
		else if (StringUtils.equalsIgnoreCase(filterOnAttachments, "true"))
			attachments = true;
		else
			attachments = null;
	}

	public String getWordInSubjectField() {
		return wordInSubjectField;
	}

	public void setWordInSubjectField(String wordInSubjectField) {
		this.wordInSubjectField = wordInSubjectField;
	}
	
	
}
