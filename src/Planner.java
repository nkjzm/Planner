import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

public class Planner {
 static ArrayList<Operator> operators;
 public ArrayList<String> preInstantiations;
 public ArrayList<String> preGoals;
 public ArrayList<String> allGoals ;
 static Random rand;
 public ArrayList<Operator> plan;
 public ArrayList<String> ProgressResult;

 public int count;
 public int max = 100;

 public static void main(String argv[]){
	 Planner aPlanner = new Planner();
	 aPlanner.initOperators();
	 aPlanner.staticPrioritySet();
	 ArrayList<String> goalList     = aPlanner.initGoalList();
	 ArrayList<String> initialState = aPlanner.initInitialState();
	 aPlanner.sortGoals(goalList);
	 System.out.println("all"+aPlanner.allGoals);
	 aPlanner.start(goalList,initialState);
 }

 Planner(){
  rand = new Random();
  preInstantiations = new ArrayList<String>();
  preGoals = new ArrayList<String>();
  allGoals = new ArrayList<String>();
 }

 /*
  * ゴールリストをプランニングしやすい順番にならべかえるメソッド
  *
  * @param	ゴールリストを表すArrayList<String> goalList
  */
 public void sortGoals(ArrayList<String> goalList){
	 /*
	  * step 1
	  *
	  *それぞれのゴール要素をADDリストに持つオペレータを1つずつ決定する
	  */
	 ArrayList<Operator> theOperators = new ArrayList<Operator>();
	 HashMap<Operator,String> operatorsMap = new HashMap<Operator,String>();
	 for(int i = 0; i < goalList.size(); ++i){
		 ArrayList<Operator> conflict = new ArrayList<Operator>();
		 for(int j = 0; j < operators.size(); ++j){
			 ArrayList<String> theAddList = operators.get(j).getAddList();
			 for(int k = 0; k <theAddList.size(); ++k){
				 HashMap<String,String> aBinding = new HashMap<String,String>();
				 if((new Unifier()).unify(theAddList.get(k), goalList.get(i), aBinding)){
					 Operator instanced = operators.get(j).instantiate(aBinding);
					 conflict.add(instanced);

					 //ついでに動的な優先度決定指標となる1階層展開されたゴール要素を保存しておく
					 allGoals.addAll(instanced.getIfList());
				 }
			 }
		 }
		 //適応できるオペレータがない場合プランニング失敗
		 if(conflict == null)	return;

		 //オペレータの競合が起こった場合静的な優先度に基づいてオペレータを1つに絞る
		 Operator anOperator = null;
		 int maxPriority = -1;
		 for(int j = 0; j < conflict.size(); ++j){
			 if(j == 0){
				 anOperator = conflict.get(j);
			 } else if(maxPriority < conflict.get(j).getPriority()){
				 maxPriority = conflict.get(j).getPriority();
				 anOperator = conflict.get(j);
			 }
		 }
		 theOperators.add(anOperator);
		 //ゴール要素とオペレータの対応関係を保存
		 operatorsMap.put(anOperator,goalList.get(i));
	 }

	 /*
	  * step 2
	  *
	  *オペレータのペアに対して一方がもう一方にどれだけ貢献する可能性があるかの尺度（以下"貢献度"と呼ぶ）を
	  *すべてのペアに対して計算し、それらを行列として保持する
	  *
	  * ここで
	  * オペレータA(opA)のオペレータB(opB)に対する貢献度　=
	  * （opAのADDリストに含まれるopBのIFリストの要素数　+ opBのDELETEリストに含まれるopAのIFリストの要素数） (>= 0)
	  * としている
	  */
	 int size = operatorsMap.size();
	 Integer[][] contributionMat = new Integer[size][size];
	 //rowContributed は行列の各行の貢献度の和（各オペレータの非貢献度の和と考えられる）を要素に持つリスト
	 Integer[] rowContributed = new Integer[size];
	 for(int i = 0; i < size; ++i){
		 rowContributed[i] = 0;
		 for(int j = 0; j < size; ++j){
			 contributionMat[i][j] = 0;
		 }
	 }
	 for(int i = 0; i < size; ++i){
		 Operator columnOp = theOperators.get(i);
		 ArrayList<String> columnAddList = columnOp.getAddList();
		 ArrayList<String> columnDeleteList = columnOp.getDeleteList();
		 for(int j = 0; j < size; ++j){
			 Operator rowOp = theOperators.get(j);
			 ArrayList<String> rowIfList = rowOp.getIfList();
			 if(i != j){
				 for(int k = 0; k < rowIfList.size(); ++k){
					 for(int l = 0; l < columnAddList.size(); ++l){
						 if(rowIfList.get(k).equals(columnAddList.get(l))){
						// if((new Unifier()).unify(rowIfList.get(k), columnAddList.get(l))){
							 contributionMat[i][j]++;
							 rowContributed[j]++;
						 }
					 }
					 for(int l = 0; l < columnDeleteList.size(); ++l){
						 if(rowIfList.get(k).equals(columnDeleteList.get(l))){
						 //if((new Unifier()).unify(rowIfList.get(k), columnDeleteList.get(l))){
							 contributionMat[j][i]++;
							 rowContributed[i]++;
						 }
					 }
				 }
			 }
		 }
	 }

	 //貢献度行列表示
	 for(int i =0; i<size; ++i){
		 for(int j =0;j<size;++j){
			 System.out.print(contributionMat[i][j]+" ");
		 }
		 System.out.println("");
	 }

	 /*
	  * すべてのオペレータの順序付けが完了するまでstep3,step4を繰り返す
	  */
	 ArrayList<Integer> indexies = new ArrayList<Integer>();
	 for(int i = 0; i < size; ++i){
		 indexies.add(i);
	 }
	 int d = 0;
	 Operator[] sortedOp = new Operator[size];
	 while(!indexies.isEmpty()){
	 /*
	  * step 3
	  * 非貢献度が最小のオペレータを調べる
	  * 複数ある場合はすべて保持
	  */
	 Integer[] index = new Integer[size];
	 int c = 0;
	 int min = 0;
	 for(int j = 0; j < indexies.size(); ++j){
		 if(j == 0){
			 min = rowContributed[indexies.get(j)];
			 index[c++] = indexies.get(j);
		 } else if(rowContributed[indexies.get(j)] < min){
			 min = rowContributed[indexies.get(j)];
			 c = 0;
			 index[c++] = indexies.get(j);
		 } else if(rowContributed[indexies.get(j)] == min){
			 index[c++] = indexies.get(j);
		 }
	 }

	 /*
	  * step 4
	  * step 3で得られたオペレータを初期値として、
	  * 貢献度を参考に効率的と考えられる順序でオペレータを取り出して行く
	  */
	 boolean increase = true;
	 while(increase){
		 increase = false;
		 //抽出したオペレータから順に保存
		 for(int i = 0; i < c; ++i){
			 System.out.println(d+theOperators.get(index[i]).name);
			 sortedOp[d++] = theOperators.get(index[i]);
			 indexies.remove(index[i]);
		 }
		 //次に貢献されているオペレータを抽出
		 int c1 = 0;
		 Integer[] index1 = new Integer[size];
		 for(int i = 0; i < c; ++i){
			 for(int j = 0; j < size; ++j){
				 if(contributionMat[index[i]][j] > 0 && indexies.contains(j)){
					 //他のオペレータからも貢献されている場合、それらすべてのオペレータが取り出されてからこのオペレータを取り出す
					 boolean thisIsMax = true;
					 for(int k = 0; k < size; ++k){
						 if(k != index[i] && contributionMat[k][j] >= contributionMat[index[i]][j]){
							 contributionMat[index[i]][j] = 0;
							 thisIsMax = false;
							 break;
						 }
					 }
					 if(thisIsMax){
						 index1[c1++] = j;
						 increase = true;
					 }
				 }
			 }
		 }
		 c = c1;
		 index = index1;
	 }

	 //書き換えられた貢献度行列を表示
	 for(int i = 0; i < size; ++i){
		 for(int j = 0; j < size; ++j)
		 System.out.print(contributionMat[i][j]+" ");
	 System.out.println("");
	 }

	 /*
	 //残ったオペレータを取り出す
	 for(int i : indexies){
		 sortedOp[d++] = theOperators.get(i);
	 }
	 */
	 }

	 //終了
	 //所与のゴールリストを並べ替える
	 goalList.clear();
	 for(int i = 0; i < size; ++i){
		 goalList.add(operatorsMap.get(sortedOp[i]));
		 //System.out.println(sortedOp[i].name);
	 }
 }

