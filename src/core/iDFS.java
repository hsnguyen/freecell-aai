package core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.TreeMap;

public class iDFS {
	
	/** maximum number of visited states that are saved */
	final int MAX_NUMBER_OF_STATES = 2000000;
	
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
	 * Contains all moves of each visit.
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
			
			double score;
			if (Configuration.MAKE_DATA == true) {
				score = scorer.eval(currentState);
			}
			else {
				score = scorer.evalByNeuralNetworks(currentState);
			}
			
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
			
			// determine whether currentState has already visited by encoding
			// it with key() function.
			short[] key = (short[])currentState.key();
			Integer exist = prev.get(key);
			
			if (exist == null) {	// if currentState had not ever been visited
				++count;
				
				if (prev.size() > MAX_NUMBER_OF_STATES) {
					prev.clear();
					System.gc();
				}

				prev.put(key, count); 
				moveStack.push(move);               
				
				if (visit(count, depth+1)) {	// continue visiting with one-added depth 
					return true;	// visit successfully
				}
				
				moveStack.pop();
			}
			
			// turn back to the state before moving
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
		if (Configuration.MAKE_DATA == false) {
			this.scorer.loadData();
		}
		
		prev = new TreeMap<short[], Integer>(comparator);
		
		moveStack = new Stack<Move>();
		
		pQueue = new PriorityQueue<FreeCellState>();
		
		// add the starting state to queue
		FreeCellState aCopy = currentState.clone();
		if (Configuration.MAKE_DATA == false) {
			aCopy.score(this.scorer.evalByNeuralNetworks(currentState));
		} else {
			aCopy.score(this.scorer.eval(currentState));
		}
		pQueue.add(aCopy);
		
		int lastBoardID;
		// begin solving by searching
		while (pQueue.size() > 0) {
			
			// get the first entry in current stack of FreeCell State
			currentState = pQueue.poll();
			
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
				// search successfully
				// update stored move information.
				nextTrace = new Trace((Stack<Move>)moveStack.clone(), nextTrace);
				currentState.store(nextTrace);
				
				return traceBack(currentState);
			}
		}	
		
		return null;	// no solution
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
				Move tmp = it.next();
				result.add(tmp);
				System.out.println("Normal move: "+ tmp);
				if (tmp.automoves.size() > 0)
					for(int i = 0 ; i < tmp.automoves.size(); i++){
						result.add(tmp.automoves.get(i));
						System.out.println("Automove: " + tmp.automoves.get(i));
					}
			}
		}
		
		return result;
	}	
}
