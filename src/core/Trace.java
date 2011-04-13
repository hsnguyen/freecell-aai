package core;

import java.util.Stack;

public class Trace {
	
	public final Stack<Move> moves;
	
	public final Trace previous;
	
	/** ID of board that is at the end of these moves */
	public final int lastBoardID;
	
	public Trace (Stack<Move> moves, Trace previous, int lastBoardID) {
		this.moves = moves;
		this.previous = previous;
		this.lastBoardID = lastBoardID;
	}
	
	public Trace (Stack<Move> moves, Trace previous) {
		this.moves = moves;
		this.previous = previous;
		this.lastBoardID = 0;
	}
	
	public Trace (Stack<Move> moves) {
		this.previous = null;
		this.moves = moves;
		this.lastBoardID = 0;
	}
	
	public Trace (Stack<Move> moves, int lastBoardID) {
		this.previous = null;
		this.moves = moves;
		this.lastBoardID = lastBoardID;
	}
}
