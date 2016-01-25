package planner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Label;
import java.awt.MediaTracker;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
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

public class GraphicalPlanner extends JPanel 
{
	public float scale;
	public float blockScale;
	public int offsetX;

	public JPanel bgPanel;
	public JPanel blankPanel;
	public JPanel blockPanel;
	public JPanel uiPanel;
	public JLabel arm;
	public JLabel swapLabel;
	public JLabel[] dustboxLabels;
	public JLabel[] addLabels;

	public static void main(String args[]) 
	{
		GraphicalPlanner aaa = new GraphicalPlanner(0.3f,true);
		JFrame jframe = new JFrame("DrawRect");
		jframe.setVisible(true);
		Container c = jframe.getContentPane();
		c.add(aaa, BorderLayout.CENTER);
		//ウィンドウのタイトルバーと枠を考慮
		jframe.getContentPane().setPreferredSize(
				new Dimension(aaa.getSize().width,aaa.getSize().height));
		jframe.pack();

		ArrayList<String> tmp = new ArrayList<String>();
		tmp.add("ontable A");
		tmp.add("ontable B");
		tmp.add("C on B");
		tmp.add("I on C");
		tmp.add("clear C");
		//tmp.add("holding D");
		aaa.SetBlockArrangement(tmp);
	}

	HashMap<String, JLabel> blocks = new HashMap<String, JLabel>();
	private PositionManager pManager;

	public GraphicalPlanner(float scale,boolean isFull) 
	{
		offsetX = -180;
		int offsetY = -180;
		if(isFull){
			offsetX = offsetY = 0;
		}
		
		this.scale = scale;
		blockScale = scale;
		this.setLayout(null);
		setBounds(0, 0, (int) (1280 * scale) + (int)(offsetX*2*scale), (int) (960 * scale) + (int)(offsetY*scale));

		this.setLayout(new OverlayLayout(this));

		InitPanel(bgPanel = new JPanel());		//背景描画用
		InitPanel(blankPanel = new JPanel());	//点線パネル描画用
		InitPanel(blockPanel = new JPanel());	//ブロック描画用
		InitPanel(uiPanel = new JPanel());		//UI描画用

		pManager = new PositionManager(this);

		//ブロック
		for(int i = 0;i<3;++i){
			AddBlock();
		}

		//背景
		JLabel bg = GenerateLabel("bg.png", 0+offsetX, 710);
		bgPanel.add(bg);

		//アーム
		arm = GenerateLabel("arm.png", 800+offsetX, 0);
		bgPanel.add(arm);

		//ゴミ箱
		dustboxLabels = new JLabel[2];
		for(int i=0;i<2;++i){
			dustboxLabels[i] = 
					GenerateLabel("dustbox_"+i+".png", (i==0?22:12) + offsetX, i==0?718:628);
			bgPanel.add(dustboxLabels[i]);
		}
		dustboxLabels[1].setVisible(false);

		//追加ボタン
		addLabels = new JLabel[2];
		for(int i=0;i<2;++i){
			addLabels[i] = GenerateLabel("add_"+i+".png", 1121 + offsetX, 780);
			bgPanel.add(addLabels[i]);
		}
		AddMouseListener listener = new AddMouseListener();
		addLabels[0].addMouseListener(listener);
		addLabels[0].addMouseMotionListener(listener);
		addLabels[1].setVisible(false);
		addLabels[1].setOpaque(false);

		//UIパーツ
		swapLabel = GenerateLabel("swap.png", 0, 0);
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
		char peekBlockId = 'a' - 1;
		for(int i=0;i<'p'-'a'+1;++i){
			++peekBlockId;
			String blockId = String.valueOf(peekBlockId);
			Boolean isUse = false;
			for(String key : blocks.keySet()){
				if(key.equals(blockId)){
					isUse = true;
					break;
				}
			}
			if(!isUse){
				SetBlock(GenerateLabel("block_"+blockId+".png", -999, -999), blockId);
				pManager.AddSlot(blockId);
				return;
			}
		}
	}
	public void RemoveBlock(String blockId) 
	{
		RemoveBlock(blockId,false);
	}
	public void RemoveBlock(String blockId,Boolean selfRemove) 
	{
		blockPanel.remove(blocks.get(blockId));
		blockPanel.repaint();
		if(!selfRemove){
			pManager.GetPosition(blockId).SetIsEmpty(true);
			blocks.remove(blockId);
		}
	}

