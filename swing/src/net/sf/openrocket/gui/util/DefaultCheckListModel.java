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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractListModel;

/**
 * Default model for check list. It is based on the list of items
 * Implementation of checks is based on HashSet of checked items
 *
 * @author Eugene Ryzhikov
 *
 * @param <T> list element type
 */
public class DefaultCheckListModel<T> extends AbstractListModel {
	
	private static final long serialVersionUID = 1L;
	
	private final List<T> data = new ArrayList<T>();
	private final Set<T> checks = new HashSet<T>();
	
	public DefaultCheckListModel(Collection<? extends T> data) {
		
		if (data == null)
			return;
		for (T object : data) {
			this.data.add(object);
			checks.clear();
		}
	}
	
	public DefaultCheckListModel(T... data) {
		this(Arrays.asList(data));
	}
	
	/* (non-Javadoc)
	 * @see org.oxbow.swingbits.list.ICheckListModel#getSize()
	 */
	@Override
	public int getSize() {
		return data().size();
	}
	
	private List<T> data() {
		return data;
	}
	
	
	@Override
	public Object getElementAt(int index) {
		return data().get(index);
	}
	
	public boolean isCheckedIndex(int index) {
		return checks.contains(data().get(index));
	}
	
	public void setCheckedIndex(int index, boolean value) {
		T o = data().get(index);
		if (value)
			checks.add(o);
		else
			checks.remove(o);
		fireContentsChanged(this, index, index);
	}
	
	public Collection<T> getCheckedItems() {
		List<T> items = new ArrayList<T>(checks);
		items.retainAll(data);
		return Collections.unmodifiableList(items);
	}
	
	public void clearAll() {
		checks.clear();
		fireContentsChanged(this, 0, checks.size() - 1);
	}
	
	public void checkAll() {
		checks.addAll(data);
		fireContentsChanged(this, 0, checks.size() - 1);
	}
	
	public void setCheckedItems(Collection<T> items) {
		
		//		if ( CollectionUtils.isEmpty(items))  return;
		
		List<T> correctedItems = new ArrayList<T>(items);
		correctedItems.retainAll(data);
		
		checks.clear();
		checks.addAll(correctedItems);
		fireContentsChanged(this, 0, checks.size() - 1);
		
		
	}
	
	public void setUncheckedItems( Collection<T> items ) {
		
		List<T> correctedItems = new ArrayList<T>(data);
		correctedItems.removeAll(items);
		
		checks.clear();
		checks.addAll(correctedItems);
		fireContentsChanged(this, 0, checks.size() - 1);
		
	}
}
