package core;

import java.io.FileReader;
import java.util.Arrays;
import java.util.Scanner;

public class Scorer {
	
	final int DIMENSION_OF_INPUT = 67;
	final int NUMBER_OF_NEURONS = 5;
	
	private double[][] hlWeights = new double[NUMBER_OF_NEURONS][DIMENSION_OF_INPUT];
	private double[][] hlBias = new double[NUMBER_OF_NEURONS][1];
	private double[][] olWeights = new double[1][NUMBER_OF_NEURONS];
	private double[][] olBias = new double[1][1];
	
	public void score(FreeCellState state) {
		return;
	}
	
	public void loadData() {
		System.out.println("Scorer->loadData: entering...");
		try {
			Scanner sc = new Scanner(new FileReader("hiddenM.txt"));
			for (int i = 0; i < NUMBER_OF_NEURONS; ++i) {
				for (int j = 0; j < DIMENSION_OF_INPUT; ++j) {
					hlWeights[i][j] = sc.nextDouble();
				}
			}
			sc.close();
			
			sc = new Scanner(new FileReader("biasHiddenM.txt"));
			for (int i = 0; i < NUMBER_OF_NEURONS; ++i) {
				for (int j = 0; j < 1; ++j) {
					hlWeights[i][j] = sc.nextDouble();
				}
			}
			sc.close();
			
			sc = new Scanner(new FileReader("outputM.txt"));
			for (int i = 0; i < 1; ++i) {
				for (int j = 0; j < NUMBER_OF_NEURONS; ++j) {
					olWeights[i][j] = sc.nextDouble();
				}
			}
			sc.close();
			
			sc = new Scanner(new FileReader("biasOutputM.txt"));
			for (int i = 0; i < 1; ++i) {
				for (int j = 0; j < 1; ++j) {
					olBias[i][j] = sc.nextDouble();
				}
			}
			sc.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *  50% -> foundation
	 *  10% -> sum of freecell cards
	 *  10% -> sum of head columns * number of free columns
	 *  25% -> number of valid moves 
	 */
	public int evalBadVersion(FreeCellState state) {
		
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
				if (isNextCard[rank][suit]) value += (n-j-1); // 50%
			}
		}
		
		value += 2*state.getNumberOfValidMoves(); // 25% valid moves
		
		int sFreeCards = 0;
		for (int i = 0; i < 4; ++i) {
			sFreeCards += state.freecell[i];
		}
		
		value += 5*sFreeCards;	// 10% 
		
		int nFreeColumns = 0;
		int sHeadColumns = 0;
		for (int i = 0; i < 8; ++i) {
			if (state.columns[i].getNum() == 0) nFreeColumns++;
			else sHeadColumns += state.columns[i].get(0);
		}
		
		value += 5*(nFreeColumns+1)*(sHeadColumns+1);
		
		return value;
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
	
	public double[][] mulMatrixes(double[][] A, double[][] B, int m, int n, int p) {
		double[][] C = new double[m][p];
		for (int i = 0; i < m; ++i) {
			for (int j = 0; j < p; ++j) {
				C[i][j] = 0;
				for (int k = 0; k < n; ++k) {
					C[i][j] += A[i][k]*B[k][j];
				}
			}
		}
		return C;
	}
	
	public double[][] addMatrixes(double[][] A, double[][] B, int m, int n) {
		double[][] C = new double[m][n];
		for (int i = 0; i < m; ++i) {
			for (int j = 0; j < n; ++j) {
				C[i][j] = A[i][j]+B[i][j];
			}
		}
		return C;
	}
	
	public double evalByNeuralNetworks(FreeCellState state) {
		
		// input signals: 68x1
		double[][] input = new double[DIMENSION_OF_INPUT][1];
		
		int index = 0;
		// 4 freecell
		for(int i=0; i < 4; i++) input[index++][0] = state.freecell[i];
		// 4 foundation
		for(int i=0; i < 4; i++) input[index++][0] = state.foundation[i];
		// 8 columns, two columns is separated by -1
		for(int i=0; i < 8; i++) {
			Column col = new Column(state.columns[state.sort[i]]);
			for(int j=0; j<col.getNum(); j++) {
				input[index++][0] = col.get(j);
			}
			if (index < DIMENSION_OF_INPUT) input[index++][0] = -1;
		}
		for (; index < DIMENSION_OF_INPUT;) {
			input[index++][0] = -1;
		}
		
		
		// weights of hidden layer: 5x68;
		
		// hlWeights*input : (5x68)x(68x1) = 5x1
		double[][] hiddenOutput = mulMatrixes(hlWeights, input, 5, DIMENSION_OF_INPUT, 1);
		
		// hiddenOutput+bias: (5x1)+(5x1) = 5x1
		hiddenOutput = addMatrixes(hiddenOutput, hlBias, 5, 1);
		
		// apply to tansig function
		for (int i = 0; i < 5; ++i) {
			hiddenOutput[i][0] = 2/(1+Math.exp(-2*hiddenOutput[i][0]))-1;
		}
		
		// outputWeights*hiddenOutput: (1x5)x(5x1) = 1x1
		double[][] output = mulMatrixes(olWeights, hiddenOutput, 1, 5, 1);
		
		// output+bias: (1x1)+(1x1) = 1x1
		output = addMatrixes(output, olBias, 1, 1);	
		
		//double result = output[0][0] < 0 ? 0 : output[0][0];
		double result = output[0][0];
		
		System.out.println("Scorer->evalByNeuralNetworks: result: " + result);
		
		return result;
	}
}
