package cn.fudan.sonic.util;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class SwingConsole {

	public static void run(final JFrame frame,final int width,final int height){
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setSize(width, height);
				frame.setVisible(true);
			}
		});
	}
}
