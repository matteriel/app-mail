package com.itesoft.mtp.consumer.connection.impl;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.itesoft.mtp.consumer.connection.ServerMailConnection;
import com.itesoft.mtp.model.EmailObject;
import com.itesoft.mtp.model.Filter;

import microsoft.exchange.webservices.data.autodiscover.IAutodiscoverRedirectionUrl;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BodyType;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.LogicalOperator;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.EmailMessageSchema;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;
import microsoft.exchange.webservices.data.search.filter.SearchFilter.SearchFilterCollection;

public class ServerMailConnectionEWS implements ServerMailConnection {

	static class RedirectionUrlCallback implements IAutodiscoverRedirectionUrl {
		public boolean autodiscoverRedirectionUrlValidationCallback(
				String redirectionUrl) {
			return redirectionUrl.toLowerCase().startsWith("https://");
		}
	}

	private ExchangeService service;

	private Folder emailFolder;

	private Integer NUMBER_EMAILS_FETCH = 200;


	public ServerMailConnectionEWS() {}

	public ServerMailConnectionEWS(final String PROTOCOL, final String HOST, final String USER, final String PASSWORD) {
		openConnection(PROTOCOL, null, USER, PASSWORD);

	}

	/**
	 * Open the connection with param
	 * @param PROTOCOL	defined protocol to use
	 * @param HOST		server mail (no use for the ews protocol)
	 * @param USER		user
	 * @param PASSWORD	password
	 */
	public void openConnection(final String PROTOCOL, final String HOST, final String USER, final String PASSWORD) {


		service = new ExchangeService();

		ExchangeCredentials credentials = new WebCredentials(USER, PASSWORD);
		service.setCredentials(credentials);

		try {
			service.autodiscoverUrl(USER, new RedirectionUrlCallback());

			//Open connection on the main inBox
			emailFolder = Folder.bind(service, WellKnownFolderName.Inbox);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Close the connection
	 */
	public void closeConnection() {
		service.close();
	}

	/**
	 * Search mails and apply filter
	 * @param filter	filter to use (sender, subject or/and attachments presence)
	 * @return 
	 */
	public EmailObject[] perfomFilter(final Filter filter) {
		EmailObject[] messages = null;

		try {
			ItemView view = new ItemView(NUMBER_EMAILS_FETCH);
			FindItemsResults<Item> findResults = null;

			//set the searchFilter with emails' sender param
			SearchFilter filterFrom = new SearchFilter.ContainsSubstring(EmailMessageSchema.From,filter.getSender());

			//if the filter on the Subject is set, add it to the searchFilter
			if(StringUtils.isNoneBlank(filter.getWordInSubjectField())) {
				SearchFilterCollection searchFilterColl = new SearchFilter.SearchFilterCollection(LogicalOperator.And);
				SearchFilter filterSubject = new SearchFilter.ContainsSubstring(ItemSchema.Subject,filter.getWordInSubjectField());
				searchFilterColl.add(filterFrom);
				searchFilterColl.add(filterSubject);

				findResults = service.findItems(getInboxFolder().getId(), searchFilterColl, view);

			}else {
				findResults = service.findItems(getInboxFolder().getId(), filterFrom, view);

			}

			//Define the part of the itemMail we need for the process
			PropertySet itempropertyset = new PropertySet(BasePropertySet.IdOnly, ItemSchema.Subject, ItemSchema.MimeContent, EmailMessageSchema.From, EmailMessageSchema.Body, EmailMessageSchema.Attachments);
			itempropertyset.setRequestedBodyType(BodyType.Text);

			//Load the properties
			service.loadPropertiesForItems(findResults, itempropertyset);

			//for each item of the result
			List<EmailObject> temp = new ArrayList<EmailObject>();
			for (Item item : findResults) {
				EmailMessage emailMessage = EmailMessage.bind(service, item.getId());
				String from = emailMessage.getFrom().getAddress().toString();

				//Appli the emails' sender filter
				if(StringUtils.equalsIgnoreCase(from, filter.getSender())){

					//Appli the presence of attachments filter
					if(matchAttachmentFilterCriteria(emailMessage, filter)) {

						//Prepare the object for the business layer
						emailMessage.load(itempropertyset);
						EmailObject email = new EmailObject("ews", emailMessage);
						temp.add(email);
					}
				}
			}

			if(!temp.isEmpty()) {
				EmailObject[] result = new EmailObject[temp.size()];
				messages = temp.toArray(result);
			}


		}catch (Exception e) {
			e.printStackTrace();
		}

		return messages;
	}

	private Folder getInboxFolder() throws Exception {
		if(emailFolder == null && service != null)
			emailFolder = Folder.bind(service, WellKnownFolderName.Inbox);
		return emailFolder;
	}



	private boolean matchAttachmentFilterCriteria(EmailMessage message, Filter filter) throws ServiceLocalException {
		boolean match = true;

		if(filter.getAttachments() != null) {
			match = (filter.getAttachments() && message.getHasAttachments()) 
					|| (!filter.getAttachments() && !message.getHasAttachments());
		}

		return match;
	}
}