 public void start(ArrayList<String> goalList,ArrayList<String> initialState){
  HashMap<String,String> theBinding = new HashMap<String,String>();
  plan = new ArrayList<Operator>();
  ProgressResult = new ArrayList<String>();

  if(planning(goalList,initialState,theBinding)){

	  System.out.println("SUCCESS !!");
	  System.out.println("***** This is a plan! *****");
	  for(int i = 0 ; i < plan.size() ; i++){
		  Operator op = (Operator)plan.get(i);
		  ProgressResult.add((op.instantiate(theBinding)).name);
	  }
	  	for(int i = 0; i<ProgressResult.size(); i++){
	  		System.out.println(ProgressResult.get(i));
	  	}
  }else{
	  System.out.println("FALSE !!");
  }
  }

 /*
  * ゴールリストに対するプランニングを行うメソッド
  *
  * @param	ゴールリストを表す theGoalList,
  * 		ワーキングメモリを表す theCurrentState,
  * 		変数束縛情報を表す theBinding
  * @return theGoalListに対するプランニングが成功すれば true ,失敗すれば false
  */
 public boolean planning(ArrayList<String> theGoalList,
                          ArrayList<String> theCurrentState,
                          HashMap<String,String> theBinding)
 {
	 //ループの上限が設定してある
	 if(count++ > max) return false;

	 System.out.println("*** GOALS ***" + theGoalList);

	 //ゴールリストがワーキングメモリにすべて含まれている場合のみ成功とみなす
	 if(theCurrentState.containsAll(theGoalList)) {
		 preGoals.clear();
		 System.out.println("成功");
		 return true;
	 }

	 if(theGoalList.size() == 1){
		 String aGoal = (String)theGoalList.get(0);
		 theGoalList.remove(0);

		 //同じゴールを設定しないことにする
		 if(preGoals.contains(aGoal)){
			 System.out.println(aGoal + " contains "+ preGoals);
			 return false;
		 }

		 preGoals.add(aGoal);
		 System.out.println(aGoal + preGoals);
		 if(planningAGoal(aGoal,theGoalList,theCurrentState,theBinding,0) != -1){
			 //一度成功した場合過去のゴールの情報を捨てる
			 preGoals.clear();
			 return true;
		 } else {
			  return false;
		 }

	 } else {
		 String aGoal = (String)theGoalList.get(0);
		 theGoalList.remove(0);

		 int cPoint = 0;
		 while(cPoint < theCurrentState.size() + operators.size()){
		 //同じゴールを設定しないことにする
		 if(preGoals.contains(aGoal)) {
			 System.out.println(aGoal + " contains "+ preGoals);
			  return false;
		 }

		// System.out.println(aGoal + preGoals);
		 preGoals.add(aGoal);
		 // Store original binding
		 HashMap<String,String> orgBinding = new HashMap<String,String>();
		 for(Iterator e = theBinding.keySet().iterator() ; e.hasNext();){
			 String key = (String)e.next();
			 String value = (String)theBinding.get(key);
			  orgBinding.put(key,value);
		 }
		 ArrayList<String> orgState = new ArrayList<String>();
		 for(int i = 0; i < theCurrentState.size() ; i++){
			 orgState.add(theCurrentState.get(i));
		 }

		 int tmpPoint = planningAGoal(aGoal,theGoalList,theCurrentState,theBinding,cPoint);

		 if(tmpPoint != -1){
			 //ワーキングメモリにゴールリストが含まれている場合のみを成功とみなす
			 if(theCurrentState.containsAll(theGoalList)){
				 //探索木を折り返す
				 preGoals.clear();
				return true;
			 } else {
				 //他のゴール条件にオペレータを適応する余地が残っている
				 preGoals.remove(aGoal);

				 /*
				  * ここがデバックのポイント
				  */

				 //全ゴールに対して変数束縛を行う
				 /*
				  * 先ほどプランニングに用いたゴールを再びリストの尾に戻す
				  * これはのちのプランニングでワーキングメモリが書きかえられる可能性があるため、
				  * 真の成功条件で終了するまで保持し続ける必要があるから
				  */
				 theGoalList.add(aGoal);
				 for(int i = 0; i < theGoalList.size(); ++i){
					 String st = theGoalList.get(0);
					 theGoalList.remove(0);
					 st = instantiateString(st,theBinding);
					 theGoalList.add(st);
				 }

				 //再帰的にプランニングを続行
				 if(planning(theGoalList,theCurrentState,theBinding)){
					 preGoals.clear();
					 return true;
				 } else {
					 //return false;
					 cPoint = tmpPoint;

					 theBinding.clear();
					 for(Iterator e=orgBinding.keySet().iterator();e.hasNext();){
						 String key = (String)e.next();
						 String value = (String)orgBinding.get(key);
						 theBinding.put(key,value);
					 }
					 theCurrentState.clear();
					 for(int i = 0 ; i < orgState.size() ; i++){
						 theCurrentState.add(orgState.get(i));
					 }
				 }
			 }
		 } else {
			 theBinding.clear();
			 for(Iterator e=orgBinding.keySet().iterator();e.hasNext();){
				 String key = (String)e.next();
				 String value = (String)orgBinding.get(key);
				 theBinding.put(key,value);
			 }
			 theCurrentState.clear();
			 for(int i = 0 ; i < orgState.size() ; i++){
				 theCurrentState.add(orgState.get(i));
			 }
			 return false;
		 }
		 }
		 return false;
	 }
 }

