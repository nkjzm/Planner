import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Graphical_sample extends JPanel {
	private static float scale;

	public static void main(String args[]) {
		Graphical_sample aaa = new Graphical_sample(0.5f);
		JFrame jframe = new JFrame("DrawRect");
		Container c = jframe.getContentPane();
		c.add(aaa, BorderLayout.CENTER);
		jframe.setSize((int) (800 * scale), (int) (608 * scale));
		jframe.setVisible(true);
		ArrayList<String> tmp = new ArrayList<String>();
		tmp.add("ontable A");
		tmp.add("ontable B");
		tmp.add("C on B");
		tmp.add("clear C");
		aaa.SetBlockArrangement(tmp);
	}

	HashMap<String, JLabel> blocks = new HashMap<String, JLabel>();
	HashMap<PosName, Position> positions = new HashMap<PosName, Position>();

	public enum State {
		FILL, EMPTY, DISABLE
	}

	public enum PosName {
		left_bottom, left_middle, left_top, center_bottom, center_middle, right_bottom, arm
	}

	public class Position {
		public int x;
		public int y;
		public State state;
		public JLabel label;
		// stateがFillの時のみ利用。それ以外の場合は不定とする。
		public String blockId;

		public Position(int X, int Y, JPanel p) {
			x = X;
			y = Y;
			state = State.EMPTY;
			label = GenerateLabel("empty.png", x, y, 128, 128);
			p.add(label);
			// new Timer(30,(ActionListener) label).start();
			// TODO: イベントリスナー定義してタイマーオブジェクトをいじる
		}

		public void SetState(State s) {
			state = s;
			if (state == State.DISABLE) {
				label.setVisible(false);
			} else {
				label.setVisible(true);
			}
		}

		public State GetState() {
			return state;
		}
	}

	Graphical_sample(float scale) {
		this.scale = scale;
		this.setLayout(null);
		setBounds(0, 0, 800, 608);
		positions.put(PosName.arm, new Position(448, 64, this));
		positions.put(PosName.left_bottom, new Position(64, 352, this));
		positions.put(PosName.left_middle, new Position(64, 224, this));
		positions.put(PosName.left_top, new Position(64, 96, this));
		positions.put(PosName.center_bottom, new Position(256, 352, this));
		positions.put(PosName.center_middle, new Position(256, 224, this));
		positions.put(PosName.right_bottom, new Position(448, 352, this));
		// ブロック
		SetBlock(GenerateLabel("block_a.png", 650, 64, 128, 128), "A", this);
		SetBlock(GenerateLabel("block_b.png", 650, 256, 128, 128), "B", this);
		SetBlock(GenerateLabel("block_c.png", 650, 448, 128, 128), "C", this);
		// 固定パーツ
		JLabel arm = GenerateLabel("arm.png", 438, 0, 148, 128);
		this.add(arm);
		JLabel floor = GenerateLabel("floor.png", 0, 480, 640, 128);
		this.add(floor);
		// 点線ラベルをパネルに追加
		for (PosName n : PosName.values()) {
			this.add(positions.get(n).label);
		}

		UpdateDisplay();
	}

	private JLabel GenerateLabel(String imgName, int x, int y, int witdh, int height) {
		ImageIcon icon = new ImageIcon("./img/" + imgName);
		MediaTracker tracker = new MediaTracker(this);
		// ポイント２．getScaledInstanceで大きさを変更します。
		Image smallImg = icon.getImage().getScaledInstance((int) (icon.getIconWidth() * scale), -1, Image.SCALE_SMOOTH);
		// ポイント３．MediaTrackerで処理の終了を待ちます。
		tracker.addImage(smallImg, 1);
		ImageIcon smallIcon = new ImageIcon(smallImg);
		JLabel label = new JLabel(smallIcon);
		label.setBounds((int) (x * scale), (int) (y * scale), (int) (witdh * scale), (int) (height * scale));
		return label;
	}

	private void SetBlock(JLabel label, String Id, JPanel p) {
		blocks.put(Id, label);
		// リスナーを登録
		MyMouseListener listener = new MyMouseListener(Id);
		label.addMouseListener(listener);
		label.addMouseMotionListener(listener);
		p.add(label);
	}

	private static class Validator {
		public static boolean includes(int lower, int upper, int value) {
			return lower <= value && value <= upper;
		}
	}

	public ArrayList<String> getCurrentState() {
		ArrayList<String> initialState = new ArrayList<String>();

		if (positions.get(PosName.arm).GetState() == State.FILL) {
			initialState.add("holding " + positions.get(PosName.arm).blockId);
		} else {
			initialState.add("handEmpty");
		}

		if (positions.get(PosName.left_bottom).GetState() == State.FILL) {
			initialState.add("ontable " + positions.get(PosName.left_bottom).blockId);
		}

		if (positions.get(PosName.left_middle).GetState() != State.DISABLE) {
			if (positions.get(PosName.left_middle).GetState() == State.FILL) {
				initialState.add(positions.get(PosName.left_middle).blockId + " on "
						+ positions.get(PosName.left_bottom).blockId);
			} else {
				initialState.add("clear " + positions.get(PosName.left_bottom).blockId);
			}
		}

		if (positions.get(PosName.left_top).GetState() != State.DISABLE) {
			if (positions.get(PosName.left_top).GetState() == State.FILL) {
				initialState.add(
						positions.get(PosName.left_top).blockId + " on " + positions.get(PosName.left_middle).blockId);
				initialState.add("clear " + positions.get(PosName.left_top).blockId);
			} else {
				initialState.add("clear " + positions.get(PosName.left_middle).blockId);
			}
		}

		if (positions.get(PosName.center_bottom).GetState() == State.FILL) {
			initialState.add("ontable " + positions.get(PosName.center_bottom).blockId);
		}

		if (positions.get(PosName.center_middle).GetState() != State.DISABLE) {
			if (positions.get(PosName.center_middle).GetState() == State.FILL) {
				initialState.add(positions.get(PosName.center_middle).blockId + " on "
						+ positions.get(PosName.center_bottom).blockId);
				initialState.add("clear " + positions.get(PosName.center_middle).blockId);
			} else {
				initialState.add("clear " + positions.get(PosName.center_bottom).blockId);
			}
		}

		if (positions.get(PosName.right_bottom).GetState() == State.FILL) {
			initialState.add("ontable " + positions.get(PosName.right_bottom).blockId);
			initialState.add("clear " + positions.get(PosName.right_bottom).blockId);
		}

		return initialState;
	}

	public void SetBlockArrangement(ArrayList<String> state) 
	{
		Pattern p = Pattern.compile("handEmpty");
		for (String st : state) {
			Matcher m = p.matcher(st);
			if (m.find()) {
				state.remove(st);
				break;
			}
		}

		p = Pattern.compile("holding (.)");
		for (String st : state) {
			Matcher m = p.matcher(st);
			//見つからなかったら続ける
			if (!m.find()) {
				continue;
			}
			SetBlockPosition(m.group(1), PosName.arm);
			state.remove(st);
			break;
		}

		//配置情報を表現する二次元配列
		//初めのインデックスが列数(左から)、次のインデックスが行数(下から)
		String keyPos[][] = new String[3][3];
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				keyPos[i][j] = "";
			}
		}

		int it = 0;
		p = Pattern.compile("ontable (.)");
		while(true){
			boolean isFind = false;
			for (String st : state) {
				Matcher m = p.matcher(st);
				//見つからなかったら続ける
				if (!m.find()) {
					continue;
				}
				isFind = true;
				keyPos[it++][0] = m.group(1);
				state.remove(st);
				break;
			}
			//見つからなくなったら終了
			if(!isFind){
				break;
			}
		}


		it = 0;
		p = Pattern.compile("(.) on (.)");
		while(true){
			boolean isFind = false;
			for (String st : state) {
				Matcher m = p.matcher(st);
				//見つからなかったら続ける
				if (!m.find()) {
					continue;
				}
				isFind = true;
				boolean isRemoved = false;
				//下に該当するブロックを探す
				String underKey = m.group(2);
				for (int i = 0; i < 3; ++i) {
					for (int j = 0; j < 2; ++j) {
						if(!underKey.equals(keyPos[i][j])){
							continue;
						}
						//該当するなら一つ上にブロックをセットし、状態を取り除く
						keyPos[i][j+1] = m.group(1);
						state.remove(st);
						isRemoved = true;
						break;
					}
				}
				//取り除いた後はイテレーターが保障されないから必ずbreak
				if(isRemoved){
					break;
				}
			}
			//見つからなくなったら終了
			if(!isFind){
				break;
			}
		}

		//表示の都合上、一番高い列を左に
		if(keyPos[0][1].equals("") && !keyPos[1][1].equals("")){
			for (int i = 0; i < 2; ++i) {
				String tmp = keyPos[0][i];
				keyPos[0][i] = keyPos[1][i];
				keyPos[1][i] = tmp;
			}
		}

		if (!keyPos[0][0].equals("")) {
			SetBlockPosition(keyPos[0][0], PosName.left_bottom);
		}
		if (!keyPos[0][1].equals("")) {
			SetBlockPosition(keyPos[0][1], PosName.left_middle);
		}
		if (!keyPos[0][2].equals("")) {
			SetBlockPosition(keyPos[0][2], PosName.left_top);
		}
		if (!keyPos[1][0].equals("")) {
			SetBlockPosition(keyPos[1][0], PosName.center_bottom);
		}
		if (!keyPos[1][1].equals("")) {
			SetBlockPosition(keyPos[1][1], PosName.center_middle);
		}
		if (!keyPos[2][0].equals("")) {
			SetBlockPosition(keyPos[2][0], PosName.right_bottom);
		}
	}

	private void SetBlockPosition(String key, PosName posName) 
	{
		Position pos = positions.get(posName);
		blocks.get(key).setLocation((int) (pos.x * scale), (int) (pos.y * scale));
		pos.state = State.FILL;
		pos.blockId = key;
		UpdateDisplay();
	}

	public void UpdateDisplay() 
	{
		// 左上の判定
		if (positions.get(PosName.left_top).GetState() != State.FILL) {
			if (positions.get(PosName.left_middle).GetState() == State.FILL) {
				positions.get(PosName.left_top).SetState(State.EMPTY);
			} else {
				positions.get(PosName.left_top).SetState(State.DISABLE);
			}
		}
		// 左中段の判定
		if (positions.get(PosName.left_middle).GetState() != State.FILL) {
			if (positions.get(PosName.left_bottom).GetState() == State.FILL) {
				positions.get(PosName.left_middle).SetState(State.EMPTY);
			} else {
				positions.get(PosName.left_middle).SetState(State.DISABLE);
			}
		}
		// 左下は常時表示
		// 中央中段の判定
		if (positions.get(PosName.center_middle).GetState() != State.FILL) {
			if (positions.get(PosName.center_bottom).GetState() == State.FILL) {
				positions.get(PosName.center_middle).SetState(State.EMPTY);
			} else {
				positions.get(PosName.center_middle).SetState(State.DISABLE);
			}
		}
		// 中央下の判定
		if (positions.get(PosName.center_bottom).GetState() != State.FILL) {
			if (positions.get(PosName.left_bottom).GetState() == State.FILL) {
				positions.get(PosName.center_bottom).SetState(State.EMPTY);
			} else {
				positions.get(PosName.center_bottom).SetState(State.DISABLE);
			}
		}
		// 右下の判定
		if (positions.get(PosName.right_bottom).GetState() != State.FILL) {
			if (positions.get(PosName.left_bottom).GetState() == State.FILL
					&& positions.get(PosName.center_bottom).GetState() == State.FILL) {
				positions.get(PosName.right_bottom).SetState(State.EMPTY);
			} else {
				positions.get(PosName.right_bottom).SetState(State.DISABLE);
			}
		}

	}

	private class MyMouseListener extends MouseAdapter {
		private int dx;
		private int dy;
		private int init_x;
		private int init_y;
		private String key;

		private boolean canDrag;
		private PosName prevPosName;

		public MyMouseListener(String key) {
			this.key = key;
		}

		public void mouseDragged(MouseEvent e) {
			if (!canDrag) {
				return;
			}
			// マウスの座標からラベルの左上の座標を取得する
			int x = e.getXOnScreen() - dx;
			int y = e.getYOnScreen() - dy;
			blocks.get(key).setLocation(x, y);
		}

		public void mousePressed(MouseEvent e) {
			canDrag = true;
			// 上にブロックがある場合は動かせない処理
			PosName posName = PosName.arm;
			for (PosName pn : PosName.values()) {
				Position pos = positions.get(pn);
				if (pos.state.equals(State.FILL) && pos.blockId.equals(key)) {
					posName = pn;
					break;
				}
			}
			if (posName == PosName.left_bottom && positions.get(PosName.left_middle).GetState() == State.FILL) {
				canDrag = false;
				return;
			}
			if (posName == PosName.left_middle && positions.get(PosName.left_top).GetState() == State.FILL) {
				canDrag = false;
				return;
			}
			if (posName == PosName.center_bottom && positions.get(PosName.center_middle).GetState() == State.FILL) {
				canDrag = false;
				return;
			}

			// 前状態を削除
			for (PosName pn : PosName.values()) {
				Position pos = positions.get(pn);
				if (pos.state.equals(State.FILL) && pos.blockId.equals(key)) {
					positions.get(pn).state = State.EMPTY;
					prevPosName = pn;
					break;
				}
			}
			UpdateDisplay();

			// 押さえたところからラベルの左上の差を取っておく
			init_x = blocks.get(key).getX();
			init_y = blocks.get(key).getY();
			dx = e.getXOnScreen() - init_x;
			dy = e.getYOnScreen() - init_y;
		}

		public void mouseReleased(MouseEvent e) {
			if (!canDrag) {
				return;
			}
			int x = e.getXOnScreen() - dx;
			int y = e.getYOnScreen() - dy;
			PosName posName = prevPosName;
			// 各ポジションの判定
			for (PosName n : PosName.values()) {
				if (IsFit(x, y, n)) {
					posName = n;
					break;
				}
			}
			// 可動領域でなければ初期位置に戻す
			if (posName == prevPosName) {
				blocks.get(key).setLocation(init_x, init_y);
			}
			Position pos = positions.get(posName);
			if(pos == null){
				return;
			}
			pos.state = State.FILL;
			pos.blockId = key;
			UpdateDisplay();
		}

		private boolean IsFit(int x, int y, PosName posName) {
			if (positions.get(posName).state != State.EMPTY) {
				return false;
			}
			Position pos = positions.get(posName);
			// 80 = 64(半径) + 12(遊び)
			int range = 80;
			if (Validator.includes(pos.x - range, pos.x + range, (int) (x / scale))
					&& Validator.includes(pos.y - range, pos.y + range, (int) (y / scale))) {
				blocks.get(key).setLocation((int) (pos.x * scale), (int) (pos.y * scale));
				return true;
			}
			return false;
		}
	}

}
