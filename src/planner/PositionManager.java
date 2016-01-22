package planner;

import java.util.ArrayList;
import java.util.Stack;

import javax.swing.DebugGraphics;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PositionManager
{
	public enum State 
	{
		FILL,	//ブロックが入っている状態
		EMPTY,	//ブロックが入っていない状態
		DISABLE	//非表示
	}
	public class Position 
	{
		public int x;
		public int y;
		private State state;
		public JLabel emplyLabel;
		private String blockId;
		public Position() 
		{
			state = State.EMPTY;
			emplyLabel = Graphical_sample.GenerateLabel("empty.png", x, y, 128, 128);
			Graphical_sample.instance.blankPanel.add(emplyLabel);
		}
		// 座標と同時に点線パネルとブロックの描画も更新する
		public void SetPosition(int _x,int _y) 
		{
			x = _x;
			y = _y;
			emplyLabel.setLocation(
					(int) (x * Graphical_sample.scale),
					(int) (y * Graphical_sample.scale)
					);
			if(state.equals(State.FILL)){
				Graphical_sample.SetBlockPosition(blockId, this);
			}
		}
		public void SetState(State s) 
		{
			state = s;
			if (state == State.DISABLE) {
				emplyLabel.setVisible(false);
			} else {
				emplyLabel.setVisible(true);
			}
		}
		public State GetState() 
		{
			return state;
		}
		public boolean EqualState(State opState) 
		{
			return state.equals(opState);
		}
		public String GetBlockId() 
		{
			if(EqualState(State.FILL)){
				return blockId;
			}
			return null;
		}
		public void SetBlock(String blockId) 
		{
			this.blockId = blockId;
			SetState(State.FILL);
			Graphical_sample.SetBlockPosition(blockId,this);
		}
	}
	public Position arm;
	public ArrayList<Stack<Position>> table;		
	public Stack<Position> slots;
	public PositionManager() 
	{
		arm = new Position();
		table = new ArrayList<Stack<Position>>();
		table.add(new Stack<Position>());
		table.get(table.size()-1).add(new Position());
		table.add(new Stack<Position>());
		table.get(table.size()-1).add(new Position());
		table.get(table.size()-1).add(new Position());
		slots = new Stack<>();
		slots.add(new Position());
	}
	public void AddSlot(String blockId) 
	{
		Position pos = slots.peek();
		pos.blockId = blockId;
		pos.state = State.FILL;
		pos.SetPosition(50 + (80 * slots.size()), 500);
		
		slots.add(new Position());
		pos = slots.peek();
		pos.state = State.EMPTY;
		pos.SetPosition(50 + (80 * slots.size()), 500);
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
			if (pos.state.equals(State.FILL) && pos.blockId.equals(blockId)) {
				return pos;
			}
		}
		return null;
	}
	public void SetBlock(Position nextPos, String nextBlockId, Position prevPos) 
	{
		if(prevPos != null && nextPos.EqualState(State.FILL)){
			String opBlockId = nextPos.blockId;
			prevPos.SetBlock(opBlockId);			
		}
		nextPos.SetBlock(nextBlockId);
	}
	public void UpdateDisplay() 
	{
		arm.SetPosition(448, 64);
		for (Stack<Position> stack : table) {
			for(Position pos : stack){
				int _x = 64 + (192 * table.indexOf(stack));
				int _y = 352 - (128 * stack.indexOf(pos));
				pos.SetPosition(_x,_y);
			}
		}

		//		// 左上の判定
		//		if (positions.get(PosName.left_top).GetState() != State.FILL) {
		//			if (positions.get(PosName.left_middle).GetState() == State.FILL) {
		//				positions.get(PosName.left_top).SetState(State.EMPTY);
		//			} else {
		//				positions.get(PosName.left_top).SetState(State.DISABLE);
		//			}
		//		}
		//		// 左中段の判定
		//		if (positions.get(PosName.left_middle).GetState() != State.FILL) {
		//			if (positions.get(PosName.left_bottom).GetState() == State.FILL) {
		//				positions.get(PosName.left_middle).SetState(State.EMPTY);
		//			} else {
		//				positions.get(PosName.left_middle).SetState(State.DISABLE);
		//			}
		//		}
		//		// 左下は常時表示
		//		// 中央中段の判定
		//		if (positions.get(PosName.center_middle).GetState() != State.FILL) {
		//			if (positions.get(PosName.center_bottom).GetState() == State.FILL) {
		//				positions.get(PosName.center_middle).SetState(State.EMPTY);
		//			} else {
		//				positions.get(PosName.center_middle).SetState(State.DISABLE);
		//			}
		//		}
		//		// 中央下の判定
		//		if (positions.get(PosName.center_bottom).GetState() != State.FILL) {
		//			if (positions.get(PosName.left_bottom).GetState() == State.FILL) {
		//				positions.get(PosName.center_bottom).SetState(State.EMPTY);
		//			} else {
		//				positions.get(PosName.center_bottom).SetState(State.DISABLE);
		//			}
		//		}
		//		// 右下の判定
		//		if (positions.get(PosName.right_bottom).GetState() != State.FILL) {
		//			if (positions.get(PosName.left_bottom).GetState() == State.FILL
		//					&& positions.get(PosName.center_bottom).GetState() == State.FILL) {
		//				positions.get(PosName.right_bottom).SetState(State.EMPTY);
		//			} else {
		//				positions.get(PosName.right_bottom).SetState(State.DISABLE);
		//			}
		//		}

	}
}