 /*
  * ゴールリストの1要素に対してプランニングを行うメソッド
  *
  * @param	現ゴールリストの1つ目の要素 theGoal,その他の要素のリスト otherGoals,
  * 		現在の変数束縛情報 theBinding,Choice Pointを表す cPoint
  * @return	（ワーキングメモリ＋競合解消済みのオペレータリスト）　を1つのリストと考えた場合の、
  * 		theGoalとマッチングが成功したワーキングメモリの要素のindex+1、または、
  * 		theGoalとマッチング可能な要素を持つADDリストを持ち、さらに再帰的にプランニングが成功するオペレータの要素のindex+1を返す。
  * 		また失敗した場合は -1 を返す
  *
  *（コメント）
  *	Choice Point を用いたバックトラックの機能について、
  *	このメソッドが呼び出されるたびに競合解消を行うため効率が悪くなってしまった。
  *	しかし、このバックトラック機能がなくてもある程度のプランニングは成功する機能を多く実装しているので、
  *	ここにおいて無駄に実行される計算量はあまり多くはならない
  */
 public int planningAGoal(
		 String theGoal,
		 ArrayList<String> otherGoals,
		 ArrayList<String> theCurrentState,
		 HashMap<String,String> theBinding,
		 int cPoint)
 {
	// System.out.println("preGoal :"+preGoals);
	 System.out.println("preIns :"+preInstantiations);
	 System.out.println("**"+theGoal);
	 System.out.println(preGoals);
	 int size = theCurrentState.size();
	 //ワーキングメモリとのマッチング
	 if(cPoint < theCurrentState.size()){
	 for(int i =  cPoint; i < size ; i++){
		 String aState = (String)theCurrentState.get(i);
		 if((new Unifier()).unify(theGoal,aState,theBinding)){
			 System.out.println(theGoal+" <=> "+aState);
			 System.out.println(theBinding);
			 return i+1;
 	  }
	 }
	 }

	 HashMap<Operator,HashMap<String,String>> conflictSet = new HashMap<Operator,HashMap<String,String>>();
	 // 現在のCurrent state, Binding, planをbackup
	 HashMap<String,String> orgBinding = new HashMap<String,String>();
	 for(Iterator e = theBinding.keySet().iterator() ; e.hasNext();){
		 String key = (String)e.next();
		 String value = (String)theBinding.get(key);
		 orgBinding.put(key,value);
	 }
	 ArrayList<String> orgState = new ArrayList<String>();
	 for(int j = 0; j < theCurrentState.size() ; j++){
		 orgState.add(theCurrentState.get(j));
	 }
	 ArrayList<Operator> orgPlan = new ArrayList<Operator>();
	 for(int j = 0; j < plan.size() ; j++){
		 orgPlan.add(plan.get(j));
	 }

	 //競合集合を見つける
	 for(int i = 0 ; i < operators.size() ; i++){
		 Operator anOperator = rename((Operator)operators.get(i));
		 ArrayList<String> addList = anOperator.getAddList();

		 for(int j = 0 ; j < addList.size() ; j++){
			 HashMap<String,String> tmpBinding = (HashMap<String, String>) theBinding.clone();
			 if((new Unifier()).unify(theGoal,
					 (String)addList.get(j),
					 tmpBinding)){
				 conflictSet.put(anOperator,tmpBinding);
			 }
		   }
	 }
	 if(conflictSet.size() == 0) return -1;

	 ArrayList<String> theGoals = new ArrayList<String>();
	 theGoals.add(theGoal);
	 theGoals.addAll(otherGoals);

	 //競合解消
	 ArrayList<HashMap> newConflictSetList =
		 resolveConflict(theCurrentState,theGoals,conflictSet);

	 if(cPoint < theCurrentState.size()+newConflictSetList.size()){

	 //優先度順にオペレータを試す
	 for(int i = cPoint ; i < newConflictSetList.size(); ++i){
		 HashMap<Operator,HashMap<String,String>> newConflictSet =
			 (HashMap<Operator,HashMap<String,String>>)newConflictSetList.get(i);
		 for(Iterator e = newConflictSet.keySet().iterator() ; e.hasNext();){
			 Operator newOperator = (Operator)e.next();
			 HashMap<String,String> newBinding = newConflictSet.get(newOperator);

			 newOperator = newOperator.instantiate(newBinding);
			 ArrayList<String> newGoals = newOperator.getIfList();
			// allGoals.remove(theGoal);
			// allGoals.addAll(newGoals);

			 //使用したインスタンシエーションを保存
			 preInstantiations.add(newOperator.name);

			 System.out.println("newOp:"+newOperator.name);
			 System.out.println("newBind:"+newBinding);
			 if(planning(newGoals,theCurrentState,newBinding)){
				 newOperator = newOperator.instantiate(newBinding);
				 System.out.println(newOperator.name);
				 System.out.println(newBinding.toString()+theBinding.toString());
				 //preInstantiations.add(newOperator.name);
				 plan.add(newOperator);
				 theCurrentState =
					 newOperator.applyState(theCurrentState);
				 //theBinding.clear();
				 //theBinding = newBinding;

				 //新しい変数束縛情報をこのメソッドの呼び出し元に反映させるための処理
				 copyMap(newBinding,theBinding);

				 System.out.println(theBinding);
				 return i+theCurrentState.size()+1;
			 } else {
				 // 失敗したら元に戻す．
				 //preInstantiations,preGoalsも復元
				 preInstantiations.remove(newOperator.name);
				 preGoals.remove(theGoal);
				// allGoals.removeAll(newGoals);
				// allGoals.add(theGoal);

				 System.out.println("失敗 :"+newOperator.name+"¥nGoal :"+theGoal);
				 theBinding.clear();
				 for(Iterator e1=orgBinding.keySet().iterator();e1.hasNext();){
					 String key = (String)e1.next();
					 String value = (String)orgBinding.get(key);
					 theBinding.put(key,value);
				 }
				 theCurrentState.clear();
				 for(int k = 0 ; k < orgState.size() ; k++){
					 theCurrentState.add(orgState.get(k));
				 }
				 plan.clear();
				 for(int k = 0 ; k < orgPlan.size() ; k++){
					 plan.add(orgPlan.get(k));
				 }
			 }
		 }
	 }
	 }
	 return -1;
 }

