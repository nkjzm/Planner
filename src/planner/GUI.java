package planner;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

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
		setBounds(100, 100, 1400, 800);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setBackground(Color.gray);
		contentPane.setLayout(null);

		ImageIcon icon2 = new ImageIcon("./img/hukidasi.png");
		JLabel label2 = new JLabel(icon2);
		label2.setBounds(0, -100, 1350, 660);
		contentPane.add(label2);

		JLabel lblStart = new JLabel("Start");
		lblStart.setBounds(50, 400, 100, 25);
		contentPane.add(lblStart);

		float graphical_scale = 0.4f;

		startArrange = new Graphical_sample(graphical_scale);
		startArrange.setBounds(50, 420, (int)(1275*graphical_scale), (int)(700*graphical_scale));
		contentPane.add(startArrange);

		JLabel lblFinish = new JLabel("Finish");
		lblFinish.setBounds(800, 400,100, 25);
		contentPane.add(lblFinish);

		goalArrange = new Graphical_sample(graphical_scale);
		goalArrange.setBounds(800, 420, (int)(1275*graphical_scale), (int)(700*graphical_scale));
		contentPane.add(goalArrange);

		JLabel lblProgress = new JLabel("Progress");
		lblProgress.setBounds(425,30,100,25);
		contentPane.add(lblProgress);

		progressArrange = new Graphical_sample(graphical_scale);
		progressArrange.setBounds(400, 130, (int)(1275*graphical_scale), (int)(700*graphical_scale));
		label2.add(progressArrange);

		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(1100, 50, 170, 179);
		contentPane.add(scrollPane_2);

		area = new JTextArea();
		scrollPane_2.setViewportView(area);

		JLabel lblProcess = new JLabel("Process");
		lblProcess.setBounds(1100, 30, 61, 15);
		contentPane.add(lblProcess);

		final JLabel lblCount = new JLabel("実行前");
		lblCount.setBounds(1200, 30, 61, 15);
		contentPane.add(lblCount);

		final JLabel label = new JLabel();

	    JPanel labelPanel = new JPanel();
	    labelPanel.add(label);

		JButton btnfile = new JButton("file open");
		btnfile.setBounds(100,100,160,50);
		    btnfile.addActionListener(
		    		new ActionListener(){
		    			public void actionPerformed(ActionEvent e){
		    				 JFileChooser filechooser = new JFileChooser();

		    				    int selected = filechooser.showOpenDialog(label);
		    				    if (selected == JFileChooser.APPROVE_OPTION){
		    				      final File file = filechooser.getSelectedFile();

		    				      try{

		    				          FileReader filereader = new FileReader(file);

		    				          int ch;
		    				          area.setText("");
		    				          while((ch = filereader.read()) != -1){
		    				            area.append(String.valueOf((char)ch));
		    				          }

		    				          filereader.close();
		    				        }catch(FileNotFoundException error){
		    				          System.out.println(error);
		    				        }catch(IOException error){
		    				          System.out.println(error);
		    				        }

		    				    }
		    			}
		    		});
		contentPane.add(btnfile);
		contentPane.add(labelPanel);

		JButton btnsave = new JButton("file save");
		btnsave.setBounds(100,200,160,50);
			btnsave.addActionListener(
					new ActionListener(){
						public void actionPerformed(ActionEvent e){
							 JFileChooser filechooser = new JFileChooser();
		    				    int selected = filechooser.showSaveDialog(label);
		    				    if (selected == JFileChooser.APPROVE_OPTION){
		    				     final File file = filechooser.getSelectedFile();
		    				     try{

		    				         if (checkBeforeWritefile(file)){
		    				           FileWriter filewriter = new FileWriter(file);
		    				           for(int i=0;progressStates.size()>i;i++){
		    				           ArrayList<String> states = progressStates.get(i);

		    				   		for(String str : states){
		    				   			filewriter.write(str + "\r\n");}
		    				           }
		    				           filewriter.close();
		    				         }else{
		    				           System.out.println("ファイルに書き込めません");
		    				         }
		    				       }catch(IOException error){
		    				         System.out.println(error);
		    				       }
		    				    }}
					});
			contentPane.add(btnsave);
		ImageIcon icon1 = new ImageIcon("./img/ya.png");
		JLabel label1 = new JLabel(icon1);
		label1.setBounds(570, 450, 250, 250);
			 label1.addMouseListener(new MouseListener()
			 {
			 public void mouseClicked(MouseEvent arg0) {
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
			 public void mouseEntered(MouseEvent arg0) {
			 }
			 public void mouseExited(MouseEvent arg0) {
			 }
			 public void mousePressed(MouseEvent arg0) {
			 }
			 public void mouseReleased(MouseEvent arg0) {
			 }
			 });

		contentPane.add(label1);


		JButton btnBack = new JButton("Back");
		btnBack.setBounds(1100, 234, 80, 25);
		btnBack.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent e){
						index = Math.max(--index,0);
						DisplayState();
					}
				});
		contentPane.add(btnBack);
		JButton btnGo = new JButton("Go");
		btnGo.setBounds(1200, 234, 80, 25);
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
	private class myListener extends MouseAdapter{
	    public void mouseClicked(MouseEvent e){
	    	area.setText("");
			index = 0;
			Planner planner = new Planner();
			ArrayList<String> initialState = startArrange.getCurrentState();
			ArrayList<String> goalList = goalArrange.getCurrentState();
			planner.start(goalList,initialState);
			progressStates = planner.ProgressStates;
			ProgressResult = planner.ProgressResult;


			DisplayState();
	    }
	  }
	 private static boolean checkBeforeWritefile(File file){
		    if (file.exists()){
		      if (file.isFile() && file.canWrite()){
		        return true;
		      }
		    }

		    return false;
		  }


}
