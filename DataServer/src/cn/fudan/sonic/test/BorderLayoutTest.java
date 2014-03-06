package cn.fudan.sonic.test;

import java.awt.BorderLayout;
import java.awt.HeadlessException;

import javax.swing.JButton;
import javax.swing.JFrame;

import cn.fudan.sonic.util.SwingConsole;

public class BorderLayoutTest extends JFrame{

	public BorderLayoutTest() throws HeadlessException {
		add(BorderLayout.NORTH,new JButton("North"));
		add(BorderLayout.SOUTH,new JButton("South"));
		add(BorderLayout.EAST,new JButton("East"));
		add(BorderLayout.WEST,new JButton("West"));
		add(BorderLayout.CENTER,new JButton("Center"));
	}
	
	public static void main(String[] args){
		SwingConsole.run(new BorderLayoutTest(), 800, 600);
	}

}
