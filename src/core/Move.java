package core;

import java.util.ArrayList;

/**
 * Class simulate a move
 * @author nhatuan
 * Contains 5 type of move:
 * 		1. Column to Column
 * 		2. Column to FreeCell
 * 		3. Column to Foundation
 * 		4. FreeCell to Foundation
 * 		5. FreeCell to Column
 * And one special move: automove which move all possible card in freecell 
 * and columns to foundation.
 */

public class Move {
	
	int type; // type of move
	short card; // card
	int suit; // suit of card
	int rank; // rank of card
	int fromColumn; // move from column
	int toColumn; // move to column
	int numCard; // number of card move from column to column
	ArrayList<Move> automoves = new ArrayList<Move>();
	
	/**
	 * there are 5 type of move
	 * 1. Column to Column
	 * 2. Column to Freecell
	 * 3. Column to Foundation
	 * 4. FreeCell to Foundation
	 * 5. FreeCell to Column
	 */
	public Move(int type) {
		this.type = type;
	}
	
	/**
	 * execute move again when recived all parameters
	 * @param state
	 * @return
	 */
	public boolean execute(FreeCellState state) {
		return this.execute(state, card, fromColumn, toColumn, numCard);
	}
	
	/**
	 * first move, must provide all parameters
	 * @param state current state
	 * @param card card to move (using when move from freecell to others)
	 * @param fromColumn pick a card from a column index (using when move from column to others)
	 * @param toColumn destination is a column (using when move from others to column)
	 * @param numCard number of card for move (using when move from column to column)
	 * @return
	 */
	public boolean execute(FreeCellState state, short card, int fromColumn, int toColumn, int numCard) {
		boolean ret = false;
		switch (type){
			case 1:
				ret = columnToColumn(state, fromColumn, toColumn, numCard);
				break;
			case 2:
				ret = columnToFreeCell(state, fromColumn);
				break;
			case 3:
				ret = columnToFoundation(state, fromColumn);
				break;
			case 4:
				ret = freeCellToFoundation(state, card);
				break;
			case 5:
				ret = FreeCellToColumn(state, card, toColumn);
				break;
		}
		// check automove
		automove(state);
		
		return ret;
	}
	
	/**
	 * undo a move
	 * @param state
	 * @return
	 */
	public boolean undo (FreeCellState state) {
		//undo automove before undo others
		undoAutoMove(state);
		switch (type){
			case 1:
				return undoColumnToColumn(state);
			case 2:
				return undoColumnToFreeCell(state);
			case 3:
				return undoColumnToFoundation(state);
			case 4:
				return undoFreeCellToFoundation(state);
			case 5:
				return undoFreeCellToColumn(state);
		}
		
		return false;
	}

//======================================================================================================================
// 1. move from column to column
//======================================================================================================================

	/**
	 * check if move from column to comlumn is valid
	 * @param state
	 * @param fromColumn
	 * @param toColumn
	 * @param numCard
	 * @return
	 */
	public boolean isValidColumnToColumn(FreeCellState state, int fromColumn, int toColumn, int numCard) {
		try {
			this.fromColumn = fromColumn;
			this.toColumn = toColumn;
			this.numCard = numCard;
			
			
			
			Column fromCol = state.columns[fromColumn];
			Column toCol = state.columns[toColumn];
			
			if(fromCol.getNum() == 0) return false;
			
			card = fromCol.get(fromCol.getNum() - numCard);
			suit = card % 4;
			rank = card >> 2;
			
			int canMove = state.getFreeSpace();
			if(toCol.getNum() == 0) {
				int freecellSpace = 0, freeColSpace = 0;
				for(int i=0; i<=3; i++) {
					if(state.freecell[i] == 0) freecellSpace ++;
				}
				for(int i=0; i<8; i++) {
					if(state.columns[i].getNum() == 0) freeColSpace ++;
				}
				
				canMove = (freecellSpace + 1) * freeColSpace;
			}
			
			if(numCard > canMove) return false;
			if(toCol.getNum() == 0) return true;
			
			boolean isBlackCard = (suit == Column.CLUB || suit == Column.SPADES);
			
			return (isBlackCard != toCol.isBlack() && rank == toCol.rank() - 1);
		}catch (Exception ex) {
			return false;
		}
	}
	
