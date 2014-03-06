package cn.fudan.sonic.server;

import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import cn.fudan.sonic.util.SwingConsole;

public class ServerFrame extends JFrame implements ActionListener {

	private JButton codeButton, launchButton,stopButton;
	private JTextField fileUrlField;
	private JButton chooseFileButton;
	private JLabel label;
	private JTextArea textArea;

	JFileChooser fileChooser = new JFileChooser();
	String filePath = new String();
	boolean ifAlreadCode = true;
	private MutilThreadServer threadServer;
	
	private HashMap<String, String> map = new HashMap<String, String>();
	private static int count = 0;

	public ServerFrame() throws HeadlessException {
		super("��ģʽЭͬ����ϵͳ");
		fileChooser.setCurrentDirectory(new File("D://"));
		this.setLayout(null);// ���ò��ֹ�����Ϊ��
		fileUrlField = new JTextField();
		fileUrlField.setBounds(new Rectangle(150, 100, 230, 30));
		this.add(fileUrlField);
		chooseFileButton = new JButton("ѡ���ļ�");
		chooseFileButton.setBounds(new Rectangle(400, 100, 100, 30));
		this.add(chooseFileButton);
		codeButton = new JButton("�ļ�����");
		codeButton.setBounds(new Rectangle(220, 150, 140, 30));
		this.add(codeButton);
		launchButton = new JButton("����������");
		launchButton.setBounds(new Rectangle(220, 200, 140, 30));
		this.add(launchButton);
		stopButton = new JButton("ֹͣ");
		stopButton.setBounds(new Rectangle(380,200,100,30));
		this.add(stopButton);
		label = new JLabel("����״����");
		label.setBounds(new Rectangle(150,250,100,30));
		this.add(label);
		textArea = new JTextArea("��ʱû������");
		textArea.setBounds(new Rectangle(250,250,250,200));
//		this.add(new JScrollPane(textArea));
		this.add(textArea);
		chooseFileButton.addActionListener(this);
		codeButton.addActionListener(this);
		launchButton.addActionListener(this);
		stopButton.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource().equals(chooseFileButton)) {
			System.out.println("-------");
			fileChooser.setFileSelectionMode(0);// �趨ֻ��ѡ���ļ�
			int state = fileChooser.showOpenDialog(null);// �˾��Ǵ��ļ�ѡ��������Ĵ������
			if (state == 1) {
				return;// �����򷵻�
			} else {
				File f = fileChooser.getSelectedFile();
				filePath = f.getAbsolutePath();
				fileUrlField.setText(f.getAbsolutePath());
			}
		}
		if (e.getSource().equals(codeButton)) {

		}
		if (e.getSource().equals(launchButton)) {
			if (!filePath.equals("") && ifAlreadCode) {
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						//����������
						threadServer = new MutilThreadServer();
						threadServer.service();
					}
				}).start();
			} else {
				JOptionPane.showMessageDialog(null, "����������ǰ������ѡ���ļ����б��룡", "��ʾ",
						2);
			}
		}
		if(e.getSource().equals(stopButton)){
			threadServer.shutdown();
		}
	}

	public class MutilThreadServer {
		private final int CONTROLPORT = 8000;
		private final int DATAPORT = 8001;
		private ServerSocket dataServer;
		private ThreadPoolExecutor poolExecutor;

		public MutilThreadServer() {
			try {
				dataServer = new ServerSocket(DATAPORT);
				poolExecutor = SingletonThreadPool.getThreadPool();
				System.out.println("���ݷ���������");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void service() {
			while (true) {
				Socket socket = null;
				try {
					socket = dataServer.accept();
					ClientThread thread = new ClientThread(socket);
					poolExecutor.execute(thread);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		public void shutdown(){
			System.out.println("������ֹͣ������");
			poolExecutor.shutdownNow();
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
			DataInputStream fileInputStream  = null;
			try {
				System.out.println("New connection accepted "
						+ socket.getInetAddress() + ":" + socket.getPort());
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						++count;
						map.put(String.valueOf(count), socket.getInetAddress()+":"+socket.getPort());
						textArea.setText("");
						for(Map.Entry meEntry :map.entrySet()){
							textArea.append(meEntry.getKey()+"��"+meEntry.getValue()+"\n");
						}
					}
				});
				// ���ڽ��տͻ��˷���������
				br = new DataInputStream(new BufferedInputStream(
						socket.getInputStream()));
				// ���ڷ�����Ϣ
				pw = new DataOutputStream(new BufferedOutputStream(
						new BufferedOutputStream(socket.getOutputStream())));

				int bufferSize = 8192;
				byte[] buf = new byte[bufferSize];
				File file = new File(filePath);
				long len = file.length();

				//���Զ�ȡ�ļ���
				fileInputStream = new DataInputStream(
						new BufferedInputStream(new FileInputStream(filePath)));
				System.out.println("�ļ��ĳ���Ϊ:" + len + "\n");
				System.out.println("��ʼ�����ļ�!" + "\n");

				pw.writeUTF(file.getName());
				pw.flush();
				pw.writeLong(len);
				pw.flush();

				while (true) {
					int read = 0;
					if (fileInputStream != null) {
						read = fileInputStream.read(buf);
					}
					if (read == -1) {
						break;
					}
					pw.write(buf, 0, read);
				}
				pw.flush();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				System.out.println("Close.....");
				try {
					fileInputStream.close();
					br.close();
					pw.close();
					socket.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}

	}

	public static void main(String[] args) {
		SwingConsole.run(new ServerFrame(), 700, 600);
	}

}
