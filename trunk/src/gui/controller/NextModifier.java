package gui.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import gui.view.FreeCellDrawing;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;

import core.FreeCellState;
import core.Move;

public class NextModifier implements ActionListener{
	/** Controlled JList. */
	JList list;
	
	/** Current board state. */
	FreeCellState initial;
	
	/** FreeCellDrawing entity. */
	FreeCellDrawing drawer;
	
	JButton nextBut;
	
	public NextModifier (JList list, FreeCellState node, FreeCellDrawing drawer, JButton nextBut) {
		this.list = list;
		
		// initial state
		this.initial = (FreeCellState) node.clone();
		this.drawer = drawer;
		this.nextBut = nextBut;
	}
	public void setNode(FreeCellState fcn) {
		initial = fcn;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		// must find the one that is selected
		int idx = list.getSelectedIndex();
		DefaultListModel dlm = (DefaultListModel) list.getModel();
		FreeCellState node = (FreeCellState) initial.clone();
		if(idx >= dlm.size()-2)
			nextBut.setEnabled(false);
		
		for (int i = 0; i <= idx; i++) {
			Move move = (Move) dlm.get(i);
			move.setAuto(false);
			move.execute(node);
		}
		list.setSelectedIndex(idx+1);
		drawer.setNode(node);
		drawer.repaint();
		
	}


}
