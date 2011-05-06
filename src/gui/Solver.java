package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
//import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import gui.controller.DealController;
import gui.view.CardImages;
import gui.view.CardImagesLoader;
import gui.view.FreeCellDrawing;

/**
 * Show a full solution to a board graphically.
 * 
 * @author George Heineman
 */
public class Solver {

	/** Card images. */
	public static CardImages cardImages;

	public static void main(String[] args) throws Exception {

		// solution found. Create GUI.
		final JFrame frame = new JFrame();
		// frame.setAlwaysOnTop(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {

			/** Once opened: load up the images. */
			public void windowOpened(WindowEvent e) {
				System.out.println("Loading card images...");
				cardImages = CardImagesLoader.getDeck(e.getWindow());
			}

		});

		frame.setSize(700, 700);
		// frame.setSize(1024, 720);
		JList list = new JList();

		// add widgets at proper location
		frame.setLayout(null);
		
		//------------------------------------------------------------------
		//2 buttons next and pre
		JPanel pnl = new JPanel();
		pnl.setBounds(0,0,700,100);
		JButton next = new JButton("Tiếp tục");
		JButton prev = new JButton("Trở lại");

		pnl.add(prev);
		pnl.add(next);
		next.setEnabled(false);
		prev.setEnabled(false);
		pnl.validate();
		frame.add(pnl);
		//------------------------------------------------------------------
		
		// bottom row
		FreeCellDrawing drawer = new FreeCellDrawing();
		drawer.setBounds(0, 100, 700, 600);
		drawer.setBackground(new java.awt.Color(0, 128, 0));
		frame.add(drawer);

		// Create the GUI and put it in the window with scrollbars.
		final JScrollPane scrollingPane = new JScrollPane(list);
		scrollingPane.setAutoscrolls(true);
		scrollingPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollingPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		scrollingPane.setBounds(700, 100, 500, 400);
		scrollingPane.setVisible(false);
		frame.add(scrollingPane);

		//======================================================================
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		JMenu fileMenu = new JMenu("Lựa chọn");
		menuBar.add(fileMenu);
		JMenu viewMenu = new JMenu("Hiển thị");
		menuBar.add(viewMenu);
		
		JMenuItem openAction = new JMenuItem("Chọn file");
		JMenuItem exitAction = new JMenuItem("Thoát");
		
		fileMenu.add(openAction);
		fileMenu.add(exitAction);
		
		JCheckBoxMenuItem checkAction = new JCheckBoxMenuItem("Danh sách các bước di chuyển");
		viewMenu.add(checkAction);
		openAction.addActionListener(new DealController(frame, drawer, list, next, prev));
		exitAction.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent arg0) {
	                System.exit(1);
	            }
	     });
		//Show move list check action listener
	    ActionListener aListener = new ActionListener() {
	        public void actionPerformed(ActionEvent event) {
	          AbstractButton aButton = (AbstractButton) event.getSource();
	          boolean selected = aButton.getModel().isSelected();
	          if (selected) {
	        	  frame.setSize(1240, 700);
	        	  scrollingPane.setVisible(true);
	          } else {
	        	  frame.setSize(700, 700);
	        	  scrollingPane.setVisible(false);
	          }
	        }
	      };
	      checkAction.addActionListener(aListener);
		//======================================================================
		// set up listeners and show everything
		frame.setVisible(true);
	}
}
