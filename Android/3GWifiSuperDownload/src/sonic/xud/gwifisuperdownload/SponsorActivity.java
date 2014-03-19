package sonic.xud.gwifisuperdownload;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.concurrent.ThreadPoolExecutor;

import sonic.xud.assistclass.FinalDataSource;
import sonic.xud.assistclass.Signal;
import sonic.xud.assistclass.SingletonThreadPool;
import sonic.xud.assistclass.SponsorUdp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SponsorActivity extends Activity {

	private Context context;
	private Button beginButton;
	private TextView countView;

	private final int ASSISTCOUNT = 1000;
	private final int DECODESUCCESS = 1001;
	private int udpPort;
	private Socket controlSocket = null;
	private DatagramSocket mobileUdpSocket = null;
	private SponsorUdp mobileUdp = null;
	private ThreadPoolExecutor threadPool;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == ASSISTCOUNT) {
				int count = (Integer) msg.obj;
				countView.setText(String.valueOf(count));
			} else if (msg.what == DECODESUCCESS) {

			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sponsor);
		context = SponsorActivity.this;
		init();
	}

	private void init() {
		File file = new File(Signal.sponsorPath);
		if (!file.exists()) {
			file.mkdirs();
		}
		threadPool = SingletonThreadPool.getThreadPool();
		countView = (TextView) findViewById(R.id.count);
		beginButton = (Button) findViewById(R.id.beginBtn);
		beginButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				new Thread(new Runnable() {

					public void run() {
						// TODO Auto-generated method stub
						try {
							// 建立tcp连接
							controlSocket = new Socket(FinalDataSource.DATASERVERIP,
									FinalDataSource.CONTROLPORT);
							ControlTcp controlTcp = new ControlTcp(controlSocket);

							// 启动udp监听
							Thread udpThread = new Thread(new Runnable() {

								public void run() {
									// TODO Auto-generated method stub
									try {
//										String gprsIp = MobileIp.gprsIp(context);
//										System.out.println("gprsIp: "+gprsIp);
//										InetSocketAddress socketAddress = new InetSocketAddress(
//												gprsIp,udpPort);
										mobileUdpSocket = new DatagramSocket();
										mobileUdp = new SponsorUdp(mobileUdpSocket);
										while (true) {
											if (Signal.IfReadyToGetdata && !Signal.IfGetAllFile) {
												mobileUdp.send();
												mobileUdp.receive();
											}
										}
									} catch (Exception e) {
										// TODO: handle exception
										e.printStackTrace();
									}
								}
							});

							threadPool.execute(controlTcp);
							threadPool.execute(udpThread);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}).start();
			}
		});
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			try {
				controlSocket.close();
				mobileUdp.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	class ControlTcp extends Thread {
		private Socket socket;

		public ControlTcp(Socket socket) {
			super();
			this.socket = socket;
			System.out.println("tcp连接已经建立");
		}

		@Override
		public void run() {
			DataInputStream br = null;
			DataOutputStream pw = null;
			try {
				br = new DataInputStream(new BufferedInputStream(
						socket.getInputStream()));
				pw = new DataOutputStream(new BufferedOutputStream(
						socket.getOutputStream()));
				// register,and send request
				pw.writeUTF("request");
				pw.flush();

				Signal.fileName = br.readUTF();
				Signal.fileSize = br.readLong();
				udpPort = br.readInt();
				Signal.filePath = Signal.sponsorPath + Signal.fileName;
				Signal.blockcount = (int) Math.ceil((double) Signal.fileSize
						/ (double) Signal.blockSize);
				System.out.println("file info:" + Signal.fileName + "length:"
						+ Signal.fileSize);
				System.out.println("file save:" + Signal.filePath);
				System.out.println("udpPort:" + udpPort);
				Signal.notifyGetdata(true);

				// keep connect
				while (true) {
					if (Signal.IfReadyGetNextBlock) {
						pw.writeUTF("success");
						pw.flush();
						Signal.notifyGetNextBlock(false);
					}
					if (Signal.IfGetAllFile) {
						pw.writeUTF("terminate");
						pw.flush();
						break;
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

	}
}
