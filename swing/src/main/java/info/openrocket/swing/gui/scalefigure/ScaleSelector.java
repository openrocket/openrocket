package info.openrocket.swing.gui.scalefigure;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.EventObject;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.util.Icons;
import info.openrocket.swing.gui.widgets.IconButton;
import info.openrocket.core.util.StateChangeListener;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;

@SuppressWarnings("serial")
public class ScaleSelector {
	private static final Translator trans = Application.getTranslator();

	public interface ZoomModel {
		double getScale();
		boolean isFit();
		void setScale(double scale);
		void setFit();
		void addChangeListener(StateChangeListener listener);
		void removeChangeListener(StateChangeListener listener);
	}
    
	// Ready zoom settings
	private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.#%");
	private static final String SCALE_FIT_SUFFIX = trans.get("ScaleSelector.lbl.ZoomFitSuffix");

	private static final double[] SCALE_LEVELS = { 0.15, 0.25, 0.5, 0.75, 1.0, 1.5, 2.0 };
	private static final String[] SCALE_LABELS;
	static {
		SCALE_LABELS = new String[SCALE_LEVELS.length];
		for (int i = 0; i < SCALE_LEVELS.length; i++) {
			SCALE_LABELS[i] = formatScaleText(SCALE_LEVELS[i], SCALE_LEVELS[i] == 1.0);
		}
	}

	private final ZoomModel zoomModel;
	private final JComboBox<String> scaleSelectorCombo;
	private final JButton zoomOutButton;
	private final JButton zoomInButton;
	private final JButton zoomFitButton;
	private boolean updatingScaleSelectorText = false;
	private int comboTextWidth = -1;

	public ScaleSelector(ScaleScrollPane scroll) {
		this(new ZoomModel() {
			@Override
			public double getScale() {
				return scroll.getUserScale();
			}

			@Override
			public boolean isFit() {
				return scroll.isFitting();
			}

			@Override
			public void setScale(double scale) {
				scroll.setScaling(scale);
			}

			@Override
			public void setFit() {
				scroll.setFitting(true);
			}

			@Override
			public void addChangeListener(StateChangeListener listener) {
				scroll.getFigure().addChangeListener(listener);
			}

			@Override
			public void removeChangeListener(StateChangeListener listener) {
				scroll.getFigure().removeChangeListener(listener);
			}
		});
	}

	public ScaleSelector(ZoomModel zoomModel) {
		this.zoomModel = zoomModel;

		// Zoom out button
		zoomOutButton = new IconButton(Icons.ZOOM_OUT);
		zoomOutButton.setToolTipText(trans.get("ScaleSelector.btn.ZoomOut.ttip"));
		zoomOutButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final double oldScale = ScaleSelector.this.zoomModel.getScale();
				final double newScale = getNextLargerScale(oldScale);
				ScaleSelector.this.zoomModel.setScale(newScale);
				setZoomText();
			}
		});

		// Zoom level selector
		scaleSelectorCombo = new JComboBox<>(SCALE_LABELS)  {
			@Override
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				String currentText = getSelectedItem() != null ? getSelectedItem().toString() : "";
				d.width = Math.max(d.width, getRequiredComboWidth(currentText));
				return d;
			}
		};
		scaleSelectorCombo.setEditable(true);
		scaleSelectorCombo.setPrototypeDisplayValue(formatScaleText(1.0, true));
		scaleSelectorCombo.setSelectedItem(formatScaleText(1.0, true));	// Make sure the combobox can fit this text
		Dimension comboSize = scaleSelectorCombo.getPreferredSize();
		scaleSelectorCombo.setPreferredSize(comboSize);
		scaleSelectorCombo.setMinimumSize(comboSize);
		setZoomText();
		scaleSelectorCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (updatingScaleSelectorText) {
					return;
				}
				try {
					String text = (String) scaleSelectorCombo.getSelectedItem();
					if (text == null) return;
					boolean fitSelection = text.endsWith(SCALE_FIT_SUFFIX);
					text = text.replace(SCALE_FIT_SUFFIX, "").replace("%", "").trim();

					double n = Double.parseDouble(text);
					n /= 100;
					if (n <= 0.005)
						n = 0.005;

					if (fitSelection && Math.abs(n - 1.0) < 0.0001) {
						ScaleSelector.this.zoomModel.setFit();
					} else {
						ScaleSelector.this.zoomModel.setScale(n);
					}
					setZoomText();
				} catch (NumberFormatException ignore) {
				} finally {
					setZoomText();
				}
			}
		});
		zoomModel.addChangeListener(new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
				update();
			}
		});

		// Zoom in button
		zoomInButton = new IconButton(Icons.ZOOM_IN);
		zoomInButton.setToolTipText(trans.get("ScaleSelector.btn.ZoomIn.ttip"));
		zoomInButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				double scale = ScaleSelector.this.zoomModel.getScale();
				scale = getNextSmallerScale(scale);
				ScaleSelector.this.zoomModel.setScale(scale);
				update();
			}
		});

		// Zoom fit button
		zoomFitButton = new IconButton(Icons.ZOOM_RESET);
		zoomFitButton.setToolTipText(trans.get("ScaleSelector.btn.ZoomFit.ttip"));
		zoomFitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ScaleSelector.this.zoomModel.setFit();
				update();
			}
		});
	}

	public JPanel getAsPanel() {
		JPanel panel = new JPanel(new MigLayout("insets 0", "[][]0[]0[]", "[]"));
		panel.add(zoomOutButton);
		panel.add(scaleSelectorCombo, "wmin 120lp, growx");
		panel.add(zoomInButton);
		panel.add(zoomFitButton);

		return panel;
	}

	public JComboBox<String> getScaleSelectorCombo() {
		return scaleSelectorCombo;
	}

	public JButton getZoomOutButton() {
		return zoomOutButton;
	}

	public JButton getZoomInButton() {
		return zoomInButton;
	}

	public JButton getZoomFitButton() {
		return zoomFitButton;
	}

	private void setZoomText() {
		String text = formatScaleText(zoomModel.getScale(), zoomModel.isFit());
		if (!text.equals(scaleSelectorCombo.getSelectedItem())) {
			updatingScaleSelectorText = true;
			try {
				scaleSelectorCombo.setSelectedItem(text);
			} finally {
				updatingScaleSelectorText = false;
			}
		}
	}

	private static String formatScaleText(double scale, boolean fit) {
		String text = PERCENT_FORMAT.format(scale);
		if (fit) {
			text = text + SCALE_FIT_SUFFIX;
		}
		return text;
	}

	private int getRequiredComboWidth(String currentText) {
		FontMetrics fontMetrics = scaleSelectorCombo.getFontMetrics(scaleSelectorCombo.getFont());
		if (comboTextWidth < 0) {
			comboTextWidth = 0;
			for (String label : SCALE_LABELS) {
				comboTextWidth = Math.max(comboTextWidth, fontMetrics.stringWidth(label));
			}
			comboTextWidth = Math.max(comboTextWidth, fontMetrics.stringWidth(formatScaleText(1.0, true)));
		}
		int currentWidth = currentText != null ? fontMetrics.stringWidth(currentText) : 0;
		return Math.max(comboTextWidth, currentWidth) + 40;
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
			return Math.floor(currentScale + 1.05);
		}
		return currentScale * 1.5;
	}

	public void setEnabled(boolean b){
		zoomInButton.setEnabled(b);
		scaleSelectorCombo.setEnabled(b);
		zoomOutButton.setEnabled(b);
		zoomFitButton.setEnabled(b);
	}

	public void update(){
		setZoomText();
	}

}
