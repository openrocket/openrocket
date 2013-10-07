package net.sf.openrocket.gui.widgets;
/* -------------------------------------------------------------------
 * GeoVISTA Center (Penn State, Dept. of Geography)
 *
 * Java source file for the class MultiSliderUI
 *
 * Copyright (c), 1999 - 2002, Masahiro Takatsuka and GeoVISTA Center
 * All Rights Researved.
 *
 * Original Author: Masahiro Takatsuka
 * $Author: eytanadar $
 *
 * $Date: 2005/10/05 20:19:52 $
 *
 *
 * Reference:		Document no:
 * ___				___
 *
 * To Do:
 * ___
 *
------------------------------------------------------------------- */

/* --------------------------- Package ---------------------------- */
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.plaf.metal.MetalSliderUI;

/*====================================================================
  Implementation of class MultiSliderUI
  ====================================================================*/
/***
 * A Basic L&F implementation of SliderUI.
 *
 * @version $Revision: 1.1 $
 * @author Masahiro Takatsuka (masa@jbeans.net)
 * @see MetalSliderUI
 */

class MultiSliderUI extends BasicSliderUI {
	private Rectangle[] thumbRects = null;

	private int thumbCount;
	transient private int currentIndex = 0;

	transient private boolean isDragging;
	transient private int[] minmaxIndices = new int[2];

	/***
	 * ComponentUI Interface Implementation methods
	 */
	public static ComponentUI createUI(JComponent b)    {
		return new MultiSliderUI();
	}

	/***
	 * Construct a new MultiSliderUI object.
	 */
	public MultiSliderUI()   {
		super(null);
	}

	int getTrackBuffer() {
		return this.trackBuffer;
	}

	/***
	 * Sets the number of Thumbs.
	 */
	public void setThumbCount(int count) {
		this.thumbCount = count;
	}

	/***
	 * Returns the index number of the thumb currently operated.
	 */
	protected int getCurrentIndex() {
		return this.currentIndex;
	}

	public void installUI(JComponent c)   {
		this.thumbRects = new Rectangle[this.thumbCount];
		for (int i = 0; i < this.thumbCount; i++) {
			this.thumbRects[i] = new Rectangle();
		}
		this.currentIndex = 0;
		if (this.thumbCount > 0) {
			thumbRect = this.thumbRects[this.currentIndex];
		}
		super.installUI(c);
	}

	public void uninstallUI(JComponent c) {
		super.uninstallUI(c);
		for (int i = 0; i < this.thumbCount; i++) {
			this.thumbRects[i] = null;
		}
		this.thumbRects = null;
	}

	protected void installListeners( JSlider slider ) {
		slider.addMouseListener(trackListener);
		slider.addMouseMotionListener(trackListener);
		slider.addFocusListener(focusListener);
		slider.addComponentListener(componentListener);
		slider.addPropertyChangeListener( propertyChangeListener );
		for (int i = 0; i < this.thumbCount; i++) {
			((MultiSlider)slider).getModelAt(i).addChangeListener(changeListener);
		}
	}

	protected void uninstallListeners( JSlider slider ) {
		slider.removeMouseListener(trackListener);
		slider.removeMouseMotionListener(trackListener);
		slider.removeFocusListener(focusListener);
		slider.removeComponentListener(componentListener);
		slider.removePropertyChangeListener( propertyChangeListener );
		for (int i = 0; i < this.thumbCount; i++) {
			BoundedRangeModel model = ((MultiSlider)slider).getModelAt(i);
			if (model != null) {
				model.removeChangeListener(changeListener);
			}
		}
	}

	protected void calculateThumbSize() {
		Dimension size = getThumbSize();
		for (int i = 0; i < this.thumbCount; i++) {
			this.thumbRects[i].setSize(size.width, size.height);
		}
		thumbRect.setSize(size.width, size.height);
	}

