package cn.fudan.sonic.server;

import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import cn.fudan.sonic.util.SwingConsole;

@SuppressWarnings("serial")
public class ServerFrame extends JFrame implements ActionListener {

	private JButton codeButton, launchButton, stopButton;
	private JTextField fileUrlField;
	private JButton chooseFileButton;
	private JLabel label;
	private JTextArea textArea;

	JFileChooser fileChooser = new JFileChooser();
	String filePath = new String();
	boolean ifAlreadCode = true;
	private MutilTCPServer tcpServer;
	private MutilUDPServer udpServer;
	
	private ThreadPoolExecutor poolExecutor = SingletonThreadPool
			.getThreadPool();

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
		stopButton.setBounds(new Rectangle(380, 200, 100, 30));
		this.add(stopButton);
		label = new JLabel("����״����");
		label.setBounds(new Rectangle(150, 250, 100, 30));
		this.add(label);
		textArea = new JTextArea("��ʱû������");
		textArea.setBounds(new Rectangle(250, 250, 250, 200));
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
				
				//TCP����
				Thread tcpThread = new Thread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						// ����TCP������
						tcpServer = new MutilTCPServer(filePath);
						tcpServer.service();
					}
				});
							
				//UDP���ķ���
				Thread udpSendThread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						DataSendManager sendManager = new DataSendManager(
								filePath);
						udpServer = new MutilUDPServer(sendManager);
						while(true){
							if(SignalController.IfReadyToSend && !SignalController.IfFileSendSuccess){
								udpServer.receive();
								udpServer.response();
							}
						}
					}
				});
				
				poolExecutor.execute(tcpThread);
				poolExecutor.execute(udpSendThread);
			} else {
				JOptionPane.showMessageDialog(null, "����������ǰ������ѡ���ļ����б��룡", "��ʾ",
						2);
			}
		}
		if (e.getSource().equals(stopButton)) {
			tcpServer.shutdown();
		}
	}

	public static void main(String[] args) {
		SwingConsole.run(new ServerFrame(), 700, 600);
	}

}
