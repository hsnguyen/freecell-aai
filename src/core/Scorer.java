package core;

import java.util.Arrays;

public class Scorer {
	
	public void score(FreeCellState state) {
		return;
	}
	
	public int eval(FreeCellState state) {
		
		// mark the next cards that could be moved in foundation
		boolean[][] isNextCard = new boolean[15][4];
		for (int i = 0; i < 15; ++i) {
			Arrays.fill(isNextCard[i], false);
		}
		isNextCard[state.foundation[0]+1][0] = true;
		isNextCard[state.foundation[1]+1][1] = true;
		isNextCard[state.foundation[2]+1][2] = true;
		isNextCard[state.foundation[3]+1][3] = true;
		
		// count the number of cards that are above the next cards in each column
		int value = 0;
		for (int i = 0; i < 8; ++i) {
			int n = state.columns[i].getNum();
			for (int j = 0; j < n; ++j) {
				int card = state.columns[i].get(j);
				int suit = card % 4;
				int rank = card >> 2;
				if (isNextCard[rank][suit]) value += (n-j-1);
			}
		}
		
		// free spaces are also considered 
		int nFreeSpaces = state.getFreeSpace();
		
		if (nFreeSpaces == 0) value *= 2;	// free spaces are less -> state is harder 
		
		// add moves for each of the piles still next cards.
		return value + (22 - state.foundation[0] - state.foundation[1] - 
							state.foundation[2] - state.foundation[3]);
	}
}
