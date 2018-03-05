package com.itesoft.mtp.consumer.connection.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.SearchException;
import javax.mail.search.SearchTerm;

import com.itesoft.mtp.consumer.connection.ServerMailConnection;
import com.itesoft.mtp.consumer.email.utils.FilterMails;
import com.itesoft.mtp.model.EmailObject;
import com.itesoft.mtp.model.Filter;

public class ServerMailConnectionGlobal implements ServerMailConnection {

	private Store store;
	private Folder emailFolder;
	private String protocol = "";


	public ServerMailConnectionGlobal() {}

	public ServerMailConnectionGlobal(final String PROTOCOL, final String HOST, final String USER, final String PASSWORD) throws NoSuchProviderException, MessagingException {
		openConnection(PROTOCOL, HOST, USER, PASSWORD);
	}


	/**
	 * Open the connection with param
	 * @param PROTOCOL	defined protocol to use
	 * @param HOST		server mail
	 * @param USER		user
	 * @param PASSWORD	password
	 */
	public void openConnection(final String PROTOCOL, final String HOST, final String USER, final String PASSWORD) {
		Properties properties = new Properties();
		protocol = PROTOCOL;

		//Load the protocol settings
		if(protocol.equalsIgnoreCase("imaps")) {
			properties = getImapSetting();

		}else if(protocol.equalsIgnoreCase("pop3")) {
			properties = getPopSetting();
		}

		//Open the session with login
		Session emailSession = Session.getInstance(properties,
				new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(USER, PASSWORD);
			}
		});

		//create the store object and connect with the pop server
		try {
			if("pop3".equals(protocol))
				store = emailSession.getStore("pop3s");
			else
				store = emailSession.getStore();
			
			//And finally, open the connection
			store.connect(HOST, USER, PASSWORD);
			
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Close the connection (need to close the inboxFolder, and the store object)
	 */
	public void closeConnection() {
		//close the store and folder objects
		try {
			if(emailFolder != null && emailFolder.isOpen()) 
				closeInboxFolder();

			if(store != null && store.isConnected())
				closeStore();

		} catch (MessagingException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Search mails and apply filter
	 * @param filter	filter to use (sender, subject or/and attachments presence)
	 * @return 
	 */
	public EmailObject[] perfomFilter(final Filter filter) {
		EmailObject[] messages = null;
		
		try {
			//set the searchFilter with emails' sender param and subject param (if set)
			SearchTerm searchTerm = FilterMails.getSearchWithCriteria(filter);
			
			//get Mail Item
			Message[] resultQuery = getInboxFolder().search(searchTerm);

			//for each item of the result
			List<EmailObject> temp = new ArrayList<EmailObject>();
			for (int i = 0; i < resultQuery.length; i++) {
				
				//Appli the presence of attachments filter
				if(FilterMails.matchAttachmentFilterCriteria(resultQuery[i], filter)) {
					
					//Prepare the object for the business layer
					EmailObject email = new EmailObject(protocol, resultQuery[i]);
					temp.add(email);
				}
			}

			if(!temp.isEmpty()) {
				EmailObject[] result = new EmailObject[temp.size()];
				messages = temp.toArray(result);
			}

		}catch( SearchException e ) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return messages;
	}


	private Folder getInboxFolder() throws MessagingException {
		if(emailFolder == null || !emailFolder.isOpen()) {
			//create the folder object and open it
			if(store != null && store.isConnected()) {
				emailFolder = store.getFolder("INBOX");
				emailFolder.open(Folder.READ_ONLY);
			}else {
				throw new MessagingException("Cannot get Folder : Store not found");
			}

		}
		return emailFolder;
	}


	private void closeInboxFolder() throws MessagingException {
		emailFolder.close(false);
		emailFolder = null;
	}

	private void closeStore() throws MessagingException {
		store.close();
	}

	private Properties getImapSetting() {
		String PORT = "995";

		Properties props = new Properties();
		props.setProperty("mail.store.protocol", protocol);

		props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.setProperty("mail.imap.socketFactory.fallback", "false");
		props.setProperty("mail.imap.ssl.enable", "true");
		props.setProperty("mail.imap.socketFactory.port", PORT);
		props.setProperty("mail.imap.starttls.enable", "true");

		return props;
	}

	private Properties getPopSetting() {
		String PORT = "993";

		Properties props = new Properties();
		props.setProperty("mail.store.protocol", protocol);

		props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.setProperty("mail.pop3.socketFactory.fallback", "false");
		props.setProperty("mail.pop3.ssl.enable", "true");
		props.setProperty("mail.pop3.socketFactory.port", PORT);
		props.setProperty("mail.pop3s.port", "995");
		props.setProperty("mail.pop3.starttls.enable", "true");
		
		props.setProperty("mail.store.protocol", protocol);
		
		

		return props;
	}


}