	protected void calculateThumbLocation() {
		MultiSlider slider = (MultiSlider) this.slider;
		int majorTickSpacing = slider.getMajorTickSpacing();
		int minorTickSpacing = slider.getMinorTickSpacing();
		int tickSpacing = 0;

		if (minorTickSpacing > 0) {
			tickSpacing = minorTickSpacing;
		} else if (majorTickSpacing > 0) {
			tickSpacing = majorTickSpacing;
		}
		for (int i = 0; i < this.thumbCount; i++) {
			if (slider.getSnapToTicks()) {
				int sliderValue = slider.getValueAt(i);
				int snappedValue = sliderValue;
				if (tickSpacing != 0) {
					// If it's not on a tick, change the value
					if ((sliderValue - slider.getMinimum()) % tickSpacing != 0 ) {
						float temp = (float)(sliderValue - slider.getMinimum()) / (float)tickSpacing;
						int whichTick = Math.round(temp);
						snappedValue = slider.getMinimum() + (whichTick * tickSpacing);
					}

					if( snappedValue != sliderValue ) {
						slider.setValueAt(i, snappedValue);
					}
				}
			}

			if (slider.getOrientation() == JSlider.HORIZONTAL) {
				int valuePosition = xPositionForValue(slider.getValueAt(i));
				this.thumbRects[i].x = valuePosition - (this.thumbRects[i].width / 2);
				this.thumbRects[i].y = trackRect.y;
			} else {
				int valuePosition = yPositionForValue(slider.getValueAt(i));
				this.thumbRects[i].x = trackRect.x;
				this.thumbRects[i].y = valuePosition - (this.thumbRects[i].height / 2);
			}
		}
	}

	public void paint(Graphics g, JComponent c) {
		recalculateIfInsetsChanged();
		recalculateIfOrientationChanged();
		Rectangle clip = g.getClipBounds();

		if (slider.getPaintTrack() && clip.intersects(trackRect)) {
			paintTrack( g );
		}
		if (slider.getPaintTicks() && clip.intersects(tickRect)) {
			paintTicks( g );
		}
		if (slider.getPaintLabels() && clip.intersects(labelRect)) {
			paintLabels( g );
		}
		if (slider.hasFocus() && clip.intersects(focusRect)) {
			paintFocus( g );
		}

		// first paint unfocused thumbs.
		for (int i = 0; i < this.thumbCount; i++) {
			if (i != this.currentIndex) {
				if (clip.intersects(this.thumbRects[i])) {
					thumbRect = this.thumbRects[i];
					paintThumb(g);
				}
			}
		}
		// then paint currently focused thumb.
		if (clip.intersects(this.thumbRects[this.currentIndex])) {
			thumbRect = this.thumbRects[this.currentIndex];
			paintThumb(g);
		}
	}

	public void paintThumb(Graphics g)  {
		super.paintThumb(g);
	}

	public void paintTrack(Graphics g)  {
		super.paintTrack(g);
	}

	public void scrollByBlock(int direction) {
		synchronized(slider) {
			int oldValue = ((MultiSlider)slider).getValueAt(this.currentIndex);
			int blockIncrement = slider.getMaximum() / 10;
			int delta = blockIncrement * ((direction > 0) ? POSITIVE_SCROLL : NEGATIVE_SCROLL);
			((MultiSlider)slider).setValueAt(this.currentIndex, oldValue + delta);
		}
	}

	public void scrollByUnit(int direction) {
		synchronized(slider) {
			int oldValue = ((MultiSlider)slider).getValueAt(this.currentIndex);
			int delta = 1 * ((direction > 0) ? POSITIVE_SCROLL : NEGATIVE_SCROLL);
			((MultiSlider)slider).setValueAt(this.currentIndex, oldValue + delta);
		}
	}

	protected TrackListener createTrackListener( JSlider slider ) {
		return new MultiTrackListener();
	}

	/***
	 * Track Listener Class tracks mouse movements.
	 */
	class MultiTrackListener extends BasicSliderUI.TrackListener {
		int _trackTop;
		int _trackBottom;
		int _trackLeft;
		int _trackRight;
		transient private int[] firstXY = new int[2];