 /*
  * map1 copy to map2
  */
 public void copyMap(HashMap map1 ,HashMap map2){
	 map2.clear();
	 map2.putAll(map1);
 }

 /*
  * 競合解消を行うメソッド
  *
  * @param	現在のワーキングメモリを表す theCurrentState, ゴールリストを表す theGoalList 、競合集合を表す conflictSet
  * @return 優先度順にソートされた<インスタンシエーション、変数束縛情報>のエントリを持つ ArrayList
  */
 	public ArrayList<HashMap> resolveConflict(
 			ArrayList<String> theCurrentState,
 			ArrayList<String> theGoalList,
 			HashMap<Operator,HashMap<String,String>> conflictSet){
 		ArrayList<HashMap> result = new ArrayList<HashMap>();
 		HashMap<Operator,HashMap<String,String>> dynamic = dynamicPrioritySet(theCurrentState,theGoalList,conflictSet);
 		Vector<Operator> opList = new Vector<Operator>();
 		for(Iterator ite = dynamic.keySet().iterator(); ite.hasNext();){
 			Operator instantiation = (Operator)ite.next();
 			int i = 0;
 			while(true){
 				//動的な優先度でソートする
 				if(opList.size() <= i){
 					opList.insertElementAt(instantiation, i);
 					break;
 				}
 				if(instantiation.getPriority() > opList.elementAt(i).getPriority()){
 					opList.insertElementAt(instantiation, i);
 					break;
 				} else if(instantiation.getPriority() == opList.elementAt(i).getPriority()){
 					//動的な優先度が同じ場合、静的な優先度でソートする
 					Operator op1 = null;
 					Operator op2 = null;
 					for(int j = 0; j< operators.size(); ++j){
 						if((new Unifier()).unify(operators.get(j).name,instantiation.name)){
 							op1 = operators.get(j);
 						}
 						if((new Unifier()).unify(operators.get(j).name,opList.elementAt(i).name)){
 							op2 = operators.get(j);
					 }
 					}
 					if(op1.getPriority() >= op2.getPriority()){
 						opList.insertElementAt(instantiation, i);
 						break;
 					}
 				}
 				++i;
 			}
 		}
 		for(int i = 0; i < opList.size(); ++i){
 			HashMap tmpMap = new HashMap();
 			//System.out.println(opList.get(i).name +" - "+ opList.get(i).getPriority());
 			tmpMap.put(opList.get(i),dynamic.get(opList.get(i)));
 			result.add(tmpMap);
 		}
 		return result;
 	}

