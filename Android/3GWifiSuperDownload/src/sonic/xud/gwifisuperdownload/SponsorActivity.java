package sonic.xud.gwifisuperdownload;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadPoolExecutor;

import sonic.xud.assistclass.SingletonThreadPool;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SponsorActivity extends Activity {

	private Button beginButton;

	private static File path = Environment.getExternalStorageDirectory();
	private static String SponsorPath = path.getPath() + File.separator
			+ "superdown" + File.separator + "sponsor/";
	
	private MultiThreadServer multiThreadServer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sponsor);

		init();
	}

	private void init() {
		File file = new File(SponsorPath);
		if (!file.exists()) {
			file.mkdirs();
		}
		beginButton = (Button) findViewById(R.id.beginBtn);
		beginButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				new Thread(new Runnable() {
					
					public void run() {
						// TODO Auto-generated method stub
						try {
							multiThreadServer = new MultiThreadServer();
							multiThreadServer.service();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}).start();	
			}
		});
	}
	
	//��һ�ͻ���ʱ
//	private void server() {
//		ServerSocket s = null;
//		Socket socket = null;
//		DataInputStream br = null;
//		DataOutputStream pw = null;
//		DataOutputStream fileOutputStream = null;
//		try {
//			s = new ServerSocket(PORT);
//			System.out.println("ServerSocket Start:" + s);
//			// �ȴ�����,�˷�����һֱ����,ֱ����������������
//			socket = s.accept();
//			System.out.println("Connection accept socket:" + socket);
//			// ���ڽ��տͻ��˷���������
//			br = new DataInputStream(new BufferedInputStream(
//					socket.getInputStream()));
//			// ���ڷ��ͷ�����Ϣ
//			pw = new DataOutputStream(new BufferedOutputStream(
//					new BufferedOutputStream(socket.getOutputStream())));
//
//			int bufferSize = 8192;
//			byte[] buf = new byte[bufferSize];
//			int passedlen = 0;
//			long len = 0;
//
//			String filePath = SponsorPath + br.readUTF();
//			fileOutputStream = new DataOutputStream(new BufferedOutputStream(
//					new FileOutputStream(filePath)));
//			len = br.readLong();
//			System.out.println("�ļ��ĳ���Ϊ:" + len + "\n");
//			System.out.println("��ʼ�����ļ�!" + "\n");
//
//			while (true) {
//				int read = 0;
//				if (br != null) {
//					read = br.read(buf);
//				}
//				passedlen += read;
//				if (read == -1) {
//					System.out.println("������ɣ��ļ���Ϊ" + filePath + "\n");
//					break;
//				}
//				System.out.println("�ļ�������" + (passedlen * 100 / len) + "%\n");
//				fileOutputStream.write(buf, 0, read);
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} finally {
//			System.out.println("Close.....");
//			try {
//				fileOutputStream.close();
//				br.close();
//				pw.close();
//				socket.close();
//				s.close();
//			} catch (Exception e2) {
//
//			}
//		}
//	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	public class MultiThreadServer {
		
		private int PORT = 0x1043;
		private ServerSocket serverSocket;
		private ThreadPoolExecutor threadPool;

		public MultiThreadServer() throws Exception {
			serverSocket = new ServerSocket(PORT);
			threadPool = SingletonThreadPool.getThreadPool();
			System.out.println("����������");
		}

		public void service() {
			while (true) {
				Socket socket = null;
				try {
					socket = serverSocket.accept();
					ClientThread thread = new ClientThread(socket);
					threadPool.execute(thread);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	class ClientThread extends Thread {

		private Socket socket;

		public ClientThread(Socket socket) {
			super();
			this.socket = socket;
		}

		@Override
		public void run() {
			DataInputStream br = null;
			DataOutputStream pw = null;
			DataOutputStream fileOutputStream = null;
			try {
				System.out.println("New connection accepted "
						+ socket.getInetAddress() + ":" + socket.getPort());
				// ���ڽ��տͻ��˷���������
				br = new DataInputStream(new BufferedInputStream(
						socket.getInputStream()));
				// ���ڷ��ͷ�����Ϣ
				pw = new DataOutputStream(new BufferedOutputStream(
						new BufferedOutputStream(socket.getOutputStream())));

				int bufferSize = 8192;
				byte[] buf = new byte[bufferSize];
				int passedlen = 0;
				long len = 0;

				String filePath = SponsorPath + br.readUTF();
				fileOutputStream = new DataOutputStream(
						new BufferedOutputStream(new FileOutputStream(filePath)));
				len = br.readLong();
				System.out.println("�ļ��ĳ���Ϊ:" + len + "\n");
				System.out.println("��ʼ�����ļ�!" + "\n");

				while (true) {
					int read = 0;
					if (br != null) {
						read = br.read(buf);
					}
					passedlen += read;
					if (read == -1) {
						System.out.println("������ɣ��ļ���Ϊ" + filePath + "\n");
						break;
					}
					System.out.println("�ļ�������" + (passedlen * 100 / len)
							+ "%\n");
					fileOutputStream.write(buf, 0, read);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				System.out.println("Close.....");
				try {
					fileOutputStream.close();
					br.close();
					pw.close();
					socket.close();
				} catch (Exception e2) {

				}
			}
		}

	}
}
