import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class GUI extends java.applet.Applet implements ActionListener{
	

	int screen_width = 320, screen_height = 240;
	int speed = 5;
	
	Dimension d;//表示領域
	Image offs;//オフスクリーン
	Graphics grf;
	int size = 20; //ブロックの大きさ
	
	ArrayList<String> initialCondition;
	ArrayList<String> goalCondition;
	ArrayList<String> plan;
	
	Button rb;
	Button bb;
	Button gb;
	
	public void init(){
		this.setSize(screen_width, screen_height);

		initialCondition = new ArrayList<String>();
		goalCondition = new ArrayList<String>();
		/*オフスクリーンの設定*/
		d = getSize(); //表示領域の取得
		offs = createImage(d.width, d.height);
		grf = offs.getGraphics();
		grf.setColor(Color.white);
		//各フィールドの初期化
		rb = new Button("Run");
		bb = new Button("Back");
		gb = new Button("Go");
		
		rb.addActionListener(this);
		bb.addActionListener(this);
		gb.addActionListener(this);
		setLayout(null);
		
		rb.setBounds(450, 300, 180, 100);
		bb.setBounds(450, 410, 90, 50);
		gb.setBounds(540, 410, 90, 50);
		
		add(rb);
		add(bb);
		add(gb);
		
	}
	public void actionPerformed(ActionEvent e){
		String cmd = e.getActionCommand();
		if(cmd.equals("Run")){}
		if(cmd.equals("Back")){}
		if(cmd.equals("Go")){}
		
	}
	
}