 /*
  * オペレータごとに静的な優先度を設定する
  */
 public void staticPrioritySet(){
	 //静的な優先度 = オペレータのIFリスト長 - ADDリスト長 - DELETEリスト長
	 for(int i = 0; i < operators.size(); ++i){
		 Operator anOperator = operators.get(i);
		 int priority = (anOperator).getIfList().size()
		 	- (anOperator).getAddList().size()
		 	- (anOperator).getDeleteList().size();
		 operators.get(i).setPriority(priority);
	 }
 }

 /*
  * インスタンシエーションに動的な優先度を設定する
  *
  * @param	現在のワーキングメモリ theCurrentState ,
  * 		現在のゴール状態のリスト theGoalList ,
  * 		競合集合中のオペレータのリストとそれぞれの束縛情報のエントリをもつ theConflictSet
  * @return	インスタンシエーションとそれぞれの束縛情報のエントリを持つマップ
  */
 public HashMap<Operator,HashMap<String,String>> dynamicPrioritySet(
		 ArrayList<String> theCurrentState,
		 ArrayList<String> theGoalList,
		 HashMap<Operator,HashMap<String,String>> theConflictSet){

	 HashMap<Operator,HashMap<String,String>> result = new HashMap<Operator,HashMap<String,String>>();
//	System.out.println(allGoals);
	 for(Iterator ite = theConflictSet.keySet().iterator(); ite.hasNext();){
		 Operator anOperator = (Operator)ite.next();
		 HashMap<String,String> theBinding = (HashMap<String,String>)theConflictSet.get(anOperator);
		 Operator theInstantiation = anOperator.instantiate(theBinding);

		 //過去に使用したインスタンシエーションを削除する
		 ArrayList<String> tmpInstantiations = new ArrayList<String>();
		 for(int i = 0; i < preInstantiations.size(); ++i){
			 String instantiatedName = instantiateString(preInstantiations.get(i),theBinding);
			 tmpInstantiations.add(instantiatedName);
		 }
		 if(!tmpInstantiations.contains(theInstantiation.name)){

		System.out.println(theInstantiation.name + preInstantiations);
		 int contribution = 0;
		 ArrayList<String> theIfList = theInstantiation.getIfList();
		 ArrayList<String> theAddList = theInstantiation.getAddList();
		 ArrayList<String> theDeleteList = theInstantiation.getDeleteList();

		 //現ワーキングメモリの要素でIFリストの要素が満足できるなら、それを優先する
		 HashMap<String,String> aBindings = new HashMap<String,String>();
		 for(int i = 0; i < theCurrentState.size(); ++i){
			 for(int j = 0; j < theIfList.size(); ++j){
				 if((new Unifier()).unify(theCurrentState.get(i),theIfList.get(j),aBindings)){
					contribution++;
				 }
			 }
		 }
		 //IFリスト長の違いを無視するため、IFリスト長で割る
		 contribution /= theIfList.size();

		 //ユーザに与えられたゴール状態を1階層展開したものと、ADD、DELETEリストとのマッチングを調べる
		 aBindings.clear();
		 HashMap<String,String> bBindings = new HashMap<String,String>();
		 for(int j = 0; j < allGoals.size(); ++j){
			 for(int k = 0; k < theAddList.size(); ++k){
				 if((new Unifier()).unify(allGoals.get(j),theAddList.get(k),aBindings)){
					 //ADDリスト要素とマッチするならそれを優先
					 contribution++;
				 }
			 }
			 for(int l = 0; l < theDeleteList.size(); ++l){
				 if((new Unifier()).unify(allGoals.get(j),theDeleteList.get(l),bBindings)){
		 			//目標状態をDELETEする可能性があるオペレータの優先度を下げる
					 contribution--;
				 }
			 }
		 }

		 theInstantiation.setPriority(contribution);
		 System.out.println(theInstantiation.name + " -> " + theInstantiation.getPriority());
		 result.put(theInstantiation, theBinding);
		 }
	 }
	 return result;
 }










