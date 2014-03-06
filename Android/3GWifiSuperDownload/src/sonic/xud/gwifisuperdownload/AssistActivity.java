package sonic.xud.gwifisuperdownload;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;

import sonic.xud.assistclass.FinalDataSource;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
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
	private ProgressBar downloadBar,transBar;
	private TextView downloadSpeedView,transSpeedView;
	private static File path = Environment.getExternalStorageDirectory();
	private static String AssistPath = path.getPath() + File.separator
			+ "superdown" + File.separator + "assist/";

	private Socket transSocket = null;
	private DataInputStream transBr = null;
	private DataOutputStream transPw = null;
	
	private static String TAG_LOG = "AssistActivity";
	
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			Bundle data = (Bundle)msg.obj;
			long speed = data.getLong("speed");
			System.out.println("文件接收了" + speed + "%\n");
			downloadBar.setVisibility(View.VISIBLE);
			transBar.setVisibility(View.VISIBLE);
			downloadBar.setProgress((int)speed);
			transBar.setProgress((int)speed);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.assist);
		init();
	}

	private void init() {
		context = AssistActivity.this;
		File file = new File(AssistPath);
		if (!file.exists()) {
			file.mkdirs();
		}
		downloadBar = (ProgressBar)findViewById(R.id.downloadbar);
		transBar = (ProgressBar)findViewById(R.id.transbar);
		downloadSpeedView = (TextView)findViewById(R.id.downloadspeed);
		transSpeedView = (TextView)findViewById(R.id.transmitspeed);
		
		beginButton = (Button) findViewById(R.id.beginBtn);
		beginButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				new Thread(new Runnable() {

					public void run() {
						// TODO Auto-generated method stub
						try {
							transSocket = new Socket(FinalDataSource.getSponsorip(), FinalDataSource.getSPONSORPORT());
							System.out.println("转发连接建立Socket=" + transSocket);
							transBr = new DataInputStream(new BufferedInputStream(
									transSocket.getInputStream()));
							transPw = new DataOutputStream(new BufferedOutputStream(
									transSocket.getOutputStream()));
						} catch (Exception e) {
							e.printStackTrace();
						}
						dataRequestClient();
					}
				}).start();
			}
		});
	}

	private void dataRequestClient() {
		Socket socket = null;
		DataInputStream br = null;
		DataOutputStream pw = null;
		try {
			socket = new Socket(FinalDataSource.getDataserverip(),
					FinalDataSource.getDataport());
			System.out.println("Data Socket=" + socket);
			br = new DataInputStream(new BufferedInputStream(
					socket.getInputStream()));
			pw = new DataOutputStream(new BufferedOutputStream(
					socket.getOutputStream()));
			String filename = br.readUTF();
			long length = br.readLong();
			System.out.println("Data服务器发送的文件名：" + filename);
			System.out.println("文件的长度：" + length);
			
			transPw.writeUTF(filename);
			transPw.writeLong(length);

			int bufferSize = 8192;
			byte[] buf = new byte[bufferSize];
			int passedlen = 0;

			while (true) {
				forceMobileConnection();
				int read = 0;
				if (br != null) {
					read = br.read(buf);
				}
				passedlen += read;
				if (read == -1) {
					transPw.flush();
					System.out.println("文件转发完成\n");
					break;
				}
				long speed = passedlen * 100 / length;
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putLong("speed", speed);
				message.obj = bundle;
				handler.sendMessage(message);		
				// 转发文件流
				transPw.write(buf,0,read);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			try {
				System.out.println("close......");
				br.close();
				pw.close();
				socket.close();
			} catch (Exception e2) {
				// TODO: handle exception
				e2.printStackTrace();
			}
		}
		return;
	}
	
	private boolean forceMobileConnection(){
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (null == connectivityManager) {
			return false;
		}
		int resultInt = connectivityManager.startUsingNetworkFeature(
				ConnectivityManager.TYPE_MOBILE, "enableHIPRI");
	
		if (-1 == resultInt) {
			return false;
		}
		if (0 == resultInt) {
			return true;
		}
		return true;
	}

}
