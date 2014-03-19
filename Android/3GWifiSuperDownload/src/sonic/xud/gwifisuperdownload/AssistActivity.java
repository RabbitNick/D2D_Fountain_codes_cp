package sonic.xud.gwifisuperdownload;

import java.io.File;
import java.io.FileWriter;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedHashMap;

import sonic.xud.assistclass.MobileIp;
import sonic.xud.assistclass.Signal;
import sonic.xud.assistclass.TransmitHandler;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AssistActivity extends Activity {

	private Context context;
	private Button beginButton;
	private ProgressBar downloadBar, transBar;
	private TextView downloadSpeedView, transSpeedView;

	private HashMap<Long, Long> speedMap = new LinkedHashMap<Long, Long>();
	private long priorTime = 0, currentTime = 0;

	// private static String TAG_LOG = "AssistActivity";

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			currentTime = System.currentTimeMillis();
			Bundle data = (Bundle) msg.obj;
			long size = data.getLong("size");
			long speed = 1000*size/(currentTime-priorTime);
			speedMap.put(currentTime, speed);
			downloadSpeedView.setText(String.valueOf(speed));
			transSpeedView.setText(String.valueOf(speed));
			priorTime = currentTime;
		}
	};

	// 一次性写入文件
	private void write(HashMap<Long, Long> value) {
		try {
			File file = new File(Signal.speedPath + "assistspeed.txt");
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
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.assist);
		init();
	}

	private void init() {
		context = AssistActivity.this;
		File file = new File(Signal.AssistPath);
		if (!file.exists()) {
			file.mkdirs();
		}
		File file2 = new File(Signal.speedPath);
		if (!file2.exists()) {
			file2.mkdirs();
		}
		downloadBar = (ProgressBar) findViewById(R.id.downloadbar);
		transBar = (ProgressBar) findViewById(R.id.transbar);
		downloadSpeedView = (TextView) findViewById(R.id.downloadspeed);
		transSpeedView = (TextView) findViewById(R.id.transmitspeed);

		beginButton = (Button) findViewById(R.id.beginBtn);
		beginButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				new Thread(new Runnable() {

					public void run() {
						// TODO Auto-generated method stub
						priorTime = System.currentTimeMillis();
						currentTime = System.currentTimeMillis();
						service();
					}
				}).start();
			}
		});
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		write(speedMap);
	}

	// 开启转发服务
	private void service() {
		try {
			InetSocketAddress inAddress = new InetSocketAddress(
					MobileIp.gprsIp(context), 8001);
			DatagramSocket insoSocket = new DatagramSocket(inAddress);
			System.err.println("数据下载udp监听开启： " + insoSocket);
			InetSocketAddress outAddress = new InetSocketAddress(
					MobileIp.wifiIp(context), 8001);
			DatagramSocket outSocket = new DatagramSocket(outAddress);
			System.err.println("数据转发udp监听开启： " + insoSocket);
			TransmitHandler transmitHandler = new TransmitHandler(insoSocket,
					outSocket, handler, context);
		} catch (Exception e) {
			// TODO: handle exception
		}

	}
}
