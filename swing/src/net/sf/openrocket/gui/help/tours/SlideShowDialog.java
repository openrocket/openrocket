package net.sf.openrocket.gui.help.tours;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Chars;

public class SlideShowDialog extends JDialog {
	
	private static final Logger log = LoggerFactory.getLogger(SlideShowDialog.class);
	private static final Translator trans = Application.getTranslator();
	
	private SlideShowComponent slideShowComponent;
	private SlideSet slideSet;
	private int position;
	
	private JButton nextButton;
	private JButton prevButton;
	private JButton closeButton;
	
	
	public SlideShowDialog(Window parent) {
		super(parent, ModalityType.MODELESS);
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		slideShowComponent = new SlideShowComponent();
		slideShowComponent.addHyperlinkListener(new SlideShowLinkListener(parent));
		panel.add(slideShowComponent, "spanx, grow, wrap para");
		
		
		JPanel sub = new JPanel(new MigLayout("ins 0, fill"));
		
		prevButton = new JButton(Chars.LEFT_ARROW + " " + trans.get("btn.prev"));
		prevButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Clicked previous button");
				setPosition(position - 1);
			}
		});
		sub.add(prevButton, "left");
		
		
		
		nextButton = new JButton(trans.get("btn.next") + " " + Chars.RIGHT_ARROW);
		nextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Clicked next button");
				setPosition(position + 1);
			}
		});
		sub.add(nextButton, "left, gapleft para");
		
		
		sub.add(new JPanel(), "growx");
		
		
		closeButton = new JButton(trans.get("button.close"));
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SlideShowDialog.this.dispose();
			}
		});
		sub.add(closeButton, "right");
		
		
		panel.add(sub, "growx");
		
		this.add(panel);
		updateEnabled();
		addKeyActions();
		GUIUtil.setDisposableDialogOptions(this, nextButton);
		nextButton.grabFocus();
		GUIUtil.rememberWindowPosition(this);
		GUIUtil.rememberWindowSize(this);
		//		this.setAlwaysOnTop(true);
	}
	
	public void setSlideSet(SlideSet slideSet, int position) {
		this.slideSet = slideSet;
		this.setTitle(slideSet.getTitle() + " " + Chars.EMDASH + " OpenRocket");
		slideShowComponent.setStyleSheet(slideSet.getStyleSheet());
		setPosition(position);
	}
	
	public void setPosition(int position) {
		if (this.slideSet == null) {
			throw new BugException("setPosition called when slideSet is null");
		}
		
		if (position < 0 || position >= slideSet.getSlideCount()) {
			throw new BugException("position exceeds slide count, position=" + position +
					" slideCount=" + slideSet.getSlideCount());
		}
		
		this.position = position;
		slideShowComponent.setSlide(slideSet.getSlide(position));
		updateEnabled();
	}
	
	
	private void updateEnabled() {
		if (slideSet == null) {
			prevButton.setEnabled(false);
			nextButton.setEnabled(false);
			return;
		}
		
		prevButton.setEnabled(position > 0);
		nextButton.setEnabled(position < slideSet.getSlideCount() - 1);
	}
	
	
	
	
	
	private void addKeyActions() {
		Action next = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
				log.info(Markers.USER_MARKER, "Key action for next slide");
				if (position < slideSet.getSlideCount() - 1) {
					setPosition(position + 1);
				}
			}
		};
		
		Action previous = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
				log.info(Markers.USER_MARKER, "Key action for previous slide");
				if (position > 0) {
					setPosition(position - 1);
				}
			}
		};
		
		String nextKey = "slide:next";
		String prevKey = "slide:previous";
		
		JRootPane root = this.getRootPane();
		root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), nextKey);
		root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0), nextKey);
		root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), prevKey);
		root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0), prevKey);
		
		root.getActionMap().put(nextKey, next);
		root.getActionMap().put(prevKey, previous);
	}
	
	
	
}
