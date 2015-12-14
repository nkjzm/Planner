import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

public class Graphical_sample extends JFrame{
	public static void main(String args[]){
		Graphical_sample frame = new Graphical_sample("タイトル");
		frame.setVisible(true);
	}

	HashMap<String,JLabel> blocks = new HashMap<String,JLabel>();
	HashMap<PosName,Position> positions = new HashMap<PosName,Position>();

	public enum State {
		FILL, EMPTY, DISABLE 
	}
	public enum PosName{
		left_bottom, left_middle, left_top,
		center_bottom, center_middle, right_bottom, arm
	}
	public class Position
	{
		public int x;
		public int y;
		public State state;
		public JLabel label;
		public Position(int X, int Y)
		{
			x = X;
			y = Y;
			state = State.DISABLE;
			label = GenerateLabel("empty.png",x, y, 128, 128);
		}
	}

	Graphical_sample(String title){
		setTitle(title);
		setBounds(0, 0, 800, 608);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel p = new JPanel();
		p.setLayout(null);

		positions.put(PosName.left_bottom,new Position(64, 352));
		positions.put(PosName.left_middle,new Position(64, 224));
		positions.put(PosName.left_top,new Position(64, 96));
		positions.put(PosName.center_bottom,new Position(256, 352));
		positions.put(PosName.center_middle,new Position(256, 224));
		positions.put(PosName.right_bottom,new Position(448, 352));
		positions.put(PosName.arm,new Position(448, 64));

		//ブロック
		SetBlock(GenerateLabel("block_a.png",650, 64, 128, 128),p);
		SetBlock(GenerateLabel("block_b.png",650, 256, 128, 128),p);
		SetBlock(GenerateLabel("block_c.png",650, 448, 128, 128),p);

		//固定パーツ
		JLabel arm = GenerateLabel("arm.png",438, 0, 148, 128);
		p.add(arm);
		JLabel floor = GenerateLabel("floor.png",0, 480, 640, 128);
		p.add(floor);

		//点線ラベルをパネルに追加
		for (PosName n : PosName.values()) {
			p.add(positions.get(n).label);
		}

		//非表示
		//empty_floor3.setVisible(false);

		Container contentPane = getContentPane();
		contentPane.add(p, BorderLayout.CENTER);
	}

	private JLabel GenerateLabel(String imgName, int x, int y, int witdh, int height)
	{
		ImageIcon icon = new ImageIcon("./img/"+imgName);
		JLabel label = new JLabel(icon);
		label.setBounds(x, y, witdh, height);
		return label;
	}

	private void SetBlock(JLabel label,JPanel p)
	{
		blocks.put(label.getIcon().toString(), label);

		// リスナーを登録
		MyMouseListener listener = new MyMouseListener(label.getIcon().toString());
		label.addMouseListener(listener);
		label.addMouseMotionListener(listener);

		p.add(label);
	}

	private static class Validator {
		public static boolean includes(int lower, int upper, int value) {
			return lower <= value && value <= upper;
		}
	}

	private class MyMouseListener extends MouseAdapter
	{
		private int dx;
		private int dy;
		private int init_x;
		private int init_y;
		private String key;

		public MyMouseListener(String key)
		{
			this.key = key;
		}

		public void mouseDragged(MouseEvent e) 
		{
			// マウスの座標からラベルの左上の座標を取得する
			int x = e.getXOnScreen() - dx;
			int y = e.getYOnScreen() - dy;
			blocks.get(key).setLocation(x, y);
		}

		public void mousePressed(MouseEvent e) 
		{
			// 押さえたところからラベルの左上の差を取っておく
			init_x = blocks.get(key).getX();
			init_y = blocks.get(key).getY();
			dx = e.getXOnScreen() - init_x;
			dy = e.getYOnScreen() - init_y;
		}

		public void mouseReleased(MouseEvent e) 
		{
			int x = e.getXOnScreen() - dx;
			int y = e.getYOnScreen() - dy;

			if(IsFit(x, y, 448, 64)){return;}

			if(IsFit(x, y, 64, 352)){return;}
			if(IsFit(x, y, 256, 352)){return;}
			if(IsFit(x, y, 448, 352)){return;}

			if(IsFit(x, y, 64, 224)){return;}
			if(IsFit(x, y, 64, 96)){return;}
			if(IsFit(x, y, 256, 224)){return;}

			//可動領域でなければ初期位置に戻す
			blocks.get(key).setLocation(init_x, init_y);
		}

		private boolean IsFit(int x,int y,int fit_x,int fit_y)
		{
			//80 = 64(半径) + 12(遊び)
			if(Validator.includes(fit_x-80, fit_x+80, x)
					&& Validator.includes(fit_y-80, fit_y+80, y)){
				blocks.get(key).setLocation(fit_x, fit_y);
				return true;
			}
			return false;
		}

	}
}