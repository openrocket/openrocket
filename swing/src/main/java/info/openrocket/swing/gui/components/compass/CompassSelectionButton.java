package info.openrocket.swing.gui.components.compass;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.Resettable;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.components.FlatButton;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Chars;
import info.openrocket.core.util.MathUtil;
import info.openrocket.swing.gui.widgets.SelectColorButton;


/**
 * A button that displays a current compass direction and opens a popup to edit
 * the value when clicked.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
@SuppressWarnings("serial")
public class CompassSelectionButton extends FlatButton implements Resettable {
	
	private static final Translator trans = Application.getTranslator();
	
	private static final int POPUP_COMPASS_SIZE = 200;
	private static final double SECTOR = 45;
	
	private static int minWidth = -1;
	

	@SuppressWarnings("hiding")
	private final DoubleModel model;
	
	private final ChangeListener listener;
	
	private JPopupMenu popup;
	
	
	public CompassSelectionButton(final DoubleModel model) {
		this.model = model;
		
		JPanel panel = new JPanel(new MigLayout("fill, ins 0"));
		panel.setOpaque(false);
		
		CompassPointer pointer = new CompassPointer(model);
		pointer.setPreferredSize(new Dimension(24, 24));
		pointer.setMarkerFont(null);
		pointer.setPointerArrow(false);
		pointer.setPointerWidth(0.45f);
		pointer.setScaler(1.0f);
		panel.add(pointer, "gapright rel");
		

		final JLabel label = new JLabel();
		label.setText(getLabel(model.getValue()));
		panel.add(label);
		
		listener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				label.setText(getLabel(model.getValue()));
			}
		};
		model.addChangeListener(listener);
		

		if (minWidth < 0) {
			calculateMinWidth();
			label.setMinimumSize(new Dimension(minWidth, 0));
		}
		

		this.add(panel);
		
		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openPopup();
			}
		});
	}
	
	


	private String getLabel(double value) {
		String str;
		
		value = MathUtil.reduce2Pi(value);
		value = Math.toDegrees(value);
		str = "" + Math.round(value) + Chars.DEGREE + " (";
		
		if (value <= 0.5 * SECTOR || value >= 7.5 * SECTOR) {
			str += trans.get("lbl.N");
		} else if (value <= 1.5 * SECTOR) {
			str += trans.get("lbl.NE");
		} else if (value <= 2.5 * SECTOR) {
			str += trans.get("lbl.E");
		} else if (value <= 3.5 * SECTOR) {
			str += trans.get("lbl.SE");
		} else if (value <= 4.5 * SECTOR) {
			str += trans.get("lbl.S");
		} else if (value <= 5.5 * SECTOR) {
			str += trans.get("lbl.SW");
		} else if (value <= 6.5 * SECTOR) {
			str += trans.get("lbl.W");
		} else {
			str += trans.get("lbl.NW");
		}
		
		str += ")";
		return str;
	}
	
	
	private void openPopup() {
		if (popup == null) {
			popup = new JPopupMenu();
			

			final JPanel panel = new JPanel(new MigLayout("fill"));
			
			final CompassPointer rose = new CompassSelector(model);
			rose.setPreferredSize(new Dimension(POPUP_COMPASS_SIZE, POPUP_COMPASS_SIZE));
			panel.add(rose, "spany, gapright unrel");
			
			panel.add(new JPanel(), "growy, wrap");
			
			JSpinner spin = new JSpinner(model.getSpinnerModel());
			spin.setEditor( new SpinnerEditor( spin ) );
			panel.add(spin, "wmin 50lp, growx, gapright 0, aligny bottom");
			
			panel.add(new JLabel("" + Chars.DEGREE), "wrap para");
			
			JButton close = new SelectColorButton("OK");
			close.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					popup.setVisible(false);
				}
			});
			panel.add(close, "span 2, growx, wrap");
			
			panel.add(new JPanel(), "growy, wrap");
			
			popup.add(panel);
			popup.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		}
		
		popup.pack();
		
		Dimension popupSize = popup.getPreferredSize();
		Dimension buttonSize = this.getSize();
		
		int posX = buttonSize.width / 2 - popupSize.width / 2;
		int posY = buttonSize.height / 2 - popupSize.height / 2;
		popup.show(this, posX, posY);
	}
	
	private void calculateMinWidth() {
		JLabel label = new JLabel();
		int max = 0;
		for (double deg = 0; deg < 360; deg += 0.99999999999) {
			label.setText(getLabel(Math.toRadians(deg)));
			int w = label.getPreferredSize().width;
			if (w > max) {
				max = w;
			}
		}
		minWidth = max + 1;
	}
	
	


	@Override
	public void resetModel() {
		model.removeChangeListener(listener);
	}
	
}
