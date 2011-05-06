package gui.controller;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;

import core.*;

import gui.view.FreeCellDrawing;

/**
 * Controller to process new deal.
 * 
 * @author George Heineman
 */
public class DealController implements ActionListener {

	/** Frame in which app runs. */
	final JFrame frame;
	
	/** Viewer to represent game on screen. */
	final FreeCellDrawing drawer;
	
	/** List containing moves. */
	final JList list;
	
	final JButton next, prev;
	
	/** Last listener (so we can delete with each redeal). */
	StateModifier listener;
	NextModifier nextListener;
	PrevModifier prevListener;
	
	public DealController (JFrame frame, FreeCellDrawing drawer, JList list, 
							JButton next, JButton prev) {
		this.frame = frame;
		this.drawer = drawer;
		this.list = list;
		this.next = next;
		this.prev = prev;

	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String fileName = "";	
		
		JFileChooser chooser = new JFileChooser();
	    chooser = new JFileChooser(); 
	    chooser.setCurrentDirectory(new java.io.File("."));
	    chooser.setDialogTitle("Initial state chosing");
	    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	    //
	    // disable the "All files" option.
	    //
	    chooser.setAcceptAllFileFilterUsed(false);
	    //    
	    if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) { 
	    	fileName = chooser.getSelectedFile().toString();
	      }
	    else {
	      System.out.println("No Selection ");
	      }
		// simple solution: 28352
		// no solution: 25382
		// infinite loop: 1200
		//int dealNumber = 28352;  
		
		// start from initial state (and make copy for later).
	    try {
	    	
	    	PrintWriter pwState;
	    	PrintWriter pwNMoves;
	    	Scorer scorer;
	    	
	    	if (Configuration.MAKE_DATA == true) {
	    		pwState = new PrintWriter(new FileWriter("trainingStateData.dat", true));
	    		pwNMoves = new PrintWriter(new FileWriter("trainingNMovesData.dat", true));
	    		scorer = new Scorer();
	    	}
			
			FreeCellState fcs = new FreeCellState();
			fcs.getInitialState(fileName);
			// Compute the solution.
			iDFS sd = new iDFS();	
			long time1 = System.currentTimeMillis();
			ArrayList<Move> st = sd.solve(fcs, new Scorer(), FreeCellState.comparator());
			long time2 = System.currentTimeMillis();
			System.out.println("time=" + (time2 - time1));

			if (st == null) {
		        JOptionPane jop = new JOptionPane();
		        JOptionPane.showMessageDialog(jop, "Xin lỗi, chương trình hiện tại " +
		        		"chưa tìm được cách giải cho trạng thái trò chơi này!");
				//System.gc();
			}
			//------------------------------------------------------------------
			/*JPanel pnl = new JPanel();
			pnl.setBounds(0,0,700,100);
			JButton next = new JButton("Next");
			JButton prev = new JButton("Pre");

			pnl.add(prev);
			pnl.add(next);
			prev.setEnabled(false);
			pnl.validate();
			frame.add(pnl);*/
			//------------------------------------------------------------------
			FreeCellState startState = fcs.clone();
			System.out.println(st.size());
			DefaultListModel dlm = new DefaultListModel();
			int nMoves = st.size();
			for (int i = 0; i < st.size(); i++){
				dlm.addElement(st.get(i));
				
				if (Configuration.MAKE_DATA == true) {
					String s = startState.keyToString();
					pwState.write(s + "\n");
					pwNMoves.write(scorer.eval(startState) + "\n");
					Move m = st.get(i);
					m.setAuto(false);
					m.execute(startState);
				}
				
			}
			
			if (Configuration.MAKE_DATA == true) {
				pwState.flush();
				pwState.close();
				pwNMoves.flush();
				pwNMoves.close();
			}
			
			Move end = new Move(0);
			dlm.addElement(end);
			
			list.setModel(dlm);
			list.setSelectedIndex(0);
			next.setEnabled(true);
			if (listener != null) {
				list.removeListSelectionListener(listener);
			}
			if(nextListener != null){
				next.removeActionListener(nextListener);
			}
			if(prevListener != null){
				prev.removeActionListener(prevListener);
			}			
			listener = new StateModifier(list, fcs, drawer, next, prev);
			list.addListSelectionListener(listener);
			//frame.setCursor(Cursor.getDefaultCursor());
			
			nextListener = new NextModifier(list, fcs, drawer, next);
			next.addActionListener(nextListener);
			prevListener = new PrevModifier(list, fcs, drawer, prev);
			prev.addActionListener(prevListener);
			
			frame.setCursor(Cursor.getDefaultCursor());
			
			drawer.setNode (fcs);
			drawer.repaint();
		} catch (Exception e1) {
			e1.printStackTrace();
			return;
		}

	}
	

}
