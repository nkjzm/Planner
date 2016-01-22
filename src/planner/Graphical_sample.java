package planner;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Label;
import java.awt.MediaTracker;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.Destroyable;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.text.LayeredHighlighter.LayerPainter;

import planner.PositionManager.Position;
import planner.PositionManager.State;

public class Graphical_sample extends JPanel 
{
	public static float scale;
	public static Graphical_sample instance;
	public JPanel bgPanel;
	public JPanel blankPanel;
	public JPanel blockPanel;
	public JPanel uiPanel;
	public JLabel swapLabel;
	public JLabel[] dustboxLabels;
	public JLabel[] addLabels;

	private char peekBlockId = 'a' - 1;

	public static void main(String args[]) 
	{
		Graphical_sample aaa = new Graphical_sample(0.5f);
		JFrame jframe = new JFrame("DrawRect");
		jframe.setVisible(true);
		Container c = jframe.getContentPane();
		c.add(aaa, BorderLayout.CENTER);
		//ウィンドウのタイトルバーと枠を考慮
		jframe.getContentPane().setPreferredSize(new Dimension(aaa.getSize().width,aaa.getSize().height));
		jframe.pack();

		//		ArrayList<String> tmp = new ArrayList<String>();
		//		tmp.add("ontable A");
		//		tmp.add("ontable B");
		//		tmp.add("C on B");
		//		tmp.add("clear C");
		//		aaa.SetBlockArrangement(tmp);
	}

	HashMap<String, JLabel> blocks = new HashMap<String, JLabel>();
	private PositionManager pManager;

	Graphical_sample(float scale) 
	{
		this.scale = scale;
		instance = this;
		this.setLayout(null);
		setBounds(0, 0, (int) (1280 * scale), (int) (720 * scale));

		this.setLayout(new OverlayLayout(this));

		InitPanel(bgPanel = new JPanel());		//背景描画用
		InitPanel(blankPanel = new JPanel());	//点線パネル描画用
		InitPanel(blockPanel = new JPanel());	//ブロック描画用
		InitPanel(uiPanel = new JPanel());		//UI描画用

		pManager = new PositionManager();

		//ブロック
		for(int i = 0;i<3;++i){
			AddBlock();
		}

		//背景
		JLabel bg = GenerateLabel("bg.png", 0, 470);
		bgPanel.add(bg);
		//アーム
		JLabel arm = GenerateLabel("arm.png", 800, 0);
		bgPanel.add(arm);
		//ゴミ箱
		dustboxLabels = new JLabel[2];
		for(int i=0;i<2;++i){
			dustboxLabels[i] = GenerateLabel("dustbox_"+i+".png", i==0?22:13, i==0?477:388);
			bgPanel.add(dustboxLabels[i]);
		}
		dustboxLabels[1].setVisible(false);
		//追加ボタン
		addLabels = new JLabel[2];
		for(int i=0;i<2;++i){
			addLabels[i] = GenerateLabel("add_"+i+".png", 1121, 540);
			bgPanel.add(addLabels[i]);
		}
		AddMouseListener listener = new AddMouseListener();
		addLabels[0].addMouseListener(listener);
		addLabels[0].addMouseMotionListener(listener);
		addLabels[1].setVisible(false);
		addLabels[1].setOpaque(false);

		//UIパーツ
		swapLabel = GenerateLabel("swap.png", 700, 700);
		uiPanel.add(swapLabel, 0);
		swapLabel.setVisible(false);

		pManager.UpdateDisplay();
	}

	private void  InitPanel(JPanel panel) 
	{
		panel.setOpaque(false);
		panel.setLayout(null);
		this.add(panel,0);
	}

	public void AddBlock() 
	{
		for(int i=0;i<'i'-'a';++i){
			if(++peekBlockId>'i'){
				peekBlockId = 'a';
			}
			String blockId = String.valueOf(peekBlockId);
			Boolean isUse = false;
			for(String key : blocks.keySet()){
				if(key.equals(blockId)){
					isUse = true;
					break;
				}
			}
			if(!isUse){
				SetBlock(GenerateLabel("block_"+blockId+".png", 0, 0), blockId);
				pManager.AddSlot(blockId);
				return;
			}
		}
	}
	public void RemoveBlock(String blockId) 
	{
		pManager.GetPosition(blockId).SetState(State.EMPTY);
		blockPanel.remove(blocks.get(blockId));
		blockPanel.repaint();
		blocks.remove(blockId);
		pManager.UpdateDisplay();
	}

	public static JLabel GenerateLabel(String imgName, int x, int y) 
	{
		ImageIcon icon = new ImageIcon("./img/" + imgName);
		MediaTracker tracker = new MediaTracker(instance);
		Image smallImg = icon.getImage().getScaledInstance((int) (icon.getIconWidth() * scale), -1, Image.SCALE_SMOOTH);
		tracker.addImage(smallImg, 1);
		ImageIcon smallIcon = new ImageIcon(smallImg);
		JLabel label = new JLabel(smallIcon);
		label.setBounds((int) (x * scale), (int) (y * scale), (int) (smallIcon.getIconWidth()), (int) (smallIcon.getIconHeight()));
		return label;
	}

