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
import java.awt.Color;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JButton;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.awt.event.ActionEvent;

public class GUI extends JFrame {
	private JPanel contentPane;
	Graphical_sample startArrange;
	Graphical_sample progressArrange;
	Graphical_sample goalArrange;
	int index = 0;
	ArrayList<ArrayList<String>> progressStates;
	ArrayList<String> ProgressResult;
	final JTextArea area;

	public static void main(String[] args) 
	{
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

	public GUI()
	{
		setTitle("Planner");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1200, 800);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setBackground(Color.gray);
		contentPane.setLayout(null);

		JLabel lblStart = new JLabel("Start");
		lblStart.setBounds(10, 12, 61, 15);
		contentPane.add(lblStart);

		float graphical_scale = 0.5f;

		startArrange = new Graphical_sample(graphical_scale);
		startArrange.setBounds(10, 40, (int)(800*graphical_scale), (int)(608*graphical_scale));
		contentPane.add(startArrange);

		JLabel lblFinish = new JLabel("Finish");
		lblFinish.setBounds(10, 420, 61, 15);
		contentPane.add(lblFinish);

		goalArrange = new Graphical_sample(graphical_scale);
		goalArrange.setBounds(10, 440, (int)(800*graphical_scale), (int)(608*graphical_scale));
		contentPane.add(goalArrange);

		progressArrange = new Graphical_sample(graphical_scale);
		progressArrange.setBounds(600, 400, (int)(800*graphical_scale), (int)(608*graphical_scale));
		contentPane.add(progressArrange);

		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(600, 40, 170, 179);
		contentPane.add(scrollPane_2);

		area = new JTextArea();
		scrollPane_2.setViewportView(area);

		JLabel lblProcess = new JLabel("Process");
		lblProcess.setBounds(600, 12, 61, 15);
		contentPane.add(lblProcess);

		final JLabel lblCount = new JLabel("実行前");
		lblCount.setBounds(700, 12, 61, 15);
		contentPane.add(lblCount);

		JButton btnRun = new JButton("Run");
		btnRun.setBounds(64, 380, 80, 25);
		btnRun.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent e){
						area.setText("");
						index = 0;
						Planner planner = new Planner();
						ArrayList<String> initialState = startArrange.getCurrentState();
						ArrayList<String> goalList = goalArrange.getCurrentState();
						planner.start(goalList,initialState);
						progressStates = planner.ProgressStates;
						ProgressResult = planner.ProgressResult;
						lblCount.setText("計" + (progressStates.size()-1) + "回\n");
						DisplayState();
					}
				});
		contentPane.add(btnRun);
		JButton btnBack = new JButton("Back");
		btnBack.setBounds(600, 234, 80, 25);
		btnBack.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent e){
						index = Math.max(--index,0);
						DisplayState();
					}
				});
		contentPane.add(btnBack);
		JButton btnGo = new JButton("Go");
		btnGo.setBounds(700, 234, 80, 25);
		btnGo.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent e){
						index = Math.min(++index,progressStates.size()-1);
						DisplayState();
					}
				});
		contentPane.add(btnGo);
	}
	private void DisplayState() {
		if(index == 0){
			area.setText("初期状態\n");
		}else if(index == progressStates.size() - 1){
			area.setText("終了状態\n");
		}else{
			area.setText("移動"+index+"回目\n");			
		}
		if(ProgressResult.size()>0){
			area.append(ProgressResult.get(index));
		}
		area.append(" --- \n");	
		ArrayList<String> states = progressStates.get(index);
		for(String str : states){
			area.append(str + "\n");
		}
		progressArrange.SetBlockArrangement(new ArrayList<String>(states));
	}
}