		/***
		 * If the mouse is pressed above the "thumb" component
		 * then reduce the scrollbars value by one page ("page up"),
		 * otherwise increase it by one page.  If there is no
		 * thumb then page up if the mouse is in the upper half
		 * of the track.
		 */
		public void mousePressed(MouseEvent e) {
			int[] neighbours = new int[2];
			boolean bounded = ((MultiSlider)slider).isBounded();
			if (!slider.isEnabled()) {
				return;
			}

			currentMouseX = e.getX();
			currentMouseY = e.getY();
			firstXY[0] = currentMouseX;
			firstXY[1] = currentMouseY;

			slider.requestFocus();
			// Clicked in the Thumb area?
			minmaxIndices[0] = -1;
			minmaxIndices[1] = -1;
			for (int i = 0; i < MultiSliderUI.this.thumbCount; i++) {
				if (MultiSliderUI.this.thumbRects[i].contains(currentMouseX, currentMouseY)) {
					if (minmaxIndices[0] == -1) {
						minmaxIndices[0] = i;
						MultiSliderUI.this.currentIndex = i;
					}
					if (minmaxIndices[1] < i) {
						minmaxIndices[1] = i;
					}
					switch (slider.getOrientation()) {
					case JSlider.VERTICAL:
						offset = currentMouseY - MultiSliderUI.this.thumbRects[i].y;
						break;
					case JSlider.HORIZONTAL:
						offset = currentMouseX - MultiSliderUI.this.thumbRects[i].x;
						break;
					}
					MultiSliderUI.this.isDragging = true;
					thumbRect = MultiSliderUI.this.thumbRects[i];
					if (bounded) {
						neighbours[0] = ((i - 1) < 0) ? -1 : (i - 1);
						neighbours[1] = ((i + 1) >= MultiSliderUI.this.thumbCount) ? -1 : (i + 1);
						//findClosest(currentMouseX, currentMouseY, neighbours, i);
					} else {
						MultiSliderUI.this.currentIndex = i;
						((MultiSlider)slider).setValueIsAdjustingAt(i, true);
						neighbours[0] = -1;
						neighbours[1] = -1;
					}
					setThumbBounds(neighbours);
					//return;
				}
			}
			if (minmaxIndices[0] > -1) {
				return;
			}

			MultiSliderUI.this.currentIndex = findClosest(currentMouseX, currentMouseY, neighbours, -1);
			thumbRect = MultiSliderUI.this.thumbRects[MultiSliderUI.this.currentIndex];
			MultiSliderUI.this.isDragging = false;
			((MultiSlider)slider).setValueIsAdjustingAt(MultiSliderUI.this.currentIndex, true);

			Dimension sbSize = slider.getSize();
			int direction = POSITIVE_SCROLL;

			switch (slider.getOrientation()) {
			case JSlider.VERTICAL:
				if (thumbRect.isEmpty()) {
					int scrollbarCenter = sbSize.height / 2;
					if (!drawInverted()) {
						direction = (currentMouseY < scrollbarCenter) ? POSITIVE_SCROLL : NEGATIVE_SCROLL;
					} else {
						direction = (currentMouseY < scrollbarCenter) ? NEGATIVE_SCROLL : POSITIVE_SCROLL;
					}
				} else {
					int thumbY = thumbRect.y;
					if (!drawInverted()) {
						direction = (currentMouseY < thumbY) ? POSITIVE_SCROLL : NEGATIVE_SCROLL;
					}  else {
						direction = (currentMouseY < thumbY) ? NEGATIVE_SCROLL : POSITIVE_SCROLL;
					}
				}
				break;
			case JSlider.HORIZONTAL:
				if (thumbRect.isEmpty() ) {
					int scrollbarCenter = sbSize.width / 2;
					if (!drawInverted()) {
						direction = (currentMouseX < scrollbarCenter) ? NEGATIVE_SCROLL : POSITIVE_SCROLL;
					} else {
						direction = (currentMouseX < scrollbarCenter) ? POSITIVE_SCROLL : NEGATIVE_SCROLL;
					}
				} else {
					int thumbX = thumbRect.x;
					if (!drawInverted()) {
						direction = (currentMouseX < thumbX) ? NEGATIVE_SCROLL : POSITIVE_SCROLL;
					} else {
						direction = (currentMouseX < thumbX) ? POSITIVE_SCROLL : NEGATIVE_SCROLL;
					}
				}
				break;
			}
			scrollDueToClickInTrack(direction);
			Rectangle r = thumbRect;
			if ( !r.contains(currentMouseX, currentMouseY) ) {
				if (shouldScroll(direction) ) {
					scrollTimer.stop();
					scrollListener.setDirection(direction);
					scrollTimer.start();
				}
			}
		}