	private void SetBlock(JLabel label, String Id) 
	{
		blocks.put(Id, label);
		// リスナーを登録
		MyMouseListener listener = new MyMouseListener(Id);
		label.addMouseListener(listener);
		label.addMouseMotionListener(listener);
		blockPanel.add(label, 0);
	}

	public ArrayList<String> getCurrentState() 
	{
		ArrayList<String> initialState = new ArrayList<String>();

		//		if (positions.get(PosName.left_bottom).GetState() == State.FILL) {
		//			initialState.add("ontable " + positions.get(PosName.left_bottom).blockId);
		//		}
		//
		//		if (positions.get(PosName.left_middle).GetState() != State.DISABLE) {
		//			if (positions.get(PosName.left_middle).GetState() == State.FILL) {
		//				initialState.add(positions.get(PosName.left_middle).blockId + " on "
		//						+ positions.get(PosName.left_bottom).blockId);
		//			} else {
		//				initialState.add("clear " + positions.get(PosName.left_bottom).blockId);
		//			}
		//		}
		//
		//		if (positions.get(PosName.left_top).GetState() != State.DISABLE) {
		//			if (positions.get(PosName.left_top).GetState() == State.FILL) {
		//				initialState.add(
		//						positions.get(PosName.left_top).blockId + " on " + positions.get(PosName.left_middle).blockId);
		//				initialState.add("clear " + positions.get(PosName.left_top).blockId);
		//			} else {
		//				initialState.add("clear " + positions.get(PosName.left_middle).blockId);
		//			}
		//		}
		//
		//		if (positions.get(PosName.center_bottom).GetState() == State.FILL) {
		//			initialState.add("ontable " + positions.get(PosName.center_bottom).blockId);
		//		}
		//
		//		if (positions.get(PosName.center_middle).GetState() != State.DISABLE) {
		//			if (positions.get(PosName.center_middle).GetState() == State.FILL) {
		//				initialState.add(positions.get(PosName.center_middle).blockId + " on "
		//						+ positions.get(PosName.center_bottom).blockId);
		//				initialState.add("clear " + positions.get(PosName.center_middle).blockId);
		//			} else {
		//				initialState.add("clear " + positions.get(PosName.center_bottom).blockId);
		//			}
		//		}
		//
		//		if (positions.get(PosName.right_bottom).GetState() == State.FILL) {
		//			initialState.add("ontable " + positions.get(PosName.right_bottom).blockId);
		//			initialState.add("clear " + positions.get(PosName.right_bottom).blockId);
		//		}
		//
		//		if (positions.get(PosName.arm).GetState() == State.FILL) {
		//			initialState.add("holding " + positions.get(PosName.arm).blockId);
		//		} else {
		//			initialState.add("handEmpty");
		//		}

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
			//TODO ブロックの描画座標を更新
			//SetBlockPosition(m.group(1), PosName.arm);
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
			//TODO ブロックの描画座標を更新
			//SetBlockPosition(keyPos[0][0], PosName.left_bottom);
		}
		if (!keyPos[0][1].equals("")) {
			//TODO ブロックの描画座標を更新
			//SetBlockPosition(keyPos[0][1], PosName.left_middle);
		}
		if (!keyPos[0][2].equals("")) {
			//TODO ブロックの描画座標を更新
			//SetBlockPosition(keyPos[0][2], PosName.left_top);
		}
		if (!keyPos[1][0].equals("")) {
			//TODO ブロックの描画座標を更新
			//SetBlockPosition(keyPos[1][0], PosName.center_bottom);
		}
		if (!keyPos[1][1].equals("")) {
			//TODO ブロックの描画座標を更新
			//SetBlockPosition(keyPos[1][1], PosName.center_middle);
		}
		if (!keyPos[2][0].equals("")) {
			//TODO ブロックの描画座標を更新
			//SetBlockPosition(keyPos[2][0], PosName.right_bottom);
		}
	}

	public static void SetBlockPosition(String blockId, Position pos) 
	{
		instance.blocks.get(blockId).setLocation((int) (pos.x * scale), (int) (pos.y * scale));
	}
	private boolean includes(int lower, int upper, int value) {
		return lower <= value && value <= upper;
	}

	private class MyMouseListener extends MouseAdapter 
	{
		private int dx;
		private int dy;
		private int init_x;
		private int init_y;
		private String blockId;

		private boolean canDrag;
		private Position prevPos;

		public MyMouseListener(String blockId) 
		{
			this.blockId = blockId;
		}

		//ドラッグ中の処理
		public void mouseDragged(MouseEvent e) 
		{
			//ドラッグ中でなければreturn
			if (!canDrag) { return;}

			// マウスの座標からラベルの左上の座標を取得する
			int x = e.getXOnScreen() - dx;
			int y = e.getYOnScreen() - dy;
			blocks.get(blockId).setLocation(x, y);

			// 各ポジションの判定
			ArrayList<Position> positions = pManager.GetAllPosition();
			for (Position pos : positions) {
				if(IsFit(x, y, pos) && pos.EqualState(State.FILL))
				{
					swapLabel.setLocation(x + (int)(10*scale), y + (int)(20*scale));
					swapLabel.setVisible(true);
					return;
				}
			}
			swapLabel.setVisible(false);
		}

		//ドラッグ開始時の処理
		public void mousePressed(MouseEvent e) 
		{
			canDrag = true;

			//			// 上にブロックがある場合は動かせない処理
			//			po posName = PosName.arm;
			//			for (PosName pn : PosName.values()) {
			//				Position pos = positions.get(pn);
			//				if (pos.state.equals(State.FILL) && pos.blockId.equals(key)) {
			//					posName = pn;
			//					break;
			//				}
			//			}
			//			if (posName == PosName.left_bottom && positions.get(PosName.left_middle).GetState() == State.FILL) {
			//				canDrag = false;
			//				return;
			//			}
			//			if (posName == PosName.left_middle && positions.get(PosName.left_top).GetState() == State.FILL) {
			//				canDrag = false;
			//				return;
			//			}
			//			if (posName == PosName.center_bottom && positions.get(PosName.center_middle).GetState() == State.FILL) {
			//				canDrag = false;
			//				return;
			//			}

			// 前状態を削除
			prevPos = pManager.GetPosition(blockId);
			//prevPos.SetState(State.EMPTY);

			//最前列に移動
			blockPanel.add(blocks.get(blockId),0);

			pManager.UpdateDisplay();	//描画状態を更新

			// 押さえたところからラベルの左上の差を取っておく
			init_x = blocks.get(blockId).getX();
			init_y = blocks.get(blockId).getY();
			dx = e.getXOnScreen() - init_x;
			dy = e.getYOnScreen() - init_y;
		}

		//ドラッグ終了時の処理
		public void mouseReleased(MouseEvent e) 
		{
			//ドラッグ状態となっていなければreturn
			if (!canDrag) {return;}

			swapLabel.setVisible(false);

			int x = e.getXOnScreen() - dx;
			int y = e.getYOnScreen() - dy;
			Position currentPos = prevPos;
			
			//ゴミ箱の上なら削除
			if(IsOnDustbox(x, y)){
				RemoveBlock(blockId);
				return;
			}

			// 各ポジションの判定
			ArrayList<Position> positions = pManager.GetAllPosition();
			for (Position pos : positions) {
				if(IsFit(x, y, pos))
				{
					//無効なブロック上ならbreak;
					if (pos.EqualState(State.DISABLE)) {break;}

					//位置の更新
					//異動先にブロックがある場合はスワップ
					pManager.SetBlock(pos,blockId,prevPos);
					currentPos = pos;
					break;
				}
			}
			// 可動領域でなければ初期位置に戻す
			if (currentPos.equals(prevPos)) {
				pManager.SetBlock(prevPos,blockId,null);
			}
			//情報を更新
			pManager.UpdateDisplay();
		}

		private boolean IsFit(int x, int y, Position pos) 
		{
			// 80 = 64(半径) + 12(遊び)
			int range = 80;
			if (includes(pos.x - range, pos.x + range, (int) (x / scale))
					&& includes(pos.y - range, pos.y + range, (int) (y / scale))) {
				return true;
			}
			return false;
		}
		private boolean IsOnDustbox(int x, int y) 
		{
			int range = (int)(dustboxLabels[0].getSize().width * 0.5);
			int posX = dustboxLabels[0].getX();
			int posY = dustboxLabels[0].getY();
			if (includes(posX - range, posX + range, x)
					&& includes(posY - range, posY + range, y)) {
				return true;
			}
			return false;
		}


	}
	private class AddMouseListener extends MouseAdapter 
	{
		int dx,dy;
		//ドラッグ開始時の処理
		public void mousePressed(MouseEvent e) 
		{
			addLabels[0].setVisible(false);
			addLabels[1].setVisible(true);
			dx = e.getXOnScreen() - addLabels[0].getX();
			dy = e.getYOnScreen() - addLabels[0].getY();
		}
		//ドラッグ終了時の処理
		public void mouseReleased(MouseEvent e) 
		{
			addLabels[0].setVisible(true);
			addLabels[1].setVisible(false);

			//ボタンの上で話した場合のみクリックと判定
			int range = (int)(addLabels[0].getSize().width * 0.5);
			int x = e.getXOnScreen() - dx;
			int y = e.getYOnScreen() - dy;
			int posX = addLabels[0].getX();
			int posY = addLabels[0].getY();
			if (includes(posX - range, posX + range, x)
					&& includes(posY - range, posY + range, y)) {
				AddBlock();
			}
		}
	}
}
