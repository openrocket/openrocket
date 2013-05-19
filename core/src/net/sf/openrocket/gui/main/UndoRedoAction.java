package net.sf.openrocket.gui.main;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.UndoRedoListener;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;

/**
 * Inner class to implement undo/redo actions.
 */
public class UndoRedoAction extends AbstractAction implements UndoRedoListener {
	
	// Use Factory mechanism because we want to register the new instance as an
	// UndoRedoListener.
	public static UndoRedoAction newUndoAction( OpenRocketDocument document ) {
		UndoRedoAction undo = new UndoRedoAction( UNDO, document );
		document.addUndoRedoListener(undo);
		return undo;
	}

	public static UndoRedoAction newRedoAction( OpenRocketDocument document ) {
		UndoRedoAction redo = new UndoRedoAction( REDO, document );
		document.addUndoRedoListener(redo);
		return redo;
	}

	private static final Logger log = LoggerFactory.getLogger(UndoRedoAction.class);
	private static final Translator trans = Application.getTranslator();

	private static final int UNDO = 1;
	private static final int REDO = 2;
	
	private final int type;
	
	private final OpenRocketDocument document;
	
	// Sole constructor
	private UndoRedoAction(int type, OpenRocketDocument document) {
		this.document = document;
		if (type != UNDO && type != REDO) {
			throw new IllegalArgumentException("Unknown type = " + type);
		}
		this.type = type;
		setAllValues();
	}
	
	
	// Actual action to make
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (type) {
		case UNDO:
			log.info(Markers.USER_MARKER, "Performing undo, event=" + e);
			document.undo();
			break;
		
		case REDO:
			log.info(Markers.USER_MARKER, "Performing redo, event=" + e);
			document.redo();
			break;
		}
	}
	
	
	// Set all the values correctly (name and enabled/disabled status)
	@Override
	public void setAllValues() {
		String name, desc;
		boolean actionEnabled;
		
		switch (type) {
		case UNDO:
			//// Undo
			name = trans.get("OpenRocketDocument.Undo");
			desc = document.getUndoDescription();
			actionEnabled = document.isUndoAvailable();
			this.putValue(SMALL_ICON, Icons.EDIT_UNDO);
			break;
		
		case REDO:
			////Redo
			name = trans.get("OpenRocketDocument.Redo");
			desc = document.getRedoDescription();
			actionEnabled = document.isRedoAvailable();
			this.putValue(SMALL_ICON, Icons.EDIT_REDO);
			break;
		
		default:
			throw new BugException("illegal type=" + type);
		}
		
		if (desc != null)
			name = name + " (" + desc + ")";
		
		putValue(NAME, name);
		setEnabled(actionEnabled);
	}
}
