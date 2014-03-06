package cn.fudan.sonic.test;

import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import cn.fudan.sonic.util.SwingConsole;

public class HelloSwing3 extends JFrame {
	private JButton 
		b1 = new JButton("Add Button"),
		b2 = new JButton("Clear Button");
	private TextArea textArea = new TextArea(20,40);
	private HashMap<String, String> map = new HashMap<String, String>();
	public HelloSwing3() throws HeadlessException {
		map.put("1", "上海");
		map.put("2", "北京");
		map.put("3", "广州");
		
		b1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				for(Map.Entry meEntry :map.entrySet()){
					textArea.append(meEntry.getKey()+":"+meEntry.getValue()+"\n");
				}
			}
		});
		
		b2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				textArea.setText("");
			}
		});
		
		setLayout(new FlowLayout());
		add(new JScrollPane(textArea));
		add(b1);
		add(b2);
	}
	
	public static void main(String[] args){
		SwingConsole.run(new HelloSwing3(), 800, 600);
	}

}
