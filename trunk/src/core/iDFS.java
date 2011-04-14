package core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeMap;

public class iDFS {
	
	/** maximum number of visited states that are saved */
	final int MAX_NUMBER_OF_STATES = 200000;
	
	/** depth of DFS, visiting */
	final int DEPTH = 6;
	
	/** Current state of Visiting */ 
	private FreeCellState currentState;
	
	/** Previous states that have been visited */
	private TreeMap<short[], Integer> prev;
	
	/**
	 * Next states to be visited are inserted into priority queue 
	 * according to their evaluation in increasing order.
	 */
	private TreeMap<Integer, FreeCellState> pQueue;
	
	/** last trace of state */
	private Trace lastTrace = null;
	
	/** 
	 * contains all moves of each visit.
	 * Finally, contains all moves of result.
	 */
	private Stack<Move> moveStack; 
	
	/** used to evaluate the score of a state */
	private Scorer scorer;
	
	/** number of states (may be not unique) that have been visited */
	int count = 0;
	
	@SuppressWarnings("unchecked")
	private boolean visit(final int stateID, int depth) {
		// current state is an end-state.
		if (currentState.hasWon()) {
			return true;	// visit successfully
		}

		if (depth > DEPTH) {
			
			// clear memory
			if (prev.size() > MAX_NUMBER_OF_STATES) {
				prev.clear();
				System.gc();
			}
			
			// insert current state into memory
			prev.put((short[])currentState.key(), count);
			
			int score = scorer.eval(currentState);
			FreeCellState aCopy = currentState.clone();
			// store trace of this state
			aCopy.store(new Trace((Stack<Move>)moveStack.clone(), lastTrace));
			// insert state into queue
			pQueue.put(score, aCopy);
			return false;
		}

		// get all available moves
		ArrayList<Move> listValidMoves = currentState.validMoves();
		
		Logger.write(System.out, "iDFS.visit(): number of valid moves = " 
				+ listValidMoves.size() + "\n");
		
		// try with each move
		for (int i = 0; i < listValidMoves.size(); ++i) {
			Move move = listValidMoves.get(i);
			/*
			System.out.println("===================================" + move.type );
			if(move.type == 5) {
				System.out.println("freecell to column card: " + move.card + ", column: " + move.toColumn);
				for(int tao = 0; tao < 4; tao++) {
					System.out.print(currentState.freecell[tao] + " ");
				}
				System.out.println();
			}
			System.out.println(currentState);
			*/
			move.execute(currentState);
			/*
			System.out.println(currentState);
			System.out.println("===================================" );
			*/
			short[] key = (short[])currentState.key();
			Integer exist = prev.get(key);
			if (exist == null) {
				++count;
				
				if (prev.size() > MAX_NUMBER_OF_STATES) {
					prev.clear();
					System.gc();
				}

				prev.put(key, count); 
				moveStack.push(move);               
				if (visit(count, depth+1)) {
					System.out.println("tund -> success!");
					return true;
				}
				moveStack.pop();
			}
			move.undo(currentState);
		}

		return false;                               
	}
	
	/**
	 * This method solves the problem by searching from first state to any end state.
	 * 
	 * @param startedState first state of board
	 * @param scorer used to evaluate the score of a state
	 * @param comparator
	 */
	@SuppressWarnings("unchecked")
	public Stack<Move> solve(FreeCellState startedState, Scorer scorer, 
			Comparator<short[]> comparator) {
		
		currentState = startedState;
		this.scorer = scorer;
		
		prev = new TreeMap<short[], Integer>(comparator);
		
		moveStack = new Stack<Move>();
		
		pQueue = new TreeMap<Integer, FreeCellState>();
		
		pQueue.put(this.scorer.eval(currentState), currentState.clone());
		
		int lastBoardID;
		while (pQueue.size() > 0) {
		
			// get the first entry in current stack of FreeCell State
			Entry<Integer, FreeCellState> firstEntry = pQueue.firstEntry();
			
			currentState = firstEntry.getValue();
			
			// remove the first entry of current stack
			pQueue.remove(firstEntry.getKey());
			
			// last must be set PRIOR to invoking search, since it is used for
			// linking solutions. moveStack must be instantiated anew also, so
			// we only create a stack of moves from S.min to new K-distant nodes.
			lastTrace = (Trace)currentState.storeData();
			
			moveStack = new Stack<Move>();
			
			Trace nextTrace = new Trace((Stack<Move>)moveStack.clone(), lastTrace);
			
			currentState.store(nextTrace);

			if (lastTrace == null) {
				lastBoardID = 0;
			} else {
				lastBoardID = lastTrace.lastBoardID;
			}
			
			if (visit(lastBoardID, 0)) {
				Logger.write(System.out, "iDFS.solve(): Bravo!");
				// update stored move information.
				nextTrace = new Trace((Stack<Move>)moveStack.clone(), nextTrace);
				currentState.store(nextTrace);
				
				moveStack = traceBack(currentState);
				return moveStack;
			}
		}	
		
		return null;
	}
	
	public static Stack<Move> traceBack(FreeCellState finalState) {
		Stack<Move> result = new Stack<Move>();
		Trace trace = (Trace)finalState.storeData();
		
		Stack<Trace> traceStack = new Stack<Trace>();
		while (trace != null) {
			traceStack.push(trace);
			trace = trace.previous;
		}
		
		while (!traceStack.isEmpty()) {
			trace = traceStack.pop();
			for (Iterator<Move> it = trace.moves.iterator(); it.hasNext(); ) {
				result.push(it.next());
			}
		}
		
		return result;
	}	
}
