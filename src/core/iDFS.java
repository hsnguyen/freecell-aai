package core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.TreeMap;

public class iDFS {
	
	/** maximum number of visited states that are saved */
	final int MAX_NUMBER_OF_STATES = 1000000;
	
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
	private PriorityQueue<FreeCellState> pQueue;
	
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
			// set score of state
			aCopy.score(score);
			// store trace of this state
			aCopy.store(new Trace((Stack<Move>)moveStack.clone(), lastTrace));
			// insert state into queue
			pQueue.add(aCopy);
			
			return false;
		}

		// get all available moves
		ArrayList<Move> listValidMoves = currentState.validMoves();
		
		// try with each move
		for (int i = 0; i < listValidMoves.size(); ++i) {
			// get i(th) move
			Move move = listValidMoves.get(i);
			// apply move to current state.
			move.execute(currentState); 
			
			// 
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
	public ArrayList<Move> solve(FreeCellState startedState, Scorer scorer, 
			Comparator<short[]> comparator) {
		
		currentState = startedState;
		this.scorer = scorer;
		
		prev = new TreeMap<short[], Integer>(comparator);
		
		moveStack = new Stack<Move>();
		
		pQueue = new PriorityQueue<FreeCellState>();
		
		FreeCellState aCopy = currentState.clone();
		aCopy.score(this.scorer.eval(currentState));
		pQueue.add(aCopy);
		
		int lastBoardID;
		while (pQueue.size() > 0) {
			
			// get the first entry in current stack of FreeCell State
			currentState = pQueue.poll();
			
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
				
				return traceBack(currentState);
			}
		}	
		
		return null;
	}
	
	public static ArrayList<Move> traceBack(FreeCellState finalState) {
		ArrayList<Move> result = new ArrayList<Move>();
		Trace trace = (Trace)finalState.storeData();
		
		Stack<Trace> traceStack = new Stack<Trace>();
		while (trace != null) {
			traceStack.push(trace);
			trace = trace.previous;
		}
		
		while (!traceStack.isEmpty()) {
			trace = traceStack.pop();
			for (Iterator<Move> it = trace.moves.iterator(); it.hasNext(); ) {
				result.add(it.next());
			}
		}
		
		return result;
	}	
}
