package net.sf.openrocket.gui.help.tours;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Chars;

public class SlideShowDialog extends JDialog {
	
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
		panel.add(slideShowComponent, "spanx, grow, wrap para");
		

		JPanel sub = new JPanel(new MigLayout("ins 0, fill"));
		
		prevButton = new JButton(Chars.LEFT_ARROW + " " + trans.get("btn.prev"));
		prevButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setPosition(position - 1);
			}
		});
		sub.add(prevButton, "left");
		


		nextButton = new JButton(trans.get("btn.next") + " " + Chars.RIGHT_ARROW);
		nextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
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
		GUIUtil.setDisposableDialogOptions(this, nextButton);
		this.setAlwaysOnTop(true);
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
	
	
	public static void main(String[] args) throws Exception {
		
		Locale.setDefault(new Locale("de", "DE", ""));
		
		SlideSetManager manager = new SlideSetManager("datafiles/tours");
		manager.load();
		
		final SlideSet set = manager.getSlideSet("test.tour");
		
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				
				SlideShowDialog ssd = new SlideShowDialog(null);
				
				ssd.slideShowComponent.addHyperlinkListener(new HyperlinkListener() {
					@Override
					public void hyperlinkUpdate(HyperlinkEvent e) {
						System.out.println("Hyperlink event: " + e);
						System.out.println("Event type: " + e.getEventType());
						System.out.println("Description: " + e.getDescription());
						System.out.println("URL: " + e.getURL());
						System.out.println("Source element: " + e.getSourceElement());
						
					}
				});
				
				ssd.setSize(500, 500);
				ssd.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				ssd.setVisible(true);
				
				ssd.setSlideSet(set, 0);
			}
		});
	}
	

}
