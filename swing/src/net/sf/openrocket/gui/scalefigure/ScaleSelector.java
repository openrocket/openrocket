package net.sf.openrocket.gui.scalefigure;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.EventObject;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.util.StateChangeListener;

@SuppressWarnings("serial")
public class ScaleSelector extends JPanel {

    public static final double MINIMUM_ZOOM =    0.01; // ==      1 %
    public static final double MAXIMUM_ZOOM = 1000.00; // == 10,000 %
    
	// Ready zoom settings
	private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.#%");

	private static final double[] SCALE_LEVELS = { 0.15, 0.25, 0.5, 0.75, 1.0, 1.5, 2.0 };
	private static final String SCALE_FIT = "Fit"; // trans.get("ScaleSelector.something.something");
	private static final String[] SCALE_LABELS;
	static {
		SCALE_LABELS = new String[SCALE_LEVELS.length + 1];
		for (int i = 0; i < SCALE_LEVELS.length; i++)
			SCALE_LABELS[i] = PERCENT_FORMAT.format(SCALE_LEVELS[i]);
		SCALE_LABELS[SCALE_LABELS.length - 1] = SCALE_FIT;
	}

	private final ScaleScrollPane scrollPane;
	private JComboBox<String> scaleSelector;

	public ScaleSelector(ScaleScrollPane scroll) {
		super(new MigLayout());

		this.scrollPane = scroll;

		// Zoom out button
		JButton button = new JButton(Icons.ZOOM_OUT);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final double oldScale = scrollPane.getUserScale();
				final double newScale = getNextLargerScale(oldScale);
				scrollPane.setScaling(newScale);
				setZoomText();
			}
		});
		add(button, "gap");

		// Zoom level selector
		String[] settings = SCALE_LABELS;
		
		scaleSelector = new JComboBox<>(settings);
		scaleSelector.setEditable(true);
		setZoomText();
		scaleSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					String text = (String) scaleSelector.getSelectedItem();
					text = text.replaceAll("%", "").trim();

					if (text.toLowerCase(Locale.getDefault()).startsWith(SCALE_FIT.toLowerCase(Locale.getDefault()))){
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
		add(scaleSelector, "gap rel");

		// Zoom in button
		button = new JButton(Icons.ZOOM_IN);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				double scale = scrollPane.getUserScale();
				scale = getNextSmallerScale(scale);
				scrollPane.setScaling(scale);
				setZoomText();
			}
		});
		add(button, "gapleft rel");

	}

	private void setZoomText() {
	    final double userScale = scrollPane.getUserScale();
	    String text = PERCENT_FORMAT.format(userScale);
		if (scrollPane.isFitting()) {
			text = "Fit (" + text + ")";
		}
		if (!text.equals(scaleSelector.getSelectedItem()))
			scaleSelector.setSelectedItem(text);
	}

	private static double getNextLargerScale(final double currentScale) {
		int i;
		for (i = 0; i < SCALE_LEVELS.length - 1; i++) {
			if (currentScale > SCALE_LEVELS[i] + 0.05 && currentScale < SCALE_LEVELS[i + 1] + 0.05)
				return SCALE_LEVELS[i];
		}
		if (currentScale > SCALE_LEVELS[SCALE_LEVELS.length / 2]) {
			// scale is large, drop to next lowest full 100%
			double nextScale = Math.ceil(currentScale - 1.05);
			return Math.max(nextScale, SCALE_LEVELS[i]);
		}
		// scale is small
		return currentScale / 1.5;
	}

	private static double getNextSmallerScale(final double currentScale) {
		int i;
		for (i = 0; i < SCALE_LEVELS.length - 1; i++) {
			if (currentScale > SCALE_LEVELS[i] - 0.05 && currentScale < SCALE_LEVELS[i + 1] - 0.05)
				return SCALE_LEVELS[i + 1];
		}
		if (currentScale > SCALE_LEVELS[SCALE_LEVELS.length / 2]) {
			// scale is large, give next full 100%
			double nextScale = Math.floor(currentScale + 1.05);
			return nextScale;
		}
		return currentScale * 1.5;
	}

	@Override
	public void setEnabled(boolean b){
		for ( Component c : getComponents() ){
			c.setEnabled(b);
		}
		super.setEnabled(b);
	}

}