 //与えられた文字列の変数を指定の変数束縛情報を用いて束縛するメソッド
public String instantiateString(String thePattern, HashMap<String,String> theBinding){
     String result = new String();
     StringTokenizer st = new StringTokenizer(thePattern);
     for(int i = 0 ; i < st.countTokens();){
         String tmp = st.nextToken();
         if(var(tmp)){
		String newString = (String)theBinding.get(tmp);
		if(newString == null){
		    result = result + " " + tmp;
		} else {
		    result = result + " " + newString;
		}
         } else {
             result = result + " " + tmp;
         }
     }
     return result.trim();
 }

 public boolean var(String str1){
     // 先頭が ? なら変数
     return str1.startsWith("?");
 }

 int uniqueNum = 0;
 public Operator rename(Operator theOperator){
  Operator newOperator = theOperator.getRenamedOperator(uniqueNum);
  uniqueNum = uniqueNum + 1;
  return newOperator;
 }

 public ArrayList<String> initGoalList(){
  ArrayList<String> goalList = new ArrayList<String>();
  goalList.add("A on B");
  goalList.add("B on C");
 // goalList.add("C on D");
  /*goalList.add("D on E");
  goalList.add("E on F");
  goalList.add("clear A");
  goalList.add("A on C");
  goalList.add("B on D");
  goalList.add("C on F");
  goalList.add("clear A");
  goalList.add("F on E"); */
  return goalList;
 }

 public ArrayList<String> initInitialState(){
	 ArrayList<String> initialState = new ArrayList<String>();
	 //initialState.add("F on E");
	 //initialState.add("E on D");
	// initialState.add("D on C");
	 initialState.add("C on B");
	 initialState.add("B on A");
	 initialState.add("ontable A");
	 //initialState.add("clear A");
	// initialState.add("ontable B");
	// initialState.add("clear B");
	 //initialState.add("ontable C");
	 initialState.add("clear C");
	 //initialState.add("ontable D");
	 //initialState.add("clear D");
	 //initialState.add("ontable E");
	 //initialState.add("clear E");
	 //initialState.add("ontable F");
	 //initialState.add("clear F");

	 initialState.add("handEmpty");

  return initialState;
 }

 /*
  * ファイルからオペレータを読み込むメソッド
  * ファイルの書き方の例は
  *
  * NAME	RULE
  * IF		pattern1
  * 		patten2
  * ADD	addition
  * DELETE	remove
  *
  * @param	ファイル名
  * @ret	読み込み成功時は0,Exception時は-1,読み込み途中で失敗した場合は途中までの行数
 */
 public void initOperators(){
	  operators = new ArrayList<Operator>();

	  // OPERATOR 1
	  /// NAME
	  String name1 = new String("Place ?x on ?y");
	  /// IF
	  ArrayList<String> ifList1 = new ArrayList<String>();
	  ifList1.add(new String("clear ?y"));
	  ifList1.add(new String("holding ?x"));
	  /// ADD-LIST
	  ArrayList<String> addList1 = new ArrayList<String>();
	  addList1.add(new String("?x on ?y"));
	  addList1.add(new String("clear ?x"));
	  addList1.add(new String("handEmpty"));
	  /// DELETE-LIST
	  ArrayList<String> deleteList1 = new ArrayList<String>();
	  deleteList1.add(new String("clear ?y"));
	  deleteList1.add(new String("holding ?x"));
	  Operator operator1 =
		    new Operator(name1,ifList1,addList1,deleteList1);
		operators.add(operator1);

		// OPERATOR 2
		/// NAME
		String name2 = new String("remove ?x from on top ?y");
		/// IF
		ArrayList<String> ifList2 = new ArrayList<String>();
		ifList2.add(new String("?x on ?y"));
		ifList2.add(new String("clear ?x"));
		ifList2.add(new String("handEmpty"));
		/// ADD-LIST
		ArrayList<String> addList2 = new ArrayList<String>();
		addList2.add(new String("clear ?y"));
		addList2.add(new String("holding ?x"));
		/// DELETE-LIST
		ArrayList<String> deleteList2 = new ArrayList<String>();
		deleteList2.add(new String("?x on ?y"));
		deleteList2.add(new String("clear ?x"));
		deleteList2.add(new String("handEmpty"));
		Operator operator2 =
		    new Operator(name2,ifList2,addList2,deleteList2);
		operators.add(operator2);

		// OPERATOR 3
		/// NAME
		String name3 = new String("pick up ?x from the table");
		/// IF
		ArrayList<String> ifList3 = new ArrayList<String>();
		ifList3.add(new String("ontable ?x"));
		ifList3.add(new String("clear ?x"));
		ifList3.add(new String("handEmpty"));
		/// ADD-LIST
		ArrayList<String> addList3 = new ArrayList<String>();
		addList3.add(new String("holding ?x"));
		/// DELETE-LIST
		ArrayList<String> deleteList3 = new ArrayList<String>();
		deleteList3.add(new String("ontable ?x"));
		deleteList3.add(new String("clear ?x"));
		deleteList3.add(new String("handEmpty"));
		Operator operator3 =
		    new Operator(name3,ifList3,addList3,deleteList3);
		operators.add(operator3);

		// OPERATOR 4
		/// NAME
		String name4 = new String("put ?x down on the table");
		/// IF
		ArrayList<String> ifList4 = new ArrayList<String>();
		ifList4.add(new String("holding ?x"));
		/// ADD-LIST
		ArrayList<String> addList4 = new ArrayList<String>();
		addList4.add(new String("ontable ?x"));
		addList4.add(new String("clear ?x"));
		addList4.add(new String("handEmpty"));
		/// DELETE-LIST
		ArrayList<String> deleteList4 = new ArrayList<String>();
		deleteList4.add(new String("holding ?x"));
		Operator operator4 =
		    new Operator(name4,ifList4,addList4,deleteList4);
		operators.add(operator4);
	    }
	}

