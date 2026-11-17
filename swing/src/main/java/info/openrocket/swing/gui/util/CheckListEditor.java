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

package info.openrocket.swing.gui.util;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.SwingUtilities;


/**
 * Toggles the check of the item under the mouse.
 * <p>
 * The toggle happens when the button is released over the same item it was pressed on. Using
 * mouseClicked instead would drop the toggle whenever the mouse moves even one pixel between
 * press and release, and would treat a quick second click as a double click.
 *
 * Created on Feb 4, 2011
 * @author Eugene Ryzhikov
 *
 */
final class CheckListEditor extends MouseAdapter {
	// One editor is shared by all check lists, so remember which list the press was on
	private Object pressedList = null;
	private int pressedIndex = -1;

	@Override
	public void mousePressed(MouseEvent e) {
		pressedList = e.getSource();
		pressedIndex = indexAt(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		int index = indexAt(e);
		boolean sameItem = index >= 0 && index == pressedIndex && e.getSource() == pressedList;
		pressedList = null;
		pressedIndex = -1;
		if (!sameItem) {
			return;
		}

		JList<?> list = (JList<?>) e.getSource();
		@SuppressWarnings("unchecked")
		DefaultCheckListModel<Object> model = (DefaultCheckListModel<Object>) list.getModel();
		model.setCheckedIndex(index, !model.isCheckedIndex(index));
		e.consume();
	}

	/**
	 * Return the index of the item under a left-button event, or -1 if the event is not on an
	 * item of an enabled check list.
	 */
	private static int indexAt(MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e) || !(e.getSource() instanceof JList<?> list)) {
			return -1;
		}
		if (!list.isEnabled() || !(list.getModel() instanceof DefaultCheckListModel<?>)) {
			return -1;
		}
		int index = list.locationToIndex(e.getPoint());
		if (index < 0) {
			return -1;
		}
		Rectangle bounds = list.getCellBounds(index, index);
		return bounds != null && bounds.contains(e.getPoint()) ? index : -1;
	}
}
