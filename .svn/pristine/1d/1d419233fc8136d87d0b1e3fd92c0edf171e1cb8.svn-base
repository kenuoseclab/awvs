package msl.utils;

import java.io.IOException;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

public class SiteUtils {

	public static StatusLine getCode(String url) throws IOException{
		return HttpClients.createDefault().execute(new HttpGet(url)).getStatusLine();
	}
}
