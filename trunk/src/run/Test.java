package run;

import java.util.ArrayList;
import java.util.Stack;

import core.FreeCellState;
import core.Move;
import core.Scorer;
import core.iDFS;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FreeCellState state = new FreeCellState();
		state.getInitialState("31465.txt");
		System.out.println(state.toString());
		
		//Move m = new Move(1);
		//m.execute(state, (short)-1, state.sort[2], state.sort[1], 2);
		//System.out.println(state.toString());
		//m.undo(state);
		//System.out.println(state.toString());
		
		ArrayList<Move> moves = new iDFS().solve(state, new Scorer(), 
				state.comparator());
		if (moves == null) System.out.println("No solution.");
		else {
			System.out.println("Yes.");
			for (int i = 0; i < moves.size(); ++i) {
				System.out.println("Move " + i + ": " + moves.get(i).toString());
			}
		}
	}
}