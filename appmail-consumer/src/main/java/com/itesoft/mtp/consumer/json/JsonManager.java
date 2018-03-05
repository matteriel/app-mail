package com.itesoft.mtp.consumer.json;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonManager {

	/**
	 * JSON utilitaire : 	write an index file
	 * @param list			items to index
	 * @param path			path where to save the index.json
	 */
	public static <T> void writeIndexJson(List<T> list, Path path) throws Exception {

		BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonIndexMails = gson.toJson(list);
		try {
			writer.write(jsonIndexMails);
		}
		finally {
			if (writer != null) { writer.flush(); writer.close(); }
		}

	}
}
