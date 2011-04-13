package core;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.StringTokenizer;
import core.Column;

public class FreeCellState implements Comparable<FreeCellState> {
	public short freecell[]; // 4 freecells
	public short foundation[]; // 4 foundations
	public Column columns[]; // 8 columns
	private int sort[]; // sort column -> get similar key when compare 2 similar state
	private int score; // score of state
	private Object store; // store last state
	
	/**
	 * default constructor
	 */
	public FreeCellState() {
		freecell = new short[]{0,0,0,0};
		foundation = new short[]{0,0,0,0};
		columns = new Column[8];
		for(int i=0; i<8; i++) {
			columns[i] = new Column();
		}
		sort = new int[]{0,1,2,3,4,5,6,7};
	}
	
	/**
	 * copy constructor
	 * @param state
	 */
	public FreeCellState(FreeCellState state) {
		freecell = state.freecell;
		foundation = state.foundation;
		for(int i=0; i<state.columns.length; i++) {
			columns[i] = new Column(state.columns[i]);
		}
		sort = state.sort;
	}
	
	/**
	 * constructor
	 * @param freecell: cardlist in freecell
	 * @param foundation: cardlist in foundation
	 * @param columns: columnlist
	 */
	public FreeCellState(short[] freecell, short[] foundation, Column[] columns) {
		this.freecell = freecell;
		this.foundation = foundation;
		this.columns = columns;
		this.sort = new int[]{0,1,2,3,4,5,6,7};
		
		sortColumn();
	}
	
	public FreeCellState clone() {
		Column[] columnCopy = new Column[8];
		for (int i = 0; i < 8; i++) {
			columnCopy[i] = columns[i].clone();
		}
		short[] freeCellCopy = new short[4];
		short[] foundationCopy = new short[4];
		for (int i = 0; i < 4; i++) {
			freeCellCopy[i] = freecell[i];
			foundationCopy[i] = foundation[i];
		}
		FreeCellState stateCopy = new FreeCellState(freeCellCopy, foundationCopy, columnCopy);
		
		return stateCopy;
	}
	
	public Comparator<short[]> comparator() {
		return new Comparator<short[]>() {
			// keys are the same length
			public int compare(short[] key1, short[] key2) {
				int n = key1.length;
				for (int i = 0; i < n; ++i) {
					if (key1[i] < key2[i]) { 
						return -1; 
					}
					if (key1[i] > key2[i]) { 
						return +1; 
					}
				}
				return 0;
			}
		};
	}
	
	public int compareTo(FreeCellState state) {
		return toString().compareTo(state.toString());
	}
	
	public void sortColumn() {
		for (int j = 1; j <= 7; j++) {
			int sj = sort[j];
			int value = this.columns[sj].get(0);
			
			int i = j-1;
			while (i >= 0 && this.columns[sort[i]].get(0) > value) {
				sort[i+1] = sort[i];
				i--;
			}
			sort[i+1]=sj;
		}
	}
	
	/**
	 * get space available for moving from col to col
	 * amout of space = freecolspace * (freeCellSpace + 1);
	 * @return
	 */
	public int getFreeSpace() {
		int freecellSpace = 0, freeColSpace = 0;
		for(int i=0; i<=3; i++) {
			if(freecell[i] == 0) freecellSpace ++;
		}
		for(int i=0; i<8; i++) {
			if(columns[i].getNum() == 0) freeColSpace ++;
		}
		
		return (freecellSpace + 1) * (freeColSpace + 1);
	}
	/**
	 * remove a card from freecell
	 * @param card
	 * @return card
	 */
	public short removeFreeCell(short card) {
		int index = -1;
		for(int i=3; i>=0; i--) {
			if(freecell[i] == card) {
				index = i;
				break;
			}
		}
		if (index > -1) {
			for(int i = index; i>0; i--) {
				freecell[i] = freecell[i-1];
			}
			freecell[0] = 0;
		}
		else {
			System.err.println("card " + card + " is not in freecell");
		}
		return card;
	}
	
	/**
	 * check if there is a slot in freecell or not
	 * @return boolean
	 */
	public boolean hasFreeCell() {
		return freecell[0] == 0;
	}
	