	/**
	 * execute move from column to column
	 * @param state
	 * @param fromColumn
	 * @param toColumn
	 * @param numCard
	 * @return
	 */
	public boolean columnToColumn(FreeCellState state, int fromColumn, int toColumn, int numCard) {
		if(!isValidColumnToColumn(state, fromColumn, toColumn, numCard)) return false;
		
		int index = 0;
		short[] cards = new short[numCard];
		Column fromCol = state.columns[fromColumn];
		Column toCol = state.columns[toColumn];
		while(index < numCard) {
			cards[index ++ ] = fromCol.remove();
		}
		
		for(int i = numCard - 1; i >= 0; i--) {
			toCol.add(cards[i]);
		}
		
		// sort column order again
		if(fromCol.getNum() == 0 || toCol.getNum() == 1) state.sortColumn();
		
		return true;
	}
	
	/**
	 * undo move
	 * @param state
	 * @return
	 */
	public boolean undoColumnToColumn(FreeCellState state) {
		
		Column fromCol = state.columns[fromColumn];
		Column toCol = state.columns[toColumn];
		int index = 0;
		short[] cards = new short[numCard];
		while(index < numCard) {
			cards[index ++ ] = toCol.remove();
		}
		
		for(int i = numCard - 1; i >= 0; i--) {
			fromCol.add(cards[i]);
		}
		
		return true;
	}
	
//======================================================================================================================
// 2. move from column to freecell
//======================================================================================================================

	/**
	 * check if move from column to freecell is valid
	 * @param state
	 * @param column
	 */
	public boolean isValidColumnToFreeCell(FreeCellState state, int column) {
		try {
			short card = state.columns[column].get(state.columns[column].getNum() - 1);
			
			fromColumn = column;
			this.card = card;
			suit = card % 4;
			rank = card >> 2;
			
			if(state.columns[column].getNum() == 0) return false;
			return state.hasFreeCell();
		}catch(Exception ex) {
			return false;
		}
	}
	
	/**
	 * execute move from column to freecell
	 * @param state
	 * @param column
	 * @return
	 */
	public boolean columnToFreeCell(FreeCellState state, int column) {
		if(!isValidColumnToFreeCell(state, column)) return false;
		
		state.columns[column].remove();
		state.insertFreeCell(card);
		
		if(state.columns[column].getNum() == 0) state.sortColumn();
		
		return true;
	}
	
	/**
	 * execute undo move from column to freecell
	 * @param state
	 * @return
	 */
	public boolean undoColumnToFreeCell(FreeCellState state) {
		state.removeFreeCell(card);
		state.columns[fromColumn].add(card);
		return true;
	}
//======================================================================================================================
// 3. move from column to foundation
//======================================================================================================================
	
	/**
	 * check if move is valid
	 * @param state
	 * @param column
	 */
	public boolean isValidColumnToFoundation(FreeCellState state, int column) {
		try {
			if(state.columns[column].getNum() == 0) return false;
			short card = state.columns[column].get(state.columns[column].getNum() - 1);
			
			this.fromColumn = column;
			this.card = card;
			suit = card % 4;
			rank = card >> 2;
			if(rank == 1) return true;
			return (state.foundation[suit] == rank - 1);
		}catch(Exception ex) {
			return false;
		}
	}
	/**
	 * execute move from column to foundation
	 * @param state
	 * @param column
	 * @return
	 */
	public boolean columnToFoundation(FreeCellState state, int column) {
		if(!isValidColumnToFoundation(state, column)) return false;
		
		Column col = state.columns[column];
		col.remove();
		state.insertFoundation(card);
		
		if (col.getNum() == 0) state.sortColumn();
		
		return true;
	}
	/**
	 * execute undo move from column to foundation
	 * @param state
	 * @return
	 */
	public boolean undoColumnToFoundation(FreeCellState state) {
		state.removeFoundation(suit);
		state.columns[fromColumn].add(card);
		return true;
	}
//======================================================================================================================
// 4. move from freecell to foundation
//======================================================================================================================
	/**
	 * check if move from freecell to foundation is valid
	 * @param state
	 * @param card
	 * @return
	 */
	public boolean isValidFreeToFoundation(FreeCellState state, short card) {
		try {
			if(card == 0) return false;
			
			this.card = card;
			suit = card % 4;
			rank = card >> 2;
			
			return (state.foundation[suit] == rank - 1);
		}catch(Exception ex) {
			return false;
		}
	}
	/**
	 * execute move from freecell to foundation
	 * @param state
	 * @param card
	 * @return
	 */
	public boolean freeCellToFoundation(FreeCellState state, short card) {
		if(!isValidFreeToFoundation(state, card)) return false;
		
		state.insertFoundation(card);
		state.removeFreeCell(card);
		
		return true;
	}
	/**
	 * undo move from freecell to foundation
	 * @param state
	 * @param card
	 * @return
	 */
	public boolean undoFreeCellToFoundation(FreeCellState state) {
		suit = card % 4;
		state.removeFoundation(suit);
		state.insertFreeCell(card);
		return true;
	}

//======================================================================================================================
// 5. move from freecell to column
//======================================================================================================================

