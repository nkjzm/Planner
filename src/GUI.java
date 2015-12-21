import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JEditorPane;
import javax.swing.JScrollBar;
import javax.swing.JSeparator;
import java.awt.Panel;
import java.awt.Canvas;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class GUI extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI frame = new GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GUI() {
		setTitle("Planner");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblStart = new JLabel("Start");
		lblStart.setBounds(36, 12, 61, 15);
		contentPane.add(lblStart);
		
		JLabel lblFinish = new JLabel("Finish");
		lblFinish.setBounds(36, 123, 61, 15);
		contentPane.add(lblFinish);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(25, 39, 165, 72);
		contentPane.add(scrollPane);
		
		JTree tree = new JTree();
		scrollPane.setViewportView(tree);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(25, 150, 165, 72);
		contentPane.add(scrollPane_1);
		
		JTree tree_1 = new JTree();
		scrollPane_1.setViewportView(tree_1);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(243, 40, 170, 179);
		contentPane.add(scrollPane_2);
		
		JTree tree_2 = new JTree();
		scrollPane_2.setViewportView(tree_2);
		
		JLabel lblProcess = new JLabel("Process");
		lblProcess.setBounds(253, 12, 61, 15);
		contentPane.add(lblProcess);
		
		JButton btnRun = new JButton("Run");
		btnRun.setBounds(64, 234, 80, 25);
		contentPane.add(btnRun);
		
		JButton btnBack = new JButton("Back");
		btnBack.setBounds(244, 234, 80, 25);
		contentPane.add(btnBack);
		
		JButton btnGo = new JButton("Go");
		btnGo.setBounds(333, 234, 80, 25);
		contentPane.add(btnGo);
		
	}
	public void actionPerformed(ActionEvent e){
	    String cmd = e.getActionCommand();
	    if(cmd.equals("Run")){
	    	Planner planner = new Planner();
	    }
	}
}
