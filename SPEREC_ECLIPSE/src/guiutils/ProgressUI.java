package guiutils;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

public class ProgressUI {
	private JFrame frame = null;
	private JProgressBar pb = null;
	private JTextArea taskOutput = null;

	public  ProgressUI (String title, int pbmax) {

		JFrame frame = new JFrame(title);
		frame.setBounds(200,100,600,450);
		JProgressBar pb = new JProgressBar();
		pb.setMinimum(0);
		pb.setMaximum(pbmax);
		pb.setStringPainted(true);
		JTextArea taskOutput = new JTextArea(20, 50);
		DefaultCaret caret = (DefaultCaret)taskOutput.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		taskOutput.setMargin(new Insets(5,5,5,5));
		taskOutput.setEditable(false);
		frame.setLayout(new FlowLayout());
		frame.getContentPane().add(pb);
		frame.add(new JScrollPane(taskOutput), BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true); 

		this.frame = frame;
		this.pb = pb;
		this.taskOutput = taskOutput;
	}

	/**
	 * Update the progress
	 * @param value
	 * @param msg
	 */
	public void increaseProgress(int value, String msg) {
		if (value>pb.getValue()) {
			pb.setValue(value);
		}
		taskOutput.append(msg);
	}


	/**
	 * Destroy the progress bar
	 */
	public void close() {
		frame.dispose();
	}

}
