package net.sf.openrocket.gui.help.tours;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Named;

public class GuidedTourSelectionDialog extends JDialog {
	
	private static final Translator trans = Application.getTranslator();
	
	private static GuidedTourSelectionDialog instance = null;
	
	
	private final SlideSetManager slideSetManager;
	private final List<String> tourNames;
	
	private SlideShowDialog slideShowDialog;
	
	private JList tourList;
	private JEditorPane tourDescription;
	private JLabel tourLength;
	
	
	public GuidedTourSelectionDialog(Window parent) {
		super(parent, trans.get("title"), ModalityType.MODELESS);
		
		slideSetManager = SlideSetManager.getSlideSetManager();
		tourNames = slideSetManager.getSlideSetNames();
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		panel.add(new StyledLabel(trans.get("lbl.selectTour"), Style.BOLD), "spanx, wrap rel");
		
		tourList = new JList(new TourListModel());
		tourList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tourList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateText();
			}
		});
		tourList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					startTour();
				}
			}
		});
		panel.add(new JScrollPane(tourList), "grow, gapright unrel, w 200lp, h 250lp");
		
		
		
		//  Sub-panel containing description and start button
		JPanel sub = new JPanel(new MigLayout("fill, ins 0"));
		sub.add(new StyledLabel(trans.get("lbl.description"), -1), "wrap rel");
		
		tourDescription = new JEditorPane("text/html", "");
		tourDescription.setEditable(false);
		StyleSheet ss = slideSetManager.getSlideSet(tourNames.get(0)).getStyleSheet();
		((HTMLDocument) tourDescription.getDocument()).getStyleSheet().addStyleSheet(ss);
		sub.add(new JScrollPane(tourDescription), "grow, wrap rel");
		
		tourLength = new StyledLabel(-1);
		sub.add(tourLength, "wrap unrel");
		
		JButton start = new JButton(trans.get("btn.start"));
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startTour();
			}
		});
		sub.add(start, "growx");
		
		panel.add(sub, "grow, wrap para, w 350lp, h 250lp");
		
		
		
		JButton close = new JButton(trans.get("button.close"));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GuidedTourSelectionDialog.this.dispose();
			}
		});
		panel.add(close, "spanx, right");
		
		this.add(panel);
		GUIUtil.setDisposableDialogOptions(this, close);
		GUIUtil.rememberWindowPosition(this);
		tourList.setSelectedIndex(0);
	}
	
	
	private void startTour() {
		SlideSet ss = getSelectedSlideSet();
		if (ss == null) {
			return;
		}
		
		if (slideShowDialog != null && !slideShowDialog.isVisible()) {
			closeTour();
		}
		
		if (slideShowDialog == null) {
			slideShowDialog = new SlideShowDialog(this);
		}
		
		slideShowDialog.setSlideSet(ss, 0);
		slideShowDialog.setVisible(true);
	}
	
	
	private void closeTour() {
		if (slideShowDialog != null) {
			slideShowDialog.dispose();
			slideShowDialog = null;
		}
	}
	
	
	private void updateText() {
		SlideSet ss = getSelectedSlideSet();
		if (ss != null) {
			tourDescription.setText(ss.getDescription());
			tourLength.setText(trans.get("lbl.length") + " " + ss.getSlideCount());
		} else {
			tourDescription.setText("");
			tourLength.setText(trans.get("lbl.length"));
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private SlideSet getSelectedSlideSet() {
		return ((Named<SlideSet>) tourList.getSelectedValue()).get();
	}
	
	private class TourListModel extends AbstractListModel {
		
		@Override
		public Object getElementAt(int index) {
			String name = tourNames.get(index);
			SlideSet set = slideSetManager.getSlideSet(name);
			return new Named<SlideSet>(set, set.getTitle());
		}
		
		@Override
		public int getSize() {
			return tourNames.size();
		}
		
	}
	
	
	public static void showDialog(Window parent) {
		if (instance != null && instance.isVisible()) {
			instance.setVisible(true);
			instance.toFront();
		} else {
			instance = new GuidedTourSelectionDialog(parent);
			instance.setVisible(true);
		}
	}
	
}
