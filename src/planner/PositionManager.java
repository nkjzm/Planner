package planner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import javax.swing.DebugGraphics;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PositionManager
{
	public class Position 
	{
		public int x = -999;
		public int y = -999;
		private Boolean isEmpty;
		public JLabel emplyLabel;
		private String blockId;
		public Position() 
		{
			isEmpty = true;
			emplyLabel = gPlanner.GenerateLabel("empty.png", x, y);
			gPlanner.blankPanel.add(emplyLabel);
		}
		// 座標と同時に点線パネルとブロックの描画も更新する
		public void SetPosition(int _x,int _y) 
		{
			x = _x + gPlanner.offsetX;
			y = _y;
			emplyLabel.setLocation(
					(int) (x * gPlanner.scale),
					(int) (y * gPlanner.scale)
					);
			if(!isEmpty){
				gPlanner.SetBlockPosition(blockId, this);
			}
		}
		public void SetIsEmpty(Boolean flg) 
		{
			isEmpty = flg;
		}
		public Boolean GetIsEmpty() 
		{
			return isEmpty;
		}
		public String GetBlockId() 
		{
			if(!isEmpty){
				return blockId;
			}
			return null;
		}
		public void SetBlock(String blockId) 
		{
			this.blockId = blockId;
			isEmpty = false;
			gPlanner.SetBlockPosition(blockId,this);
		}
		public void Destroy() 
		{
			gPlanner.blankPanel.remove(emplyLabel);getClass();
			gPlanner.blankPanel.repaint();		
		}
	}
	public Position arm;
	public ArrayList<Stack<Position>> table;		
	public Stack<Position> slots;
	private GraphicalPlanner gPlanner;
	public int blockLength;
	public PositionManager(GraphicalPlanner graphicalPlanner) 
	{
		gPlanner = graphicalPlanner;
		arm = new Position();
		table = new ArrayList<Stack<Position>>();
		table.add(new Stack<Position>());
		table.get(table.size()-1).add(new Position());
		table.add(new Stack<Position>());
		table.get(table.size()-1).add(new Position());
		table.get(table.size()-1).add(new Position());
		slots = new Stack<>();
		slots.add(new Position());
		blockLength = arm.emplyLabel.getWidth();
	}
	public void AddSlot(String blockId) 
	{
		Position pos = slots.peek();
		pos.blockId = blockId;
		pos.isEmpty = false;

		slots.add(new Position());
	}
	public void Reset() 
	{
		//スロットポジションをデストロイ
		Iterator<Position> itr = slots.iterator();
		while (itr.hasNext()) {
			Position pos = itr.next();
			pos.Destroy();
			itr.remove();
		}
		//メインポジションをデストロイ
		Iterator<Stack<Position>> listItr = table.iterator();
		while (listItr.hasNext()) {
			Stack<Position> stack = listItr.next();
			Iterator<Position> stackItr = stack.iterator();
			while (stackItr.hasNext()) {
				Position pos = stackItr.next();
				pos.Destroy();
				stackItr.remove();
			}
		}
		//アームをデストロイ
		arm.Destroy();

		arm = new Position();
		table.add(new Stack<Position>());
		table.get(table.size()-1).add(new Position());
		slots = new Stack<>();
		slots.add(new Position());
	}
	private void UpdateSlot()
	{
		Iterator<Position> itr = slots.iterator();
		while (itr.hasNext()) {
			Position pos = itr.next();
			if(pos.isEmpty){
				pos.Destroy();
				itr.remove();
			}
		}
		slots.add(new Position());
	}
	private void UpdateTable() 
	{
		Iterator<Stack<Position>> listItr = table.iterator();
		while (listItr.hasNext()) {
			Stack<Position> stack = listItr.next();
			Iterator<Position> stackItr = stack.iterator();
			while (stackItr.hasNext()) {
				Position pos = stackItr.next();
				if(pos.isEmpty){
					pos.Destroy();
					stackItr.remove();
				}
			}
			if(stack.empty()){
				listItr.remove();
			}else{
				stack.add(new Position());
			}
		}
		table.add(new Stack<Position>());
		table.get(table.size()-1).add(new Position());		
	}
	public ArrayList<Position> GetAllPosition() 
	{
		ArrayList<Position> positions = new ArrayList<Position>();
		positions.add(arm);
		for (Stack<Position> stack : table) {
			positions.addAll(stack);
		}
		positions.addAll(slots);
		return positions;
	}
	public Position GetPosition(String blockId) 
	{
		ArrayList<Position> positions = GetAllPosition();
		for (Position pos : positions) {
			if (!pos.isEmpty && pos.blockId.equals(blockId)) {
				return pos;
			}
		}
		return null;
	}
	public void SetBlock(Position nextPos, String nextBlockId, Position prevPos) 
	{
		if(prevPos != null){
			if(!nextPos.isEmpty){
				String opBlockId = nextPos.blockId;
				prevPos.SetBlock(opBlockId);			
			}else{
				prevPos.isEmpty = true;
			}
		}
		nextPos.SetBlock(nextBlockId);
	}
	public void PutBlock(String blockId) 
	{
		PutBlock(blockId,false);	
	}
	public void PutBlock(String blockId, boolean isArm) 
	{
		if(isArm){
			arm.blockId = blockId;
			arm.isEmpty = false;
			return;
		}
		//新しい列にブロックを追加
		Position position = table.get(table.size()-1).peek();
		position.blockId = blockId;
		position.isEmpty = false;
		//一つ上のポジションと新しい列のポジションを追加
		table.get(table.size()-1).add(new Position());
		table.add(new Stack<Position>());
		table.get(table.size()-1).add(new Position());
	}
	public boolean PutBlock(String blockId, String underBlockId) 
	{
		Iterator<Stack<Position>> listItr = table.iterator();
		while (listItr.hasNext()) {
			Stack<Position> stack = listItr.next();
			Iterator<Position> stackItr = stack.iterator();
			Boolean isFind = false;
			while (stackItr.hasNext()) {
				Position position = stackItr.next();
				if(isFind){
					position.blockId = blockId;
					position.isEmpty = false;
					stack.add(new Position());
					return true;
				}
				if(!position.isEmpty
						&& position.blockId.equals(underBlockId)){
					isFind = true;
				}
			}
		}
		return false;
	}
	public void UpdateDisplay() 
	{
		float scale = gPlanner.scale;
		scale = Math.min(scale, 6f/slots.size() * gPlanner.scale);
		scale = Math.min(scale, 6f/table.size() * gPlanner.scale);
		for (Stack<Position> stack : table) {
			scale = Math.min(scale, 5.5f/stack.size() * gPlanner.scale);
		}
		gPlanner.blockScale = scale;
		gPlanner.UpdateBlockScale();
		blockLength = arm.emplyLabel.getWidth();
		
		int fixedBlockLength = (int)(blockLength/gPlanner.scale);
		
		UpdateSlot();	//スロットの内部状態を更新
		for(Position pos : slots){
			int x = 180 + ((920/slots.size()) * slots.indexOf(pos));
			int y = 908 - fixedBlockLength;
			pos.SetPosition(x,y);
		}

		int armX = 800 + (int)(10 * gPlanner.blockScale / gPlanner.scale);
		int armY = (int)(62 * gPlanner.blockScale / gPlanner.scale);
		arm.SetPosition(armX, armY);

		UpdateTable();	//テーブルの内部状態を更新
		for (Stack<Position> stack : table) {
			for(Position pos : stack){
				int x = 220 + ((860/table.size()) * table.indexOf(stack));
				int y = 708 - (fixedBlockLength * (stack.indexOf(pos)+1));
				pos.SetPosition(x,y);
			}
		}
	}
}