	public JLabel GenerateLabel(String imgName, int x, int y) 
	{
		ImageIcon icon = new ImageIcon("./img/" + imgName);
		MediaTracker tracker = new MediaTracker(this);
		Image smallImg = icon.getImage().getScaledInstance(
				(int) (icon.getIconWidth() * blockScale), -1,Image.SCALE_SMOOTH);
		tracker.addImage(smallImg, 1);
		ImageIcon smallIcon = new ImageIcon(smallImg);
		JLabel label = new JLabel(smallIcon);
		label.setBounds((int) (x * scale), (int) (y * scale)
				, smallIcon.getIconWidth(), smallIcon.getIconHeight());
		return label;
	}

	public void UpdateBlockScale() 
	{
		HashMap<String, JLabel> tmpBlocks = new HashMap<>(blocks);
		Iterator<String> iterator = blocks.keySet().iterator();
		while(iterator.hasNext()) {
			String blockId = iterator.next();
			RemoveBlock(blockId,true);
			iterator.remove();
		}
		blocks.clear();
		for (String blockId : tmpBlocks.keySet()) {
			SetBlock(GenerateLabel("block_"+blockId+".png", -999, -999), blockId);
		}
		ArrayList<Position> positions = pManager.GetAllPosition();
		blankPanel.removeAll();
		for (Position position : positions) {
			position.emplyLabel = GenerateLabel("empty.png", 0, 0);
			blankPanel.add(position.emplyLabel);
		}
		bgPanel.remove(arm);
		arm = GenerateLabel("arm.png", 800 + offsetX, 0);
		bgPanel.add(arm);
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

	public void Reset() 
	{
		Iterator<String> iterator = blocks.keySet().iterator();
		while(iterator.hasNext()){
			String blockId = iterator.next();
			RemoveBlock(blockId, true);
			iterator.remove();
		}
	}

	public ArrayList<String> getCurrentState() 
	{
		ArrayList<String> state = new ArrayList<String>();

		Iterator<Stack<Position>> listItr = pManager.table.iterator();
		while (listItr.hasNext()) {
			Stack<Position> stack = listItr.next();
			Iterator<Position> stackItr = stack.iterator();
			String underBlockId = "";
			while (stackItr.hasNext()) {
				Position pos = stackItr.next();
				if(!pos.GetIsEmpty()){
					if(stack.get(0).equals(pos)){
						String blockId = pos.GetBlockId().toUpperCase();
						state.add("ontable " + blockId);
						underBlockId = pos.GetBlockId().toUpperCase();
					}else{
						String blockId = pos.GetBlockId().toUpperCase();
						state.add(blockId + " on " + underBlockId);
						underBlockId = blockId;
					}
				}else if(stack.peek().equals(pos) && !underBlockId.equals("")){
					state.add("clear " + underBlockId);
				}
			}
		}

		if (!pManager.arm.GetIsEmpty()) {
			state.add("holding " + pManager.arm.GetBlockId().toUpperCase());
		} else {
			state.add("handEmpty");
		}

		return state;
	}

	public void SetBlockArrangement(ArrayList<String> state) 
	{
		pManager.Reset();
		Reset();

		Pattern p = Pattern.compile("handEmpty");
		Iterator<String> iterator = state.iterator();
		while(iterator.hasNext()) {
			String st = iterator.next();
			Matcher m = p.matcher(st);
			if (m.find()) {
				iterator.remove();
				break;
			}
		}

		p = Pattern.compile("holding (.)");
		iterator = state.iterator();
		while(iterator.hasNext()) {
			String st = iterator.next();
			Matcher m = p.matcher(st);
			if (m.find()) {
				String blockId = m.group(1).toLowerCase();
				SetBlock(GenerateLabel("block_"+blockId+".png", -999, -999), blockId);
				pManager.PutBlock(blockId,true);
				iterator.remove();
				break;
			}
		}

		p = Pattern.compile("ontable (.)");
		iterator = state.iterator();
		while(iterator.hasNext()) {
			String st = iterator.next();
			Matcher m = p.matcher(st);
			if (m.find()) {
				String blockId = m.group(1).toLowerCase();
				SetBlock(GenerateLabel("block_"+blockId+".png", -999, -999), blockId);
				pManager.PutBlock(blockId);
				iterator.remove();
			}
		}

		p = Pattern.compile("(.) on (.)");
		while(true){
			boolean isExist = false;
			iterator = state.iterator();
			while(iterator.hasNext()) {
				String st = iterator.next();
				Matcher m = p.matcher(st);
				if (m.find()) {
					isExist = true;
					String blockId = m.group(1).toLowerCase();
					String underBlockId = m.group(2).toLowerCase();
					SetBlock(GenerateLabel("block_"+blockId+".png", -999, -999), blockId);
					//置けたら取り除く
					if(pManager.PutBlock(blockId,underBlockId)){
						iterator.remove();
					}
				}
			}
			//パターンが無くなったらbreak
			if(!isExist){break;}
		}

		pManager.UpdateDisplay();
	}

	public void SetBlockPosition(String blockId, Position pos) 
	{
		blocks.get(blockId).setLocation((int) (pos.x * scale), (int) (pos.y * scale));
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

			//ゴミ箱の上なら削除
			if(IsOnDustbox(x, y)){
				dustboxLabels[0].setVisible(false);
				dustboxLabels[1].setVisible(true);
				return;
			}
			dustboxLabels[0].setVisible(true);
			dustboxLabels[1].setVisible(false);

			// 各ポジションの判定
			ArrayList<Position> positions = pManager.GetAllPosition();
			for (Position pos : positions) {
				if(IsFit(x, y, pos) && !pos.GetIsEmpty() 
						&& !pManager.GetPosition(blockId).equals(pos))
				{
					swapLabel.setLocation(x + (int)(10*scale), y + (int)(20*scale));
					swapLabel.setVisible(true);
					blocks.get(pos.GetBlockId()).setBackground(new Color(1, 1, 1, 150));
					return;
				}
			}
			swapLabel.setVisible(false);
		}

		//ドラッグ開始時の処理
		public void mousePressed(MouseEvent e) 
		{
			canDrag = true;

			// 前状態を削除
			prevPos = pManager.GetPosition(blockId);
			//prevPos.SetState(State.EMPTY);

			//最前列に移動
			blockPanel.add(blocks.get(blockId),0);

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
			dustboxLabels[0].setVisible(true);
			dustboxLabels[1].setVisible(false);

			int x = e.getXOnScreen() - dx;
			int y = e.getYOnScreen() - dy;
			Position currentPos = prevPos;

			//ゴミ箱の上なら削除
			if(IsOnDustbox(x, y)){
				RemoveBlock(blockId);
				pManager.UpdateDisplay();
				return;
			}

			// 各ポジションの判定
			ArrayList<Position> positions = pManager.GetAllPosition();
			for (Position pos : positions) {
				if(IsFit(x, y, pos))
				{
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
			//なんか二回呼ばないといけない・・・
			pManager.UpdateDisplay();
			pManager.UpdateDisplay();
		}

		private boolean IsFit(int x, int y, Position pos) 
		{
			// 80 = 64(半径) + 12(遊び)
			int range = (int)(pManager.blockLength * 1.2f);
			if (includes(pos.x - range, pos.x + range, (int) (x / scale))
					&& includes(pos.y - range, pos.y + range, (int) (y / scale))) {
				return true;
			}
			return false;
		}
		private boolean IsOnDustbox(int x, int y) 
		{
			int range = (int)(dustboxLabels[0].getSize().width * 1.2f) - pManager.blockLength/2;
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
				pManager.UpdateDisplay();
				//				for(String str :getCurrentState()){
				//					System.out.println(str);
				//				}
			}
		}
	}
}
