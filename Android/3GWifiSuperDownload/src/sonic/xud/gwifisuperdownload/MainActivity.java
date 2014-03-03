package sonic.xud.gwifisuperdownload;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;

import sonic.xud.assistclass.IPv4Util;
import sonic.xud.assistclass.MyHttpClient;

import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener{

	private Context context;
	private Button activateBtn,sponsorBtn,assistBtn;
	private TextView wifiStateView,mobileStateView,testView;
	private LinearLayout testLayout;
	
	private final String TAG_LOG = "MobileConnect";
	private final int MOBILE = 1;
	private final int TESTRESULT = 2;
	private WifiManager wifiManager;
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(msg.what == MOBILE){
				String mobileIp = (String) msg.obj;
				mobileStateView.setText("3G已连接\n"+mobileIp);
			}else if(msg.what == TESTRESULT){
				String test = (String)msg.obj;
				if(test.equals("success")){
					testLayout.setVisibility(View.VISIBLE);
					testView.setText("一切正常");
				}else{
					testView.setText("出现问题，请先检查");
				}		
			}		
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = MainActivity.this;
        
        initView();
    } 
    
    private void initView(){
    	wifiStateView = (TextView)findViewById(R.id.wifiState);
    	mobileStateView = (TextView)findViewById(R.id.mobileState);
    	testView = (TextView)findViewById(R.id.testState);
    	testLayout = (LinearLayout)findViewById(R.id.testLayout);
    	testLayout.setVisibility(View.GONE);
    	
    	sponsorBtn = (Button)findViewById(R.id.sponsorBtn);
    	assistBtn = (Button)findViewById(R.id.assistBtn);
    	activateBtn = (Button)findViewById(R.id.activateBtn);
    	
    	activateBtn.setOnClickListener(this);
    	sponsorBtn.setOnClickListener(this);
    	assistBtn.setOnClickListener(this);
    	
    	/*判断wifi的连接状态*/
    	wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (!wifiManager.isWifiEnabled()) {
			wifiStateView.setText("wifi未连接");
		} else {
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			String ssid = wifiInfo.getSSID();
			int ip = wifiInfo.getIpAddress();
			Log.d(TAG_LOG, "wifi ip：" + ip);
			String ipString = IPv4Util.intToIp(ip);
			Log.d(TAG_LOG, "wifi ip：" + ipString);
			String state = "wifi连接" + "\n" + ssid + "\n" + ipString;
			wifiStateView.setText(state);
		}
		
		/*3G mobile连接状态判断*/
		String mobileIp = getIP();
		if(mobileIp.equals("")){
			mobileStateView.setText("3G未连接");
		}else{
			mobileStateView.setText("3G连接"+"\n"+mobileIp);
		}
    }

    public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.activateBtn:
		{
			new Thread(new Runnable() {
				
				public void run() {
					// TODO Auto-generated method stub
					forceMobileConnectionForAddress(context, "http://www.taobao.com");
					Post("http://www.taobao.com", null);
				}
			}).start();
		}
			break;
		case R.id.sponsorBtn:
			Intent intent1 = new Intent(MainActivity.this,SponsorActivity.class);
			startActivity(intent1);
			break;
		case R.id.assistBtn:
			Intent intent2 = new Intent(MainActivity.this,AssistActivity.class);
			startActivity(intent2);
			break;

		default:
			break;
		}
	}
    
    private String getIP() {
		String IP = null;
		StringBuilder IPStringBuilder = new StringBuilder();
		try {
			Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface
					.getNetworkInterfaces();
			while (networkInterfaceEnumeration.hasMoreElements()) {
				NetworkInterface networkInterface = networkInterfaceEnumeration
						.nextElement();
				Enumeration<InetAddress> inetAddressEnumeration = networkInterface
						.getInetAddresses();
				while (inetAddressEnumeration.hasMoreElements()) {
					InetAddress inetAddress = inetAddressEnumeration
							.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& !inetAddress.isLinkLocalAddress()
							&& inetAddress.isSiteLocalAddress()) {
						IPStringBuilder.append(inetAddress.getHostAddress()
								.toString() + "\n");
					}
				}
			}
		} catch (SocketException ex) {

		}
		IP = IPStringBuilder.toString();
		System.out.println("Mobile ip：" + IP);
		Log.d(TAG_LOG, "Mobile ip：" + IP);
		return IP;
	}

    private boolean forceMobileConnectionForAddress(Context context,
			String address) {
		Log.d(TAG_LOG, "Begin force");
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (null == connectivityManager) {
			Log.d(TAG_LOG,
					"ConnectivityManager is null, cannot try to force a mobile connection");
			return false;
		}

		State state = connectivityManager.getNetworkInfo(
				ConnectivityManager.TYPE_MOBILE_HIPRI).getState();
		Log.d(TAG_LOG, "TYPE_MOBILE_HIPRI network state: " + state);
		if (0 == state.compareTo(State.CONNECTED)
				|| 0 == state.compareTo(State.CONNECTING)) {
			return true;
		}

		int resultInt = connectivityManager.startUsingNetworkFeature(
				ConnectivityManager.TYPE_MOBILE, "enableHIPRI");
		Log.d(TAG_LOG, "startUsingNetworkFeature for enableHIPRI result: "
				+ resultInt);

		if (-1 == resultInt) {
			Log.d(TAG_LOG,
					"Wrong result of startUsingNetworkFeature, maybe problems");
			return false;
		}
		if (0 == resultInt) {
			Log.d(TAG_LOG, "No need to perform additional network settings");
			return true;
		}
		if (1 == resultInt) {
			Log.d(TAG_LOG, "startUsingNetworkFeature:enabled");
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String mobileIp = getIP();
			Message msg = new Message();
			msg.what = MOBILE;
			msg.obj = mobileIp;
			handler.sendMessage(msg);
		}

		// find the host name to route
		String hostName = extractAddressFromUrl(address);
		Log.d(TAG_LOG, "Source address: " + address);
		Log.d(TAG_LOG, "Destination host address to route: " + hostName);
		if (TextUtils.isEmpty(hostName))
			hostName = address;

		// create a route for the specified address
		int hostAddress = lookupHost(hostName);
		Log.d(TAG_LOG, String.valueOf(hostAddress));
		Log.d(TAG_LOG, IPv4Util.intToIp(hostAddress));
		if (-1 == hostAddress) {
			Log.d(TAG_LOG, "Wrong host address transformation, result was -1");
			return false;
		}
		// wait some time needed to connection manager for waking up
		try {
			for (int counter = 0; counter < 30; counter++) {
				State checkState = connectivityManager.getNetworkInfo(
						ConnectivityManager.TYPE_MOBILE_HIPRI).getState();
				if (0 == checkState.compareTo(State.CONNECTED))
					break;
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			// nothing to do
		}
		boolean resultBool = connectivityManager.requestRouteToHost(
				ConnectivityManager.TYPE_MOBILE_HIPRI, hostAddress);
		Log.d(TAG_LOG, "requestRouteToHost result: " + resultBool);
		if (!resultBool)
			Log.d(TAG_LOG,
					"Wrong requestRouteToHost result: expected true, but was false");

		return resultBool;
	}

	/**
	 * This method extracts from address the hostname
	 * @param url eg. http://some.where.com:8080/sync
	 * @return some.where.com
	 */
	public static String extractAddressFromUrl(String url) {
		String urlToProcess = null;

		// find protocol
		int protocolEndIndex = url.indexOf("://");
		if (protocolEndIndex > 0) {
			urlToProcess = url.substring(protocolEndIndex + 3);
		} else {
			urlToProcess = url;
		}

		// If we have port number in the address we strip everything
		// after the port number
		int pos = urlToProcess.indexOf(':');
		if (pos >= 0) {
			urlToProcess = urlToProcess.substring(0, pos);
		}

		// If we have resource location in the address then we strip
		// everything after the '/'
		pos = urlToProcess.indexOf('/');
		if (pos >= 0) {
			urlToProcess = urlToProcess.substring(0, pos);
		}

		// If we have ? in the address then we strip
		// everything after the '?'
		pos = urlToProcess.indexOf('?');
		if (pos >= 0) {
			urlToProcess = urlToProcess.substring(0, pos);
		}
		return urlToProcess;
	}

	/**
	 * Transform host name in int value used by
	 * {@link ConnectivityManager.requestRouteToHost} method
	 * 
	 * @param hostname
	 * @return -1 if the host doesn't exists, elsewhere its translation to an
	 *         integer
	 */
	private static int lookupHost(String hostname) {
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getByName(hostname);
		} catch (UnknownHostException e) {
			return -1;
		}
		byte[] addrBytes;
		int addr;
		addrBytes = inetAddress.getAddress();
		addr = ((addrBytes[3] & 0xff) << 24) | ((addrBytes[2] & 0xff) << 16)
				| ((addrBytes[1] & 0xff) << 8) | (addrBytes[0] & 0xff);
		return addr;
	}
	
	/* http请求 */
	private void Post(final String url, final List<NameValuePair> params) {
		Log.d(TAG_LOG, "Begin Http Test....");
		HttpPost httpRequest = new HttpPost(url);
		HttpClient httpClient = MyHttpClient.getHttpClient();
		String strResult = "doPostError";
		try {
			/* 添加请求参数到请求对象 */
			// httpRequest.setEntity(new UrlEncodedFormEntity(params,
			// HTTP.UTF_8));
			/* 发送请求并等待响应 */
			HttpResponse httpResponse = httpClient.execute(httpRequest);

			int i = httpResponse.getStatusLine().getStatusCode();
			Log.d(TAG_LOG, "Http Test: " + String.valueOf(i));

			if (i == 200) {
				System.out.println("开始解析JSON数据");
				String Result = EntityUtils.toString(httpResponse.getEntity());
				Log.d(TAG_LOG, "Http Test catResult: " + Result);
				Message msg = new Message();
				msg.what = TESTRESULT;
				msg.obj = "success";
				handler.sendMessage(msg);
			} else {
				strResult = "Error Response: "
						+ httpResponse.getStatusLine().toString();
				Log.d(TAG_LOG, "Http Test catResult error info: " + strResult);
			}
		} catch (ClientProtocolException e) {
			strResult = e.getMessage().toString();
			e.printStackTrace();
		} catch (IOException e) {
			strResult = e.getMessage().toString();
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

 
}
