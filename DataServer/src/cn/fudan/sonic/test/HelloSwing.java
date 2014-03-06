package cn.fudan.sonic.test;

import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class HelloSwing extends JFrame{
	
	private JLabel label;
	private JButton button;
	int count = 0;
	public HelloSwing() throws HeadlessException {
		super("Hello Swing");
		label = new JLabel("First Label");
		button = new JButton("First Button");
		setLayout(new FlowLayout());
		add(label);
		add(button);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800,600);
		setVisible(true);
		//触发事件
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				++count;
				label.setText("点击次数："+count);
			}
		});
	}
	
	static HelloSwing swing;

	public static void main(String[] args){
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				swing = new HelloSwing();
			}
		});
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				swing.label.setText("Hey!This is different!");
			}
		});

	}
}
