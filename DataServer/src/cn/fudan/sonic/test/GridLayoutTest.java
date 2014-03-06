package cn.fudan.sonic.test;

import java.awt.GridLayout;
import java.awt.HeadlessException;

import javax.swing.JButton;
import javax.swing.JFrame;

import cn.fudan.sonic.util.SwingConsole;

public class GridLayoutTest extends JFrame{

	public GridLayoutTest() throws HeadlessException {
		setLayout(new GridLayout(7,3));
		for(int i = 0;i<15;i++){
			JButton button = new JButton("Button"+i);
			add(button);
		}
	}
	
	public static void main(String[] args){
		SwingConsole.run(new GridLayoutTest(), 800, 600);
	}

}
