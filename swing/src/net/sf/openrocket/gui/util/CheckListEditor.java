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

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.JList;
import javax.swing.SwingUtilities;


/**
 * Determines mouse click and 
 * 1. Toggles the check on selected item if clicked once
 * 2. Clears checks and checks selected item if clicked more then once
 * 
 * Created on Feb 4, 2011
 * @author Eugene Ryzhikov
 *
 */
final class CheckListEditor extends MouseAdapter {
	@Override
	public void mouseClicked(MouseEvent e) {
		
		if (!SwingUtilities.isLeftMouseButton(e))
			return;
		
		JList list = (JList) e.getSource();
		if (!list.isEnabled() || (!(list.getModel() instanceof DefaultCheckListModel<?>)))
			return;
		
		int index = list.locationToIndex(e.getPoint());
		if (index < 0)
			return;
		
		Rectangle bounds = list.getCellBounds(index, index);
		
		if (bounds.contains(e.getPoint())) {
			
			@SuppressWarnings("unchecked")
			DefaultCheckListModel<Object> model = (DefaultCheckListModel<Object>) list.getModel();
			
			if (e.getClickCount() > 1) {
				// clear all and check selected for more then 1 clicks
				// Original implementation had this implementation.  I didn't like that behavior.
//				model.setCheckedItems(Arrays.asList(model.getElementAt(index)));
			} else {
				// simple toggle for 1 click
				model.setCheckedIndex(index, !model.isCheckedIndex(index));
			}
			e.consume();
		}
		
	}
	
}
