import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
public class Graphical_sample extends JPanel
{
private static float scale;
public static void main(String args[])
{
Graphical_sample aaa = new Graphical_sample(0.5f);
JFrame jframe = new JFrame("DrawRect");
Container c = jframe.getContentPane();
c.add(aaa, BorderLayout.CENTER);
jframe.setSize((int)(800*scale), (int)(608*scale));
jframe.setVisible(true);
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
//stateがFillの時のみ利用。それ以外の場合は不定とする。
public String blockId;
public Position(int X, int Y,JPanel p)
{
x = X;
y = Y;
state = State.DISABLE;
label = GenerateLabel("empty.png",x, y, 128, 128);
p.add(label);
// new Timer(30,(ActionListener) label).start();
//TODO: イベントリスナー定義してタイマーオブジェクトをいじる
}
}
Graphical_sample(float scale){
this.scale = scale;
this.setLayout(null);
setBounds(0, 0, 800, 608);
positions.put(PosName.arm, new Position(448, 64,this));
positions.put(PosName.left_bottom, new Position(64, 352,this));
positions.put(PosName.left_middle, new Position(64, 224,this));
positions.put(PosName.left_top, new Position(64, 96,this));
positions.put(PosName.center_bottom,new Position(256, 352,this));
positions.put(PosName.center_middle,new Position(256, 224,this));
positions.put(PosName.right_bottom, new Position(448, 352,this));
//ブロック
SetBlock(GenerateLabel("block_a.png",650, 64, 128, 128),this);
SetBlock(GenerateLabel("block_b.png",650, 256, 128, 128),this);
SetBlock(GenerateLabel("block_c.png",650, 448, 128, 128),this);
//固定パーツ
JLabel arm = GenerateLabel("arm.png",438, 0, 148, 128);
this.add(arm);
JLabel floor = GenerateLabel("floor.png",0, 480, 640, 128);
this.add(floor);
//点線ラベルをパネルに追加
for (PosName n : PosName.values()) {
this.add(positions.get(n).label);
}
}
private JLabel GenerateLabel(String imgName, int x, int y, int witdh, int height)
{
ImageIcon icon = new ImageIcon("./img/"+imgName);
MediaTracker tracker = new MediaTracker(this);
// ポイント２．getScaledInstanceで大きさを変更します。
Image smallImg = icon.getImage().getScaledInstance((int) (icon.getIconWidth() * scale), -1,
Image.SCALE_SMOOTH);
// ポイント３．MediaTrackerで処理の終了を待ちます。
tracker.addImage(smallImg, 1);
ImageIcon smallIcon = new ImageIcon(smallImg);
JLabel label = new JLabel(smallIcon);
label.setBounds((int)(x*scale), (int)(y*scale), (int)(witdh*scale), (int)(height*scale));
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
public ArrayList<String> getCurrentState(){
ArrayList<String> initialState = new ArrayList<String>();
return initialState;
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
//各ポジションの判定
for (PosName n : PosName.values()) {
//判定されなければ続行
if(!IsFit(x, y, n)){
continue;
}
//判定された場合
//前状態を削除
for (PosName pn : PosName.values()) {
Position pos = positions.get(pn);
if(pos.state.equals(State.FILL) && pos.blockId.equals(key)){
positions.get(pn).state = State.EMPTY;
break;
}
}
//現在状態をセット
Position pos = positions.get(n);
pos.state = State.FILL;
pos.blockId = key;
return;
}
//可動領域でなければ初期位置に戻す
blocks.get(key).setLocation(init_x, init_y);
}
private boolean IsFit(int x,int y, PosName posName)
{
Position pos = positions.get(posName);
//80 = 64(半径) + 12(遊び)
int range = 80;
if(Validator.includes(pos.x-range, pos.x+range, (int)(x/scale))
&& Validator.includes(pos.y-range, pos.y+range, (int)(y/scale))){
blocks.get(key).setLocation((int)(pos.x*scale), (int)(pos.y*scale));
return true;
}
return false;
}
}


}
