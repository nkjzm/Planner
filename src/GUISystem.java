import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.util.*;
import java.util.List;

/**
 * Javaによる簡単な計算機
 *
 */
public class GUISystem {
	static GUISystemFrame gf;    
	public static void main(String args[]){
		gf = new GUISystemFrame();
		gf.show();
	}
}

class GUISystemFrame extends Frame implements ActionListener, KeyListener
{
	Label text;
	Button button[];
	Panel numberPanel;
	Button clear,plus,minus,equal;
	Button minority, pow, log;    
	Panel commandPanel;
	String buffer;
	Double result;
	String operator;
	boolean append = false;
	enum MINORITY_STATE{NONE,PUTTED,EXIST};
	MINORITY_STATE minorityState = MINORITY_STATE.NONE;
	String keyBuffer;

	GUISystemFrame()
	{
		addWindowListener(new ClosingWindowListener());

		setTitle("Calculator");

		initBuffer();
		initOperator();
		text = new Label(buffer,Label.RIGHT);
		text.setBackground(Color.white);
		showBuffer();
		result = Double.parseDouble(buffer);

		button = new Button[10];
		for(int i = 0  ; i < 10 ; i++){
			button[i] = new Button((new Integer(i)).toString());
			button[i].addActionListener(this);
		}

		clear = new Button("C");
		clear.addActionListener(this);	
		plus  = new Button("+");
		plus.addActionListener(this);	
		minus = new Button("-");
		minus.addActionListener(this);	
		equal = new Button("=");
		equal.addActionListener(this);	
		minority = new Button(".");
		minority.addActionListener(this);
		log = new Button("log");
		log.addActionListener(this);
		pow = new Button("pow");
		pow.addActionListener(this);

		numberPanel = new Panel();
		numberPanel.setLayout(new GridLayout(4,3));
		for(int i = 1 ; i < 10 ; i++){
			numberPanel.add(button[i]);
		}
		numberPanel.add(button[0]);

		commandPanel = new Panel();
		commandPanel.setLayout(new GridLayout(4,1));
		commandPanel.add(clear);
		commandPanel.add(plus);
		commandPanel.add(minus);
		commandPanel.add(equal);
		commandPanel.add(minority);
		commandPanel.add(log);
		commandPanel.add(pow);

		setLayout(new BorderLayout());
		add("North",text);
		add("Center",numberPanel);
		add("East",commandPanel);
		pack();

		commandPanel.addKeyListener(this);
		commandPanel.requestFocus();
		

	}


	public void actionPerformed(ActionEvent event)
	{
		Object source = event.getSource();
		if(source == clear){
			// クリアの場合
			initBuffer();
			initOperator();
			showBuffer();
		} else if (source == plus){
			// プラスの場合
			calculate();
			setOperator("plus");
			append = false;
		} else if (source == minus){
			// マイナスの場合
			calculate();
			setOperator("minus");
			append = false;
		} else if (source == equal){
			// イコールの場合
			calculate();
			append = false;
			initOperator();
		} else if (source == log){
			// ログの場合
			calculate();
			setOperator("log");
			append = false;
		} else if (source == pow){
			// 階乗の場合
			calculate();
			setOperator("pow");
			append = false;
		} else if (source == minority){
			//少数点
			if(minorityState != MINORITY_STATE.NONE){
				return;
			}
			if(append){
				buffer += event.getActionCommand();
			} else {
				buffer = "0" + event.getActionCommand();
			}
			append = true;
			minorityState = MINORITY_STATE.PUTTED;
			showBuffer();
		} else {
			// 0 〜 9の数字の場合
			if(append){
				buffer = buffer + event.getActionCommand();
			} else {
				buffer = event.getActionCommand();
			}
			append = true;
			showBuffer();
		}
		commandPanel.requestFocus();
	}

	void showBuffer()
	{
		if(minorityState == MINORITY_STATE.NONE){
			buffer = Integer.toString(Integer.parseInt(buffer));
			text.setText(buffer);						
		}else if(minorityState == MINORITY_STATE.PUTTED){
			text.setText(buffer);
			minorityState = MINORITY_STATE.EXIST;
		}else{
			// 文字列として010などのように0が先頭についてしまう時，
			// これを一度Integerに変換すると10と認識される．
			// さらにこの10を文字列に変換しバッファにしまう．
			buffer = Double.toString(Double.parseDouble(buffer));
			text.setText(buffer);
		}
	}

