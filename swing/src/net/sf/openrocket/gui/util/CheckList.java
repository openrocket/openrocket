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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

/**
 * The decorator for JList which makes it work like check list
 * UI can be designed using JList and which can be later decorated to become a check list
 * @author Eugene Ryzhikov
 *
 * @param <T> list item type
 */
public class CheckList<T> {
	
	private final JList list;
	private static final MouseAdapter checkBoxEditor = new CheckListEditor();
	
	public static class Builder {
		
		private JList list;
		
		public Builder(JList list) {
			this.list = list == null ? new JList() : list;
		}
		
		public Builder() {
			this(null);
		}
		
		public <T> CheckList<T> build() {
			return new CheckList<T>(list);
		}
		
	}
	
	
	/**
	 * Wraps the standard JList and makes it work like check list 
	 * @param list
	 */
	private CheckList(final JList list) {
		
		if (list == null)
			throw new NullPointerException();
		this.list = list;
		this.list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		if (!isEditorAttached())
			list.addMouseListener(checkBoxEditor);
		this.list.setCellRenderer(new CheckListRenderer());
		
		setupKeyboardActions(list);
		
	}
	
	@SuppressWarnings("serial")
	private void setupKeyboardActions(final JList list) {
		String actionKey = "toggle-check";
		list.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), actionKey);
		list.getActionMap().put(actionKey, new AbstractAction() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleIndex(list.getSelectedIndex());
			}
		});
	}
	
	private boolean isEditorAttached() {
		
		for (MouseListener ml : list.getMouseListeners()) {
			if (ml instanceof CheckListEditor)
				return true;
		}
		return false;
		
	}
	
	public JList getList() {
		return list;
	}
	
	/**
	 * Sets data to a check list. Simplification for setting new the model 
	 * @param data
	 */
	public void setData(Collection<T> data) {
		setModel(new DefaultCheckListModel<T>(data));
	}
	
	/**
	 * Sets the model for check list.
	 * @param model
	 */
	public void setModel(DefaultCheckListModel<T> model) {
		list.setModel(model);
	}
	
	@SuppressWarnings("unchecked")
	public DefaultCheckListModel<T> getModel() {
		return (DefaultCheckListModel<T>) list.getModel();
	}
	
	/**
	 * Returns a collection of checked items. 
	 * @return collection of checked items. Empty collection if nothing is selected
	 */
	public Collection<T> getCheckedItems() {
		return getModel().getCheckedItems();
	}
	
	public Collection<T> getUncheckedItems() {
		List<T> unchecked = new ArrayList<T>();
		for (int i = getModel().getSize() - 1; i >= 0; i--) {
			unchecked.add((T) getModel().getElementAt(i));
		}
		unchecked.removeAll(getCheckedItems());
		return unchecked;
	}
	
	public void checkAll() {
		getModel().checkAll();
	}
	
	public void clearAll() {
		getModel().clearAll();
	}
	
	/**
	 * Resets checked elements 
	 * @param elements
	 */
	public void setCheckedItems(Collection<T> elements) {
		getModel().setCheckedItems(elements);
	}
	
	public void setUncheckedItems( Collection<T> elements ) {
		getModel().setUncheckedItems(elements);
	}
	
	public void toggleIndex(int index) {
		if (index >= 0 && index < list.getModel().getSize()) {
			DefaultCheckListModel<T> model = getModel();
			model.setCheckedIndex(index, !model.isCheckedIndex(index));
		}
	}
	
}
