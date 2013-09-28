/*
 * Copyright (c) 2009-2011, EzWare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.Redistributions
 * in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.Neither the name of the
 * EzWare nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package net.sf.openrocket.gui.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.io.Serializable;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class CheckListRenderer extends JCheckBox implements ListCellRenderer, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private static final Border NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
	private static final Border SAFE_NO_FOCUS_BORDER = NO_FOCUS_BORDER; // may change in the feature
	
	/**
	 * Constructs a default renderer object for an item in a list.
	 */
	public CheckListRenderer() {
		super();
		setOpaque(true);
		setBorder(getNoFocusBorder());
	}
	
	private static Border getNoFocusBorder() {
		if (System.getSecurityManager() != null) {
			return SAFE_NO_FOCUS_BORDER;
		} else {
			return NO_FOCUS_BORDER;
		}
	}
	
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		
		setComponentOrientation(list.getComponentOrientation());
		
		Color bg = null;
		Color fg = null;
		
		JList.DropLocation dropLocation = list.getDropLocation();
		if (dropLocation != null && !dropLocation.isInsert() && dropLocation.getIndex() == index) {
			
			bg = UIManager.getColor("List.dropCellBackground");
			fg = UIManager.getColor("List.dropCellForeground");
			
			isSelected = true;
		}
		
		if (isSelected) {
			setBackground(bg == null ? list.getSelectionBackground() : bg);
			setForeground(fg == null ? list.getSelectionForeground() : fg);
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		
		if (value instanceof Icon) {
			setIcon((Icon) value);
			setText("");
		} else {
			setIcon(null);
			setText(getObjectAsText(value));
		}
		
		setSelected(isChecked(list, index));
		
		setEnabled(list.isEnabled());
		setFont(list.getFont());
		
		Border border = null;
		if (cellHasFocus) {
			if (isSelected) {
				border = UIManager.getBorder("List.focusSelectedCellHighlightBorder");
			}
			if (border == null) {
				border = UIManager.getBorder("List.focusCellHighlightBorder");
			}
		} else {
			border = getNoFocusBorder();
		}
		setBorder(border);
		
		return this;
	}
	
	protected String getObjectAsText(Object obj) {
		return (obj == null) ? "" : obj.toString();
	}
	
	private boolean isChecked(JList list, int index) {
		
		if (list.getModel() instanceof DefaultCheckListModel<?>) {
			return ((DefaultCheckListModel<?>) list.getModel()).isCheckedIndex(index);
		} else {
			return false;
		}
		
	}
	
	/**
	 * @return true if the background is opaque and differs from the JList's background; false otherwise
	 */
	@Override
	public boolean isOpaque() {
		Color back = getBackground();
		Component p = getParent();
		if (p != null) {
			p = p.getParent();
		}
		// p should now be the JList.
		boolean colorMatch = (back != null) && (p != null) && back.equals(p.getBackground()) && p.isOpaque();
		return !colorMatch && super.isOpaque();
	}
	
	@Override
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		
		if ("text".equals(propertyName) ||
				(("font".equals(propertyName) || "foreground".equals(propertyName)) &&
						oldValue != newValue && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null)) {
			
			super.firePropertyChange(propertyName, oldValue, newValue);
		}
	}
	
	// Methods below are overridden for performance reasons.
	
	@Override
	public void validate() {
	}
	
	@Override
	public void invalidate() {
	}
	
	@Override
	public void repaint() {
	}
	
	@Override
	public void revalidate() {
	}
	
	@Override
	public void repaint(long tm, int x, int y, int width, int height) {
	}
	
	@Override
	public void repaint(Rectangle r) {
	}
	
	@Override
	public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {
	}
	
	@Override
	public void firePropertyChange(String propertyName, char oldValue, char newValue) {
	}
	
	@Override
	public void firePropertyChange(String propertyName, short oldValue, short newValue) {
	}
	
	@Override
	public void firePropertyChange(String propertyName, int oldValue, int newValue) {
	}
	
	@Override
	public void firePropertyChange(String propertyName, long oldValue, long newValue) {
	}
	
	@Override
	public void firePropertyChange(String propertyName, float oldValue, float newValue) {
	}
	
	@Override
	public void firePropertyChange(String propertyName, double oldValue, double newValue) {
	}
	
	@Override
	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
	}
	
	@SuppressWarnings("serial")
	public static class UIResource extends DefaultListCellRenderer implements javax.swing.plaf.UIResource {
	}
	
}