package sonic.xud.gwifisuperdownload;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AssistActivity extends Activity {

	private Button beginButton;
	private static int PORT = 0x1043;
	private static File path = Environment.getExternalStorageDirectory();
	private static String AssistPath = path.getPath() + File.separator
			+ "superdown" + File.separator + "assist/";
	private static String Filename = "test.zip";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.assist);
		init();
	}

	private void init() {
		File file = new File(AssistPath);
		if(!file.exists()){
			file.mkdirs();
		}
		beginButton = (Button) findViewById(R.id.beginBtn);
		beginButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				new Thread(new Runnable() {

					public void run() {
						// TODO Auto-generated method stub
						client();
					}
				}).start();
			}
		});
	}

	private void client() {
		Socket socket = null;
		DataInputStream br = null;
		DataOutputStream pw = null;
		DataInputStream fileInputStream = null;
		try {
			// 客户端socket指定服务器的地址和端口号
			socket = new Socket("192.168.43.1", PORT);
			System.out.println("Socket=" + socket);
			// 用以接收服务端返回信息
			br = new DataInputStream(new BufferedInputStream(socket.getInputStream())); 
			
			String filePath = AssistPath + Filename;
			File file = new File(filePath);
			fileInputStream = new DataInputStream(
					new BufferedInputStream(new FileInputStream(filePath)));
			// 用以发送信息
			pw = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream())) ;
			
			//将文件名及长度发送给接收端
			pw.writeUTF(file.getName());
			pw.flush();
			pw.writeLong((long)file.length());
			pw.flush();
			
			int bufferSize = 8192;
            byte[] buf = new byte[bufferSize];
            while (true) {
                int read = 0;
                if (fileInputStream != null) {
                    read = fileInputStream.read(buf);
                }
                if (read == -1) {
                	pw.flush();
                    break;
                }
                pw.write(buf, 0, read);
            }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				System.out.println("close......");
				fileInputStream.close();
				br.close();
				pw.close();
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