	/**
	 * check if move from freecell to column is valid
	 * @param state
	 * @param card
	 * @param column
	 * @return
	 */
	public boolean isValidFreeCellToColumn(FreeCellState state, short card, int column) {
		try {
			if (card == 0) return false;
			
			Column col = state.columns[column];
			if(col.getNum() == 0) return true;
			
			toColumn = column;
			this.card = card;
			suit = card % 4;
			rank = card >> 2;
			
			boolean isBlackCard = (suit == Column.CLUB || suit == Column.SPADES);
	 		
			return (isBlackCard != col.isBlack() && rank == col.rank() - 1);
		}catch(Exception ex) {
			return false;
		}
	}
	
	/**
	 * execute move from freecell to a column
	 * @param state
	 * @param card
	 * @param column
	 * @return
	 */
	public boolean FreeCellToColumn(FreeCellState state, short card, int column) {
		if(!isValidFreeCellToColumn(state, card, column)) return false;
		
		state.removeFreeCell(card);
		state.columns[column].add(card);
		
		return true;
	}
	
	/**
	 * undo move from freecell to column
	 * @param state
	 * @return
	 */
	public boolean undoFreeCellToColumn(FreeCellState state) {
		state.columns[toColumn].remove();
		state.insertFreeCell(card);
		
		return true;
	}

//======================================================================================================================
// AUTOMOVE
//======================================================================================================================

	/**
	 * automove a card to foundation
	 */
	public void automove(FreeCellState state) {
		//clear all automove before
		automoves.clear();
		
		boolean flag = true;
		
		// we must find all automoves
		while(flag) {
			flag = false;
			
			// begin with freecell cards
			// only automoves when all the card with different color and higher rank 
			// than current rank -1 is moved in foundation
			for(int i=0; i<state.freecell.length; i++) {
				short freeCard = state.freecell[i];
				int freeSuit = freeCard % 4;
				int freeRank = freeCard >> 2;
			
				boolean canMove = false;
				boolean isBlackCard = (freeSuit == Column.CLUB || freeSuit == Column.SPADES);
				if(isBlackCard) {
					if(state.foundation[Column.DIAMOND] >= (freeRank - 1) || state.foundation[Column.HEARTS] >= (freeRank -1))
						canMove = true;
				}
				else {
					if(state.foundation[Column.SPADES] >= (freeRank - 1) || state.foundation[Column.CLUB] >= (freeRank -1))
						canMove = true;
				}
				
				if(canMove) {
					Move ftf = new Move(4);
					if(ftf.isValidFreeToFoundation(state, freeCard)) {
						ftf.execute(state, freeCard, -1, -1, 1);
						automoves.add(ftf);
						flag = true;
					}
				}
			}
			
			// after get through freecell, check in all bottom of columns
			// the condition to move to foundation is similar to the condition at freecell
			for(int i=0; i<state.columns.length; i++) {
				if(state.columns[i].getNum() == 0) continue;
				short colCard = state.columns[i].get(state.columns[i].getNum() - 1);
				int colSuit = colCard % 4;
				int colRank = colCard >> 2;
			
				boolean canMove = false;
				boolean isBlackCard = (colSuit == Column.CLUB || colSuit == Column.SPADES);
				if(isBlackCard) {
					if(state.foundation[Column.DIAMOND] >= (colRank - 1) || state.foundation[Column.HEARTS] >= (colRank -1))
						canMove = true;
				}
				else {
					if(state.foundation[Column.SPADES] >= (colRank - 1) || state.foundation[Column.CLUB] >= (colRank -1))
						canMove = true;
				}
				
				if(canMove) {
					Move ctf = new Move(3);
					if(ctf.isValidColumnToFoundation(state, i)) {
						//ftf.execute(state, colCard, -1, -1, 1);
						ctf.execute(state, (short)-1, i, -1, 1);
						automoves.add(ctf);
						flag = true;
					}
				} 
			}
		}
	}
	
	/**
	 * undo all automove
	 * @param state
	 */
	public void undoAutoMove(FreeCellState state) {
		for(int i = automoves.size() - 1; i>=0; i--) {
			Move m = automoves.get(i);
			m.undo(state);
		}
		automoves.clear();
	}
}
