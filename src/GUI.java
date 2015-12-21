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
	public GUI() 
	{
		setTitle("Planner");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1200, 800);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblStart = new JLabel("Start");
		lblStart.setBounds(10, 12, 61, 15);
		contentPane.add(lblStart);
		
		float graphical_scale = 0.5f;
		Graphical_sample start_arrange = new Graphical_sample(graphical_scale);
		start_arrange.setBounds(10, 40, (int)(800*graphical_scale), (int)(608*graphical_scale));
		contentPane.add(start_arrange);

		JLabel lblFinish = new JLabel("Finish");
		lblFinish.setBounds(10, 420, 61, 15);
		contentPane.add(lblFinish);

		Graphical_sample goal_arrange = new Graphical_sample(graphical_scale);
		goal_arrange.setBounds(10, 440, (int)(800*graphical_scale), (int)(608*graphical_scale));
		contentPane.add(goal_arrange);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(820, 40, 170, 179);
		contentPane.add(scrollPane_2);
		
		JTree tree_2 = new JTree();
		scrollPane_2.setViewportView(tree_2);
		
		JLabel lblProcess = new JLabel("Process");
		lblProcess.setBounds(820, 12, 61, 15);
		contentPane.add(lblProcess);
		
		JButton btnRun = new JButton("Run");
		btnRun.setBounds(64, 380, 80, 25);
		contentPane.add(btnRun);
		
		JButton btnBack = new JButton("Back");
		btnBack.setBounds(900, 234, 80, 25);
		contentPane.add(btnBack);
		
		JButton btnGo = new JButton("Go");
		btnGo.setBounds(960, 234, 80, 25);
		contentPane.add(btnGo);
		
	}
	public void actionPerformed(ActionEvent e){
	    String cmd = e.getActionCommand();
	    if(cmd.equals("Run")){
	    	Planner planner = new Planner();
	    }
	}
}