class Operator{
    String name;
    ArrayList<String> ifList;
    ArrayList<String> addList;
    ArrayList<String> deleteList;
    int priority;

    Operator(String theName,
    		ArrayList<String> theIfList,ArrayList<String> theAddList,ArrayList<String> theDeleteList){
	name       = theName;
	ifList     = theIfList;
	addList    = theAddList;
	deleteList = theDeleteList;
    }


    /*
     * このオペレータオブジェクトの優先度のセットする
     */
    public void setPriority(int p){
    	this.priority = p;
    }

    /*
     * このオペレータの優先度を返す
     */
    public int getPriority(){
    	return this.priority;
    }

    public ArrayList<String> getAddList(){
	return addList;
    }

    public ArrayList<String> getDeleteList(){
	return deleteList;
    }

    public ArrayList<String> getIfList(){
	return ifList;
    }

    public String toString(){
	String result =
	    "NAME: "+name + "¥n" +
	    "IF :"+ifList + "¥n" +
	    "ADD:"+addList + "¥n" +
	    "DELETE:"+deleteList;
	return result;
    }

    public ArrayList<String> applyState(ArrayList<String> theState){
	for(int i = 0 ; i < addList.size() ; i++){
	    theState.add(addList.get(i));
	}
	for(int i = 0 ; i < deleteList.size() ; i++){
	    theState.remove(deleteList.get(i));
	}
	return theState;
    }


    public Operator getRenamedOperator(int uniqueNum){
    	ArrayList<String> vars = new ArrayList<String>();
	// IfListの変数を集める
	for(int i = 0 ; i < ifList.size() ; i++){
	    String anIf = (String)ifList.get(i);
	    vars = getVars(anIf,vars);
	}
	// addListの変数を集める
	for(int i = 0 ; i < addList.size() ; i++){
	    String anAdd = (String)addList.get(i);
	    vars = getVars(anAdd,vars);
	}
	// deleteListの変数を集める
	for(int i = 0 ; i < deleteList.size() ; i++){
	    String aDelete = (String)deleteList.get(i);
	    vars = getVars(aDelete,vars);
	}
	Hashtable renamedVarsTable = makeRenamedVarsTable(vars,uniqueNum);

	// 新しいIfListを作る
	ArrayList<String> newIfList = new ArrayList<String>();
	for(int i = 0 ; i < ifList.size() ; i++){
	    String newAnIf =
		renameVars((String)ifList.get(i),
			   renamedVarsTable);
	    newIfList.add(newAnIf);
	}
	// 新しいaddListを作る
	ArrayList<String> newAddList = new ArrayList<String>();
	for(int i = 0 ; i < addList.size() ; i++){
	    String newAnAdd =
		renameVars((String)addList.get(i),
			   renamedVarsTable);
	    newAddList.add(newAnAdd);
	}
	// 新しいdeleteListを作る
	ArrayList<String> newDeleteList = new ArrayList<String>();
	for(int i = 0 ; i < deleteList.size() ; i++){
	    String newADelete =
		renameVars((String)deleteList.get(i),
			   renamedVarsTable);
	    newDeleteList.add(newADelete);
	}
	// 新しいnameを作る
	String newName = renameVars(name,renamedVarsTable);

	return new Operator(newName,newIfList,newAddList,newDeleteList);
    }

    public ArrayList<String> getVars(String thePattern,ArrayList<String> vars){
	StringTokenizer st = new StringTokenizer(thePattern);
	for(int i = 0 ; i < st.countTokens();){
	    String tmp = st.nextToken();
	    if(var(tmp)){
		vars.add(tmp);
	    }
	}
	return vars;
    }

    public Hashtable makeRenamedVarsTable(ArrayList<String> vars,int uniqueNum){
	Hashtable result = new Hashtable();
	for(int i = 0 ; i < vars.size() ; i++){
	    String newVar =
		(String)vars.get(i) + uniqueNum;
	    result.put((String)vars.get(i),newVar);
	}
	return result;
    }

    public String renameVars(String thePattern,
			      Hashtable renamedVarsTable){
	String result = new String();
	StringTokenizer st = new StringTokenizer(thePattern);
	for(int i = 0 ; i < st.countTokens();){
	    String tmp = st.nextToken();
	    if(var(tmp)){
		result = result + " " +
		    (String)renamedVarsTable.get(tmp);
	    } else {
		result = result + " " + tmp;
	    }
	}
	return result.trim();
    }