	void initBuffer()
	{
		minorityState = MINORITY_STATE.NONE;
		buffer = null;
		buffer = new String("0");
		keyBuffer = null;
		keyBuffer = new String("0");
	}

	void initOperator()
	{
		operator = null;
		operator = new String("none");
	}

	void setOperator(String theOperator)
	{
		operator = theOperator;
	}

	void calculate()
	{
		minorityState = MINORITY_STATE.EXIST;
		if(operator.equals("plus")){
			result = result + Double.parseDouble(buffer);
			buffer = Double.toString(result);
			showBuffer();
		} else if(operator.equals("minus")){
			result = result - Double.parseDouble(buffer);
			buffer = Double.toString(result);
			showBuffer();
		} else if(operator.equals("pow")){
			result = Math.pow(result,Double.parseDouble(buffer));
			buffer = Double.toString(result);
			showBuffer();
		} else if(operator.equals("log")){
			result = Math.log(result);
			buffer = Double.toString(result);
			showBuffer();
		} else {
			result = Double.parseDouble(buffer);
		}
		minorityState = MINORITY_STATE.NONE;
	}

	class ClosingWindowListener extends WindowAdapter
	{
		public void windowClosing(WindowEvent e)
		{
			System.exit(0);
		}
	}

	public void keyTyped(KeyEvent e) 
	{
		char c = e.getKeyChar();
		System.out.println("[keyTyped] "+c);
		if(isNumber(String.valueOf(c))){
			keyBuffer = null;
			keyBuffer = new String("0");
			if(append){
				buffer += c;
			} else {
				buffer = String.valueOf(c);
			}
			append = true;
			showBuffer();
		} else if (c == '.'){
			keyBuffer = null;
			keyBuffer = new String("0");
			//少数点
			if(minorityState != MINORITY_STATE.NONE){
				return;
			}
			if(append){
				buffer += c;
			} else {
				buffer = "0" + c;
			}
			append = true;
			minorityState = MINORITY_STATE.PUTTED;
			showBuffer();
		}else if(append && c == '='){
			// イコールの場合
			calculate();
			append = false;
			initOperator();
		}else if(append && c == '+'){
			keyBuffer = null;
			keyBuffer = new String("0");
			// プラスの場合
			calculate();
			setOperator("plus");
			append = false;
		}else if(append && c == '-'){
			keyBuffer = null;
			keyBuffer = new String("0");
			// マイナスの場合
			calculate();
			setOperator("minus");
			append = false;
		}else if(c == 'C'){
			// クリアの場合
			initBuffer();
			initOperator();
			showBuffer();
		}else if(append && c == 'l'){
			keyBuffer = String.valueOf(c);
		}else if(keyBuffer.equals("l") && c == 'o'){
			keyBuffer += c;
		}else if(keyBuffer.equals("lo") && c == 'g'){
			// ログの場合
			calculate();
			setOperator("log");
			append = false;
		}else if(append && c == 'p'){
			keyBuffer = String.valueOf(c);
		}else if(keyBuffer.equals("p") && c == 'o'){
			keyBuffer += c;
		}else if(keyBuffer.equals("po") && c == 'w'){
			// 階乗の場合
			calculate();
			setOperator("pow");
			append = false;		
		}else if(!append && c == 'e'){
			keyBuffer = null;
			keyBuffer = new String("0");
			buffer = Double.toString(Math.E);
			append = true;
			minorityState = MINORITY_STATE.EXIST;
			showBuffer();
		}else{
			keyBuffer = null;
			keyBuffer = new String("0");
			text.setText("意味がわかりません");
		}
	}

	public void keyPressed(KeyEvent e) 
	{
	}

	public void keyReleased(KeyEvent e) 
	{
	}

	public boolean isNumber(String val) 
	{
		try {
			Integer.parseInt(val);
			return true;
		} catch (NumberFormatException nfex) {
			return false;
		}
	}

	
}