		/***
		 * Sets a track bound for th thumb currently operated.
		 */
		private void setThumbBounds(int[] neighbours) {
			int halfThumbWidth = thumbRect.width / 2;
			int halfThumbHeight = thumbRect.height / 2;

			switch (slider.getOrientation()) {
			case JSlider.VERTICAL:
				_trackTop = (neighbours[1] == -1) ? trackRect.y : MultiSliderUI.this.thumbRects[neighbours[1]].y + halfThumbHeight;
				_trackBottom = (neighbours[0] == -1) ? trackRect.y + (trackRect.height - 1) : MultiSliderUI.this.thumbRects[neighbours[0]].y + halfThumbHeight;
				break;
			case JSlider.HORIZONTAL:
				_trackLeft = (neighbours[0] == -1) ? trackRect.x : MultiSliderUI.this.thumbRects[neighbours[0]].x + halfThumbWidth;
				_trackRight = (neighbours[1] == -1) ? trackRect.x + (trackRect.width - 1) : MultiSliderUI.this.thumbRects[neighbours[1]].x + halfThumbWidth;
				break;
			}
		}

		/*
		 * this is a very lazy way to find the closest.  One might want to
		 * implement a much faster algorithm.
		 */
		private int findClosest(int x, int y, int[] neighbours, int excluded) {
			int orientation = slider.getOrientation();
			int rightmin = Integer.MAX_VALUE; // for dxw, dy
			int leftmin = -Integer.MAX_VALUE; // for dx, dyh
			int dx = 0;
			int dxw = 0;
			int dy = 0;
			int dyh = 0;
			neighbours[0] = -1;	// left
			neighbours[1] = -1;	// right
			for (int i = 0; i < MultiSliderUI.this.thumbCount; i++) {
				if (i == excluded) {
					continue;
				}
				switch (orientation) {
				case JSlider.VERTICAL:
					dy = MultiSliderUI.this.thumbRects[i].y - y;
					dyh = (MultiSliderUI.this.thumbRects[i].y + MultiSliderUI.this.thumbRects[i].height) - y;
					if (dyh <= 0) {
						if (dyh > leftmin) { // has to be > and not >=
							leftmin = dyh;
							neighbours[0] = i;
						}
					}
					if (dy >= 0) {
						if (dy <= rightmin) {
							rightmin = dy;
							neighbours[1] = i;
						}
					}
					break;
				case JSlider.HORIZONTAL:
					dx = MultiSliderUI.this.thumbRects[i].x - x;
					dxw = (MultiSliderUI.this.thumbRects[i].x + MultiSliderUI.this.thumbRects[i].width) - x;
					if (dxw <= 0) {
						if (dxw >= leftmin) {
							leftmin = dxw;
							neighbours[0] = i;
						}
					}
					if (dx >= 0) {
						if (dx < rightmin) { // has to be < and not <=
							rightmin = dx;
							neighbours[1] = i;
						}
					}
					break;
				}
			}
			//System.out.println("neighbours = " + neighbours[0] + ", " + neighbours[1]);
			int closest = (Math.abs(leftmin) <= Math.abs(rightmin)) ? neighbours[0] : neighbours[1];
			return (closest == -1) ? 0 : closest;
		}