	/**
	 * insert a card in freecell
	 * auto order, the larger card is put at the higher index
	 * @param card
	 */
	public void insertFreeCell(short card) {
		if(! hasFreeCell()) {
			System.err.println("there is no freecell left");
			return;
		}
		int index = -1;
		for(int i = 3; i>=0; i--) {
			if (freecell[i] <= card) {
				index = i;
				break;
			}
		}
		for(int i=0; i<index; i++) {
			freecell[i] = freecell[i+1];
		}
		freecell[index] = card;
	}
	
	/**
	 * check if the state is complete or not
	 * @return
	 */
	public boolean hasWon() {		
		for(int i=0; i<freecell.length; i++) 
			if(freecell[i] != 0) return false;
		
		for(int i=0; i<foundation.length; i++)
			if(foundation[i] != 13) return false;
		
		for(int i=0; i<columns.length; i++) 
			if(columns[i].getNum() > 0) return false;
		
		return true;
	}
	
	/**
	 * remove a rank card from foundation
	 */
	public short removeFoundation(int suit) {
		short rank = foundation[suit];
		if(rank == 0) return 0;
		return foundation[suit]--;
	}
	
	/**
	 * insert a card to foundation
	 * @param card
	 * @return true if insert complete, false otherwise
	 */
	public boolean insertFoundation(short card) {
		int suit = card % 4;
		int rank = card >> 2;
		int currentRank = foundation[suit];
		if(rank == currentRank + 1) foundation[suit] = (short)rank;
		else return false;
		return true;
	}
	
	public boolean setFoundation(short card) {
		int suit = card % 4;
		int rank = card >> 2;
		foundation[suit] = (short)rank;
		return true;
	}
	
	/**
	 * get key for state, use this function to compare between two state
	 * @return
	 */
	public Object key() {
		short[] key = new short[69];
		int index = 0;
		// 4 freecell
		for(int i=0; i < 4; i++) key[index++] = freecell[i];
		// 4 foundation
		for(int i=0; i < 4; i++) key[index++] = foundation[i];
		// 8 columns, two columns is separated by -1
		for(int i=0; i < 8; i++) {
			Column col = new Column(columns[sort[i]]);
			for(int j=0; j<col.getNum(); j++) {
				key[index ++] = col.get(j);
			}
			key[index ++] = -1;
		}
		
		return key;
	}
	
	/**
	 * set score for state
	 * @param score
	 */
	public void score(int score) {
		this.score = score;
	}
	
	/**
	 * get state's score
	 * @return
	 */
	public int score() {
		return this.score;
	}
	
	/**
	 * store information, if return null there is no last
	 * @param store
	 * @return
	 */
	public Object store(Object store) {
		Object last = this.store;
		this.store = store;
		return last;
	}
	
	/**
	 * get current store information
	 * @return
	 */
	public Object storeData() {
		return this.store;
	}
	
	/**
	 * get initial state from file
	 * @param filename
	 */
	public void getInitialState(String filename) {
		try {
			java.util.Scanner sc = new java.util.Scanner(new File(filename));
			// first line, 4 value of freecells, 4 value of foundation
			String line = sc.nextLine();
			StringTokenizer token = new StringTokenizer(line);
			for(int i=0; i<4; i++) insertFreeCell(Column.encodeCard(token.nextToken()));
			for(int i=0; i<4; i++) setFoundation(Column.encodeCard(token.nextToken()));
			
			// other lines, columns
			while(sc.hasNextLine()) {
				line = sc.nextLine();
				token = new StringTokenizer(line);
				int index = 0;
				while(token.hasMoreTokens()) {
					columns[sort[index++]].add(Column.encodeCard(token.nextToken()));
				}
			}
			sortColumn();
		} catch(Exception e){}
	}
	
	/**
	 * for debug
	 * print state to string
	 */
	public String toString() {
		String ret = "";
		for(int i=0; i<4; i++) ret += Column.decodeCard(freecell[i]) + " ";
		for(int i=0; i<4; i++) ret += foundation[i] + " ";
		ret += "\n";
		int maxCol = 0;
		for(int i=0; i<8; i++) {
			if (maxCol < columns[sort[i]].getNum()) maxCol = columns[sort[i]].getNum();  
		}
		for(int i=0; i<maxCol; i++) {
			for(int j=0; j<8; j++) {
				if(columns[sort[j]].getNum() > i) {
					ret += Column.decodeCard(columns[sort[j]].get(i)) + " ";
				}
				else ret += "   ";
			}
			ret += "\n";
		}
		return ret;
	}
	