    public Operator instantiate(HashMap<String,String> theBinding){
	// name を具体化
	String newName =
	    instantiateString(name,theBinding);
	// ifList    を具体化
	ArrayList<String> newIfList = new ArrayList<String>();
	for(int i = 0 ; i < ifList.size() ; i++){
	    String newIf =
		instantiateString((String)ifList.get(i),theBinding);
	    newIfList.add(newIf);
	}
	// addList   を具体化
	ArrayList<String> newAddList = new ArrayList<String>();
	for(int i = 0 ; i < addList.size() ; i++){
	    String newAdd =
		instantiateString((String)addList.get(i),theBinding);
	    newAddList.add(newAdd);
	}
	// deleteListを具体化
	ArrayList<String> newDeleteList = new ArrayList<String>();
	for(int i = 0 ; i < deleteList.size() ; i++){
	    String newDelete =
		instantiateString((String)deleteList.get(i),theBinding);
	    newDeleteList.add(newDelete);
	}
	return new Operator(newName,newIfList,newAddList,newDeleteList);
    }

    public String instantiateString(String thePattern, HashMap<String,String> theBinding){
        String result = new String();
        StringTokenizer st = new StringTokenizer(thePattern);
        for(int i = 0 ; i < st.countTokens();){
            String tmp = st.nextToken();
            if(var(tmp)){
		String newString = (String)theBinding.get(tmp);
		if(newString == null){
		    result = result + " " + tmp;
		} else {
		    result = result + " " + newString;
		}
            } else {
                result = result + " " + tmp;
            }
        }
        return result.trim();
    }

    public boolean var(String str1){
        // 先頭が ? なら変数
        return str1.startsWith("?");
    }
}
class Unifier {
    StringTokenizer st1;
    String buffer1[];
    StringTokenizer st2;
    String buffer2[];
    HashMap<String,String> vars;

    Unifier(){
	vars = new HashMap<String,String>();
    }

    public boolean unify(String string1,String string2,HashMap<String,String> theBindings){
	HashMap<String,String> orgBindings = new HashMap<String,String>();
	for(Iterator<String> i = theBindings.keySet().iterator(); i.hasNext();){
	    String key = i.next();
	    String value = theBindings.get(key);
	    orgBindings.put(key,value);
	}
	this.vars = theBindings; ////これがポインタ代入なら理解できる
	if(unify(string1,string2)){
	    ////更新しなくていいのか？
		return true;
	} else {
	    // 失敗したら元に戻す．
	    theBindings.clear();
	    for(Iterator<String> i = orgBindings.keySet().iterator(); i.hasNext();){
		String key = i.next();
		String value = orgBindings.get(key);
		theBindings.put(key,value);
	    }
	    return false;
	}
    }

    public boolean unify(String string1,String string2){
	// 同じなら成功
	if(string1.equals(string2)) return true;

	// 各々トークンに分ける
	st1 = new StringTokenizer(string1);
	st2 = new StringTokenizer(string2);

	// 数が異なったら失敗
	if(st1.countTokens() != st2.countTokens()) return false;

	// 定数同士
	int length = st1.countTokens();
	buffer1 = new String[length];
	buffer2 = new String[length];
	for(int i = 0 ; i < length; i++){
	    buffer1[i] = st1.nextToken();
	    buffer2[i] = st2.nextToken();
	}

	// 初期値としてバインディングが与えられていたら
	if(this.vars.size() != 0){
	    for(Iterator<String> i = vars.keySet().iterator(); i.hasNext();){
		String key = i.next();
		String value = vars.get(key);
		replaceBuffer(key,value);
	    }
	}

	for(int i = 0 ; i < length ; i++){
	    if(!tokenMatching(buffer1[i],buffer2[i])){
		return false;
	    }
	}

	return true;
    }

    boolean tokenMatching(String token1,String token2){
	if(token1.equals(token2)) return true;
	if( var(token1) && !var(token2)) return varMatching(token1,token2);
	if(!var(token1) &&  var(token2)) return varMatching(token2,token1);
	if( var(token1) &&  var(token2)) return varMatching(token1,token2);
	return false;
    }

    boolean varMatching(String vartoken,String token){
	if(vars.containsKey(vartoken)){
	    if(token.equals(vars.get(vartoken))){
		return true;
	    } else {
		return false;
	    }
	} else {
	    replaceBuffer(vartoken,token);
	    if(vars.containsValue(vartoken)){
		replaceBindings(vartoken,token);
	    }
	    vars.put(vartoken,token);
	}
	return true;
    }

    void replaceBuffer(String preString,String postString){
	for(int i = 0 ; i < buffer1.length ; i++){
	    if(preString.equals(buffer1[i])){
		buffer1[i] = postString;
	    }
	    if(preString.equals(buffer2[i])){
		buffer2[i] = postString;
	    }
	}
    }

    void replaceBindings(String preString,String postString){
	for(Iterator<String> i = vars.keySet().iterator(); i.hasNext();){
	    String key = i.next();
	    if(preString.equals(vars.get(key))){
		vars.put(key,postString);
	    }
	}
    }

    boolean var(String str1){
	// 先頭が ? なら変数
	return str1.startsWith("?");
    }
}
