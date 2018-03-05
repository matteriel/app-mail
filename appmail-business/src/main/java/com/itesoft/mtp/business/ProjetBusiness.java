package com.itesoft.mtp.business;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;

import org.apache.commons.lang3.StringUtils;

import com.itesoft.mtp.business.validator.BusinessValidator;
import com.itesoft.mtp.consumer.connection.ServerMailConnection;
import com.itesoft.mtp.consumer.connection.impl.ServerMailConnectionEWS;
import com.itesoft.mtp.consumer.connection.impl.ServerMailConnectionGlobal;
import com.itesoft.mtp.consumer.email.MailsManager;
import com.itesoft.mtp.consumer.json.JsonManager;
import com.itesoft.mtp.model.EmailObject;
import com.itesoft.mtp.model.Filter;
import com.itesoft.mtp.model.IndexMailJson;

public class ProjetBusiness {
	

	public static void main( String[] args ) throws Exception
	{
		
		// ------------------------------------------------
		// Step 1 : Get Properties (Connection + Filter)
		// ------------------------------------------------

		Properties appProperties = new Properties();
		
		//Load application.properties
		String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		String appConfigPath = rootPath + "application.properties";
		appProperties.load(new FileInputStream(appConfigPath));

		//get pool connection settings
		final String procotol = appProperties.getProperty("connection.servermail.procotol");
		final String host = appProperties.getProperty("connection.servermail.host");
		final String user = appProperties.getProperty("connection.servermail.user");
		final String password = appProperties.getProperty("connection.servermail.password");
		
		//get filter settings
		final String filterFrom = appProperties.getProperty("mail.filter.from");
		final String filterSubject = appProperties.getProperty("mail.filter.subject");
		final String filterAttachment = appProperties.getProperty("mail.filter.hasattachment");
		
		Filter filter = new Filter(filterFrom);
		filter.setWordInSubjectField(filterSubject);
		filter.setAttachments(filterAttachment);
		
		BusinessValidator.getInstance().validate(filter);
		
		//Root directory name for backup mails
		final String rootDirectoryName = appProperties.getProperty("mail.backup.directoryName");
		
		
		ServerMailConnection connection;
		try {
			 
			// ------------------------------------------------
			// Step 2 : Retrieve Filtered Mail
			// ------------------------------------------------
			
			if(StringUtils.equalsIgnoreCase(procotol, "ews")){
				connection = new ServerMailConnectionEWS();
			}else {
				connection = new ServerMailConnectionGlobal();
			}
			
			//start connection
			connection.openConnection(procotol, host, user, password);
			
			//Query for mails with filter
			EmailObject[] emailObjects = connection.perfomFilter(filter);

			//Check for result
			if(null != emailObjects && emailObjects.length > 0) {


				// ------------------------------------------------
				// Step 3 : Creation of the directory
				// ------------------------------------------------
				
				final String directoryName = rootDirectoryName + filter.getSender();
				File directory = new File(directoryName);
				
				//Clean directory
				if(directory.exists() && directory.isDirectory()){

					File[] allContents = directory.listFiles();
					if (allContents != null) {
						for (File file : allContents) {
							if(file.isFile()) {
								file.delete();
							}
						}
					}
					directory.delete();
				}

				//Check if directory was removed correctly
				Path pathToDirectory = Paths.get(directoryName);
				if(Files.exists(pathToDirectory))
					throw new Exception("Impossible de supprimer le dossier");

				Files.createDirectory(pathToDirectory);

				
				// ------------------------------------------------
				// Step 4 : Save emails
				// ------------------------------------------------
				
				List<IndexMailJson> index = new ArrayList<IndexMailJson>();
				for (int i = 0; i < emailObjects.length; i++) {
					
					EmailObject message = emailObjects[i];
					String fileName = "\\" + String.valueOf(i);
					
					// 1 - mail format EML
					MailsManager.saveMailToEMLformat(message, pathToDirectory, fileName);
					
					// 2 - mail subject + body > fichier.txt
					MailsManager.saveMailToTXTformat(message, pathToDirectory, fileName);
					
					// 3 - attachments (<numéro>_<nom pièce  jointe>.<extension pièce jointe>)
					if(StringUtils.isNoneBlank(String.valueOf(filter.getAttachments())) || (filter.getAttachments() != null && filter.getAttachments()))
						MailsManager.saveAttachments(message, pathToDirectory, fileName);

					// 4 - Update index
					IndexMailJson item = new IndexMailJson();
					item.setId(i);
					item.setSubject(message.getSubject());
					index.add(item);

				}


				// ------------------------------------------------
				// Step 6 : Creation of the JSON index
				// ------------------------------------------------

				String fileNameJson = directoryName + "\\index.json";
				Path pathIndex = Paths.get(fileNameJson);
				JsonManager.writeIndexJson(index, pathIndex);


			}

			//End of connection
			connection.closeConnection();
			System.exit(0);

		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


