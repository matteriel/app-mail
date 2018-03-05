package com.itesoft.mtp.consumer.connection;

import com.itesoft.mtp.model.EmailObject;
import com.itesoft.mtp.model.Filter;

public interface ServerMailConnection {

	public void openConnection(final String PROTOCOL, final String HOST, final String USER, final String PASSWORD);
	
	public void closeConnection();
	
	public EmailObject[] perfomFilter(final Filter filter);
}