	/**
	 * Get all possible move from current state
	 * these moves should be order by:
	 * 		1. freecell to foundation
	 * 		2. column to foundation
	 * 		3. freecell to column
	 * 		4. column to column
	 * 		5. column to freecell
	 * note that there are 5 types of move in Move class
	 * 		1. column to column
	 * 		2. column to freeCell
	 * 		3. column to foundation
	 * 		4. freecell to foundation
	 * 		5. freecell to column
	 * @return
	 */
	public ArrayList<Move> validMoves() {
		ArrayList<Move> ret = new ArrayList<Move>();
		
		// freecell to foundation
		for(int i=3; i>=0; i--) {
			Move m = new Move(4);
			if(m.isValidFreeToFoundation(this, freecell[i]))
				ret.add(m);
		}
		
		
		// column to foundation
		for(int i=0; i<8; i++) {
			Move m = new Move(3);
			if(m.isValidColumnToFoundation(this, i))
				ret.add(m);
		}
		
		
		// freecell to column
		for(int i=3; i>=0; i--) {
			for(int j = 0; j<8; j++) {
				Move m = new Move(5);
				if(m.isValidFreeCellToColumn(this, freecell[i], j)) {
					ret.add(m);
				}
			}
		}
		
		
		// column to column
		// select lowest card first and then climb up until can't move all of them
		for(int fromPos = 0; fromPos < 8; fromPos ++) {
			Column ccol = columns[fromPos];
			if(ccol.getNum() == 0) continue;
			short currentCard = ccol.get(ccol.getNum() - 1);
			@SuppressWarnings("unused")
			int suit = currentCard % 4;
			int rank = currentCard >> 2;
		
			boolean isCurrentBlack = ccol.isBlack();
			
			for(int cardNum = 1; cardNum <= ccol.getNum(); cardNum ++) {
				// if num of cards > 1 -> update current rank and isCurrentBlack
				if(cardNum > 1) {
					short nextCard = ccol.get(ccol.getNum() - cardNum);
					int nextSuit = nextCard % 4;
					int nextRank = nextCard >> 2;
					boolean isNextBlack = (nextSuit == Column.CLUB || nextSuit == Column.SPADES);
					// if current suit and next suit is the same -> break
					if(isCurrentBlack == isNextBlack) break;
					// if next rank is not equal rank + 1 -> break
					if(nextRank != rank + 1) break;
					
					//update suit, rank, card, isCurrentBlack, ...
					suit = nextSuit;
					rank = nextRank;
					isCurrentBlack = isNextBlack;
				}
				
				boolean checkMoveToBlank = false; // check if there is a move to blank earlier
				// check all position to move
				for(int toPos = 0; toPos < 8; toPos ++) {
					// if they are same column, continue search others column
					if(toPos == fromPos) continue;
					// this move is not necessary
					if(ccol.getNum() == cardNum && columns[toPos].getNum() == 0) continue;
					
					if(!checkMoveToBlank) {
						if(columns[toPos].getNum() == 0) {
							checkMoveToBlank = true;
						}
						
						Move m = new Move(1);
						if(m.isValidColumnToColumn(this, fromPos, toPos, cardNum)) {
							ret.add(m);
						}
					}
				}
			}
		}
		
		// column to freeCell
		for(int i = 0; i < 8; i++) {
			if(columns[i].getNum() == 0) continue;
			Move m = new Move(2);
			if(m.isValidColumnToFreeCell(this, i))
				ret.add(m);
		}
		
		return ret;
	}
	
	/*
	public static void main(String[] args) {
		FreeCellState state = new FreeCellState();
		state.getInitialState("input.txt");
		System.out.println(state.toString());
		
		//Move m = new Move(1);
		//m.execute(state, (short)-1, state.sort[2], state.sort[1], 2);
		//System.out.println(state.toString());
		//m.undo(state);
		//System.out.println(state.toString());
		
		System.out.println("there are " + state.validMoves().size() + " valid moves in this map");
	}
	*/
}