		/***
		 * Set the models value to the position of the top/left
		 * of the thumb relative to the origin of the track.
		 */
		public void mouseDragged( MouseEvent e ) {
			((MultiSlider) MultiSliderUI.this.slider).setValueBeforeStateChange(((MultiSlider) MultiSliderUI.this.slider).getValueAt(MultiSliderUI.this.currentIndex));
			int thumbMiddle = 0;
			boolean bounded = ((MultiSlider)slider).isBounded();

			if (!slider.isEnabled()) {
				return;
			}

			currentMouseX = e.getX();
			currentMouseY = e.getY();

			if (! MultiSliderUI.this.isDragging) {
				return;
			}

			switch (slider.getOrientation()) {
			case JSlider.VERTICAL:
				int halfThumbHeight = thumbRect.height / 2;
				int thumbTop = e.getY() - offset;
				if (bounded) {
					int[] neighbours = new int[2];
					int idx = -1;
					int diff = e.getY() - firstXY[1];
					//System.out.println("diff = " + diff);
					if (e.getY() - firstXY[1] > 0) {
						idx = minmaxIndices[0];
					} else {
						idx = minmaxIndices[1];
					}
					minmaxIndices[0] = minmaxIndices[1] = idx;
					//System.out.println("idx = " + idx);
					if (idx == -1) {
						break;
					}

					//System.out.println("thumbTop = " + thumbTop);
					neighbours[0] = ((idx - 1) < 0) ? -1 : (idx - 1);
					neighbours[1] = ((idx + 1) >= MultiSliderUI.this.thumbCount) ? -1 : (idx + 1);
					thumbRect = MultiSliderUI.this.thumbRects[idx];
					MultiSliderUI.this.currentIndex = idx;
					((MultiSlider)slider).setValueIsAdjustingAt(idx, true);
					setThumbBounds(neighbours);
				}

				thumbTop = Math.max(thumbTop, _trackTop - halfThumbHeight);
				thumbTop = Math.min(thumbTop, _trackBottom - halfThumbHeight);

				setThumbLocation(thumbRect.x, thumbTop);

				thumbMiddle = thumbTop + halfThumbHeight;
				((MultiSlider)slider).setValueAt(MultiSliderUI.this.currentIndex, valueForYPosition(thumbMiddle) );
				break;
			case JSlider.HORIZONTAL:
				int halfThumbWidth = thumbRect.width / 2;
				int thumbLeft = e.getX() - offset;
				if (bounded) {
					int[] neighbours = new int[2];
					int idx = -1;
					if (e.getX() - firstXY[0] <= 0) {
						idx = minmaxIndices[0];
					} else {
						idx = minmaxIndices[1];
					}
					minmaxIndices[0] = minmaxIndices[1] = idx;
					//System.out.println("idx = " + idx);
					if (idx == -1) {
						break;
					}
					//System.out.println("thumbLeft = " + thumbLeft);
					neighbours[0] = ((idx - 1) < 0) ? -1 : (idx - 1);
					neighbours[1] = ((idx + 1) >= MultiSliderUI.this.thumbCount) ? -1 : (idx + 1);
					thumbRect = MultiSliderUI.this.thumbRects[idx];
					MultiSliderUI.this.currentIndex = idx;
					((MultiSlider)slider).setValueIsAdjustingAt(idx, true);
					setThumbBounds(neighbours);
				}

				thumbLeft = Math.max(thumbLeft, _trackLeft - halfThumbWidth);
				thumbLeft = Math.min(thumbLeft, _trackRight - halfThumbWidth);

				setThumbLocation(thumbLeft, thumbRect.y);

				thumbMiddle = thumbLeft + halfThumbWidth;

				((MultiSlider)slider).setValueAt(MultiSliderUI.this.currentIndex, valueForXPosition(thumbMiddle));
				break;
			default:
				return;
			}
		}

		public void mouseReleased(MouseEvent e) {
			if (!slider.isEnabled()) {
				return;
			}

			offset = 0;
			scrollTimer.stop();

			if (slider.getSnapToTicks()) {
				MultiSliderUI.this.isDragging = false;
				((MultiSlider)slider).setValueIsAdjustingAt(MultiSliderUI.this.currentIndex, false);
			} else {
				((MultiSlider)slider).setValueIsAdjustingAt(MultiSliderUI.this.currentIndex, false);
				MultiSliderUI.this.isDragging = false;
			}

			slider.repaint();
		}
	}

	/***
	 * A static version of the above.
	 */
	static class SharedActionScroller extends AbstractAction {
		int _dir;
		boolean _block;

		public SharedActionScroller(int dir, boolean block) {
			_dir = dir;
			_block = block;
		}

		public void actionPerformed(ActionEvent e) {
			JSlider slider = (JSlider)e.getSource();
			MultiSliderUI ui = (MultiSliderUI)slider.getUI();
			if ( _dir == NEGATIVE_SCROLL || _dir == POSITIVE_SCROLL ) {
				int realDir = _dir;
				if (slider.getInverted()) {
					realDir = _dir == NEGATIVE_SCROLL ? POSITIVE_SCROLL : NEGATIVE_SCROLL;
				}
				if (_block) {
					ui.scrollByBlock(realDir);
				} else {
					ui.scrollByUnit(realDir);
				}
			} else {
				if (slider.getInverted()) {
					if (_dir == MIN_SCROLL) {
						((MultiSlider)slider).setValueAt(ui.currentIndex,
								slider.getMaximum());
					} else if (_dir == MAX_SCROLL) {
						((MultiSlider)slider).setValueAt(ui.currentIndex,
								slider.getMinimum());
					}
				} else {
					if (_dir == MIN_SCROLL) {
						((MultiSlider)slider).setValueAt(ui.currentIndex,
								slider.getMinimum());
					} else if (_dir == MAX_SCROLL) {
						((MultiSlider)slider).setValueAt(ui.currentIndex,
								slider.getMaximum());
					}
				}
			}
		}
	}
}
