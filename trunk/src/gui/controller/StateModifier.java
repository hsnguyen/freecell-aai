package gui.controller;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import core.Move;

import core.FreeCellState;
import gui.view.FreeCellDrawing;

/**
 * Processes GUI events over the JList to ensure the current board represents
 * the state BEFORE the final selected item.
 * <p>
 * Maintains invariant that drawer shows the state of board PRIOR to invoking the
 * selected move.
 * 
 * @author George Heineman
 */
public class StateModifier implements ListSelectionListener {

	/** Controlled JList. */
	JList list;
	
	/** Current board state. */
	FreeCellState initial;
	
	/** FreeCellDrawing entity. */
	FreeCellDrawing drawer;
	
	JButton next, prev;
	public StateModifier (JList list, FreeCellState node, FreeCellDrawing drawer,
						JButton next, JButton prev) {
		this.list = list;
		
		// initial state
		this.initial = (FreeCellState) node.clone();
		this.drawer = drawer;
		this.next = next;
		this.prev = prev;
	}
	

	public void setNode(FreeCellState fcn) {
		initial = fcn;
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		// must find the one that is selected
		int idx = list.getSelectedIndex();
		DefaultListModel dlm = (DefaultListModel) list.getModel();
		FreeCellState node = (FreeCellState) initial.clone();
		if(idx > 0) prev.setEnabled(true);
		else prev.setEnabled(false);
		if(idx < dlm.size()-1) next.setEnabled(true);
		else next.setEnabled(false);
		for (int i = 0; i < idx; i++) {
			Move move = (Move) dlm.get(i);
			//if (move.isValid(node)) {
			//	move.execute(node);
			//} else {
			//	System.out.println("INVALID MOVE!");
			//}
			move.setAuto(false);
			move.execute(node);
		}
		//list.setSelectedIndex(idx);
		drawer.setNode(node);
		drawer.repaint();

	}


}
