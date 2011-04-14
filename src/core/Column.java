package core;

import java.util.StringTokenizer;

/**
 * express a column of freecell column
 * @author nhatuan
 *
 */
public class Column {
	private int num = 0; // number of cards
	private short[] cards = new short[20]; // cards list
	
	public static final short CLUB = 0;
	public static final short DIAMOND = 1;
	public static final short HEARTS = 2;
	public static final short SPADES = 3;
	
	public Column() {} // default constructor
	
	public Column(Column c) {
		num = c.num;
		cards = c.cards;
	}
	/**
	 * Column constructor using string input
	 * @param input input string (card1 card2 card3 ...)
	 */
	public Column(String input) {
		StringTokenizer token = new StringTokenizer(input);
		String[] tmpCards = new String[20];
		while(token.hasMoreTokens()) {
			tmpCards[num++] = token.nextToken().toString();
		}
		encode(tmpCards);
	}
	
	/**
	 * Column constructor using pre-built column
	 * @param input pre-built column 
	 */
	public Column(short[] input) {
		cards = input;
		num = cards.length;
	}
	
	public Column clone() {
		Column col = new Column();
		col.num = num;
		short[] card = new short[cards.length];
		for (int i = 0; i < num; i++) {
			card[i] = cards[i];
		}
		col.cards = card;
		
		return col;		
	}
	
	/**
	 * encode cards
	 * @param tmpCard string expression of a card
	 */
	private void encode(String[] tmpCards) {
		for(int i=0; i<num; i++) {			
			cards[i] = encodeCard(tmpCards[i]);
		}
	}
	
	public static String decodeCard(short card) {
		
		if(card == 0) return "..";
		String cardsRank = ".A23456789TJQK";
		String cardsSuit = "CDHS";
		String ret = "";
		int suit = card % 4;
		int rank = card >> 2;
		ret += String.valueOf(cardsRank.charAt(rank)) + String.valueOf(cardsSuit.charAt(suit));
		
		return ret;
	}
	
	/**
	 * encode a card string to number
	 */
	public static short encodeCard(String card) {
		try {
			int suitNum = 0, rankNum = 0;
			String rank = card.substring(0, 1);
			if(rank.equals("T")) rankNum = 10;
			else if(rank.equals("J")) rankNum = 11;
			else if(rank.equals("Q")) rankNum = 12;
			else if(rank.equals("K")) rankNum = 13;
			else if(rank.equals("A")) rankNum = 1;
			else rankNum = Integer.valueOf(rank);
			
			
			char suit = card.charAt(1);
			if(suit == 'C') suitNum = CLUB;
			else if (suit == 'D') suitNum = DIAMOND;
			else if (suit == 'S') suitNum = SPADES;
			else suitNum = HEARTS;
			
			String binaryRank = Integer.toBinaryString(rankNum);
			String binarySuit = Integer.toBinaryString(suitNum).length() < 2 ? "0" + Integer.toBinaryString(suitNum) : Integer.toBinaryString(suitNum);
			
			short cardNum = (short)Integer.parseInt(binaryRank + binarySuit, 2);
			return cardNum;
		} catch(Exception ex) {return 0;}
	}
	
	/**
	 * To string
	 */
	public String toString() {
		String ret = "";
		for(int i=0; i<num; i++) {
			ret += cards[i] + " ";
		}
		return ret;
	}
	
	/**
	 * get bottom card rank
	 * return 0 if column is empty
	 * @param args
	 */
	public short rank() {
		if (num == 0) return 0;
		else return (short)(((cards[num-1])>>2));
	}
	
	/**
	 * check if bottom card is black
	 * @return boolean
	 */
	public boolean isBlack() {
		int suit =  cards[num-1] %4;
		return (suit == CLUB || suit == SPADES);
	}
	
	/**
	 * add a card to column
	 * @param card
	 */
	public void add(short card) {
		if(num >= 20) System.err.print("maximum cards exceeded");
		cards[num++] = card;
	}
	
	/**
	 * remove a card from column
	 * @return card
	 */
	public short remove() {
		if(num == 0) System.err.print("column is empty");
		short pop = cards[--num];
		cards[num] = 0;
		return pop;
	}
	
	/**
	 * get card from column at specific pos
	 * @param pos pisition
	 * @return card
	 */
	public short get(int pos) {
		if (pos > num - 1) return 0;
		else return cards[pos];
	}
	
	/**
	 * get number of cards in columns
	 * @return num
	 */
	public int getNum() {
		return num;
	}
	
	/*
	public static void main(String[] args) {
		Column c = new Column("AS 2S 3S 4S 5S 6S 7S 8S 9S TS JS QS KS");
		System.out.println(c.toString());
		System.out.println(c.rank());
	}
	*/
}
