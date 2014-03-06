package cn.fudan.sonic.test;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.*;

@SuppressWarnings("serial")
public class HelloSwing2 extends JFrame{
	public static void main(String[] args) {
		HelloSwing2 log = new HelloSwing2();
	}

	private JButton btLog;
	private JTextField tfUser;
	private JPasswordField tfPwd;
	private JCheckBox pwdKeep;
	private JComboBox<String> adminType;

	public HelloSwing2() {
		super("�̶��ʲ�����ϵͳ");
		super.setSize(380, 292);
		super.setVisible(true);
		super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		centered(this);
		btLog = new JButton("��     ¼");
		btLog.setBounds(new Rectangle(93, 220, 180, 30));// �����ֱ�������x��y������
		this.setLayout(null);// ���ò��ֹ�����Ϊ��
		this.add(btLog);
		tfUser = new JTextField();
		tfUser.setBounds(new Rectangle(73, 115, 220, 25));
		this.add(tfUser);
		tfPwd = new JPasswordField();
		tfPwd.setBounds(new Rectangle(73, 150, 220, 25));
		this.add(tfPwd);
		pwdKeep = new JCheckBox("��ס����");
		pwdKeep.setBounds(new Rectangle(68, 185, 110, 25));
		this.add(pwdKeep);
		adminType = new JComboBox<String>(new String[] { "��ְͨԱ", "����Ա", "�߼�����Ա" });
		adminType.setBounds(new Rectangle(183, 185, 100, 25));
		this.add(adminType);

	}

	// ���־��з���
	public void centered(Container container) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		int w = container.getWidth();
		int h = container.getHeight();
		container.setBounds((screenSize.width - w) / 2,
				(screenSize.height - h) / 2, w, h);
	}
}
