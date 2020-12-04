package com.osuelo.osuelo.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osuelo.osuelo.config.APIHandler;


/*
 * Helper class for performing HTTP Requests
 */
public class DataScraper {
	
	//Performs a get request using an endpoint combined with given parameters.
	//Returns a JsonNode and currently does not support XML
	public static JsonNode performGetRequest(String endpoint, Map<String, String> parameters) {
		try {
			APIHandler.rateLimiter.acquire();
			String fullUrl = endpoint;
			//Combines every parameter in a k1=v1&k2=v2&... format using the given Map
			if(parameters != null) {
				fullUrl += "?";
				Set<String> keys = parameters.keySet();
				for(String key : keys) {
					fullUrl += key + "=" + parameters.get(key) + "&";
				}
				fullUrl = fullUrl.substring(0, fullUrl.length() - 1);
			}
			//Spaces don't always work, so they are replaced with the ascii code equivalent
			fullUrl = fullUrl.replaceAll(" ", "%20");
			URL url = new URL(fullUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			InputStream is = connection.getInputStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		    StringBuffer response = new StringBuffer();
		    String line;
		    while ((line = rd.readLine()) != null) {
		      response.append(line);
		      response.append('\r');
		    }
		    rd.close();
		    ObjectMapper mapper = new ObjectMapper();
		    return mapper.readTree(response.toString());
		}
		catch(IOException e) {
			//Used to catch exception when challonge api request times out
			System.out.println(e.getMessage().substring(36, 39));
			if(e.getMessage().substring(36, 39).equals("502"))
				return performGetRequest(endpoint, parameters);
			else
				e.printStackTrace();
			return null;
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
