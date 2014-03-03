package sonic.xud.assistclass;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

public class MyHttpClient {

	private static HttpClient httpClient;

	private MyHttpClient() {
		super();
	}
	
	public static HttpClient getHttpClient(){
		if(httpClient == null){
			synchronized (MyHttpClient.class) {
				if(httpClient == null){
					httpClient = new DefaultHttpClient();
					return httpClient;
				}
			}
		}
		return httpClient;
	}
	
}
