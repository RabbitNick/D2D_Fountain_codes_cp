package sonic.xud.gwifisuperdownload;

import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import sonic.xud.assistclass.IPv4Util;
import sonic.xud.assistclass.MobileIp;

import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
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

public class MainActivity extends Activity implements OnClickListener {

	private Context context;
	private Button activateBtn, sponsorBtn, assistBtn;
	private TextView wifiStateView, mobileStateView, testView;
	private LinearLayout testLayout;

	private final String TAG_LOG = "MobileConnect";
	private final int MOBILE = 1;
	private final int TESTRESULT = 2;
	private final int SPEED = 3;
	private WifiManager wifiManager;

	private static File path = Environment.getExternalStorageDirectory();
	private static String AbsolutePath = path.getPath() + File.separator
			+ "superdown" + File.separator;
	private static String AssistPath = AbsolutePath + "assist/";
	private static String speedPath = AbsolutePath + "speed/";

	private HashMap<Long, Long> speedMap = new LinkedHashMap<Long, Long>();

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == MOBILE) {
				String mobileIp = (String) msg.obj;
				if(mobileIp != null){
					mobileStateView.setText("3G已连接\n" + mobileIp);
					testLayout.setVisibility(View.VISIBLE);
					testView.setText("一切正常");
				}else{
					testLayout.setVisibility(View.VISIBLE);
					testView.setText("出现问题，请先检查");
				}
				
			}else if (msg.what == SPEED) {
				Bundle data = (Bundle) msg.obj;
				long count = data.getLong("count");
				long timePoint = System.currentTimeMillis();
				speedMap.put(timePoint, count);
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

	private void initView() {
		File file = new File(AssistPath);
		if (!file.exists()) {
			file.mkdirs();
		}
		File file2 = new File(speedPath);
		if (!file2.exists()) {
			file2.mkdirs();
		}
		wifiStateView = (TextView) findViewById(R.id.wifiState);
		mobileStateView = (TextView) findViewById(R.id.mobileState);
		testView = (TextView) findViewById(R.id.testState);
		testLayout = (LinearLayout) findViewById(R.id.testLayout);
		testLayout.setVisibility(View.GONE);

		sponsorBtn = (Button) findViewById(R.id.sponsorBtn);
		assistBtn = (Button) findViewById(R.id.assistBtn);
		activateBtn = (Button) findViewById(R.id.activateBtn);

		activateBtn.setOnClickListener(this);
		sponsorBtn.setOnClickListener(this);
		assistBtn.setOnClickListener(this);

		/* 判断wifi的连接状态 */
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (!wifiManager.isWifiEnabled()) {
			wifiStateView.setText("wifi未开启");
		} else {
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			String ssid = wifiInfo.getSSID();
			int ip = wifiInfo.getIpAddress();
			Log.d(TAG_LOG, "wifi ip before change：" + ip);
			String ipString = IPv4Util.intToIp(ip);
			Log.d(TAG_LOG, "wifi ip：" + ipString);
			String state = "wifi已连接" + "\n" + ssid + "\n" + ipString;
			wifiStateView.setText(state);
		}

		/* 3G mobile连接状态判断 */
		String mobileIp = MobileIp.gprsIp(context);
		if (mobileIp == null) {
			mobileStateView.setText("3G未开启");
		} else {
			mobileStateView.setText("3G已连接" + "\n" + mobileIp);
		}
	}

	// 一次性写入文件
	private void write(HashMap<Long, Long> value) {
		try {
			File file = new File(speedPath + "downloadspeed.txt");
			// 采用覆盖的方式写入
			FileWriter fileWriter = new FileWriter(file, false);
			fileWriter.write(value.toString());
			fileWriter.flush();
			fileWriter.close();
			value.clear();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		write(speedMap);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.activateBtn: {
			new Thread(new Runnable() {

				public void run() {
					//强制启动mobile 连接
					forceMobileConnectionForAddress(context,"218.193.131.2:8001");
				}
			}).start();
		}
			break;
		case R.id.sponsorBtn:
			Intent intent1 = new Intent(MainActivity.this,
					SponsorActivity.class);
			startActivity(intent1);
			break;
		case R.id.assistBtn:
			Intent intent2 = new Intent(MainActivity.this, AssistActivity.class);
			startActivity(intent2);
			break;

		default:
			break;
		}
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
			String mobileIp = MobileIp.gprsIp(context);
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
	 * 
	 * @param url
	 *            eg. http://some.where.com:8080/sync
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

}
