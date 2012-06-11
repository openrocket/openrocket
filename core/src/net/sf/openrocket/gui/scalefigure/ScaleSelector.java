package net.sf.openrocket.gui.scalefigure;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.EventObject;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.util.StateChangeListener;

public class ScaleSelector extends JPanel {
	
	// Ready zoom settings
	private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.#%");
	
	private static final double[] ZOOM_LEVELS = { 0.15, 0.25, 0.5, 0.75, 1.0, 1.5, 2.0 };
	private static final String ZOOM_FIT = "Fit";
	private static final String[] ZOOM_SETTINGS;
	static {
		ZOOM_SETTINGS = new String[ZOOM_LEVELS.length + 1];
		for (int i = 0; i < ZOOM_LEVELS.length; i++)
			ZOOM_SETTINGS[i] = PERCENT_FORMAT.format(ZOOM_LEVELS[i]);
		ZOOM_SETTINGS[ZOOM_SETTINGS.length - 1] = ZOOM_FIT;
	}
	
	
	private final ScaleScrollPane scrollPane;
	private JComboBox zoomSelector;
	
	
	public ScaleSelector(ScaleScrollPane scroll) {
		super(new MigLayout());
		
		this.scrollPane = scroll;
		
		// Zoom out button
		JButton button = new JButton(Icons.ZOOM_OUT);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				double scale = scrollPane.getScaling();
				scale = getPreviousScale(scale);
				scrollPane.setScaling(scale);
			}
		});
		add(button, "gap");
		
		// Zoom level selector
		String[] settings = ZOOM_SETTINGS;
		if (!scrollPane.isFittingAllowed()) {
			settings = Arrays.copyOf(settings, settings.length - 1);
		}
		
		zoomSelector = new JComboBox(settings);
		zoomSelector.setEditable(true);
		setZoomText();
		zoomSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					String text = (String) zoomSelector.getSelectedItem();
					text = text.replaceAll("%", "").trim();
					
					if (text.toLowerCase(Locale.getDefault()).startsWith(ZOOM_FIT.toLowerCase(Locale.getDefault())) &&
							scrollPane.isFittingAllowed()) {
						scrollPane.setFitting(true);
						setZoomText();
						return;
					}
					
					double n = Double.parseDouble(text);
					n /= 100;
					if (n <= 0.005)
						n = 0.005;
					
					scrollPane.setScaling(n);
					setZoomText();
				} catch (NumberFormatException ignore) {
				} finally {
					setZoomText();
				}
			}
		});
		scrollPane.getFigure().addChangeListener(new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
				setZoomText();
			}
		});
		add(zoomSelector, "gap rel");
		
		
		// Zoom in button
		button = new JButton(Icons.ZOOM_IN);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				double scale = scrollPane.getScaling();
				scale = getNextScale(scale);
				scrollPane.setScaling(scale);
			}
		});
		add(button, "gapleft rel");
		
	}
	
	
	
	private void setZoomText() {
		String text;
		double zoom = scrollPane.getScaling();
		text = PERCENT_FORMAT.format(zoom);
		if (scrollPane.isFitting()) {
			text = "Fit (" + text + ")";
		}
		if (!text.equals(zoomSelector.getSelectedItem()))
			zoomSelector.setSelectedItem(text);
	}
	
	
	
	private double getPreviousScale(double scale) {
		int i;
		for (i = 0; i < ZOOM_LEVELS.length - 1; i++) {
			if (scale > ZOOM_LEVELS[i] + 0.05 && scale < ZOOM_LEVELS[i + 1] + 0.05)
				return ZOOM_LEVELS[i];
		}
		if (scale > ZOOM_LEVELS[ZOOM_LEVELS.length / 2]) {
			// scale is large, drop to next lowest full 100%
			scale = Math.ceil(scale - 1.05);
			return Math.max(scale, ZOOM_LEVELS[i]);
		}
		// scale is small
		return scale / 1.5;
	}
	
	
	private double getNextScale(double scale) {
		int i;
		for (i = 0; i < ZOOM_LEVELS.length - 1; i++) {
			if (scale > ZOOM_LEVELS[i] - 0.05 && scale < ZOOM_LEVELS[i + 1] - 0.05)
				return ZOOM_LEVELS[i + 1];
		}
		if (scale > ZOOM_LEVELS[ZOOM_LEVELS.length / 2]) {
			// scale is large, give next full 100%
			scale = Math.floor(scale + 1.05);
			return scale;
		}
		return scale * 1.5;
	}
	
	@Override
	public void setEnabled(boolean b){
		for ( Component c : getComponents() ){
			c.setEnabled(b);
		}
		super.setEnabled(b);
	}
	
}
