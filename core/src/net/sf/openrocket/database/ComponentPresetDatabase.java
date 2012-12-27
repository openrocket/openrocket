package net.sf.openrocket.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.startup.Application;

public abstract class ComponentPresetDatabase extends Database<ComponentPreset> implements ComponentPresetDao {

	private static final LogHelper logger = Application.getLogger();

	private volatile boolean startedLoading = false;
	private volatile boolean endedLoading = false;
	private final boolean asynchronous;

	/** Set to true the first time {@link #blockUntilLoaded()} is called. */
	protected volatile boolean inUse = false;

	public ComponentPresetDatabase() {
		super();
		this.asynchronous = false;
	}
	
	public ComponentPresetDatabase(boolean asynchronous ) {
		super();
		this.asynchronous = asynchronous;
	}

	@Override
	public List<ComponentPreset> listAll() {
		blockUntilLoaded();
		return list;
	}

	@Override
	public void insert( ComponentPreset preset ) {
		list.add(preset);
	}

	@Override
	public List<ComponentPreset> listForType( ComponentPreset.Type type ) {
		blockUntilLoaded();
		if ( type == null ) {
			return Collections.<ComponentPreset>emptyList();
		}

		List<ComponentPreset> result = new ArrayList<ComponentPreset>(list.size()/6);

		for( ComponentPreset preset : list ) {
			if ( preset.get(ComponentPreset.TYPE).equals(type) ) {
				result.add(preset);
			}
		}
		return result;

	}

	/**
	 * Return a list of component presets based on the type.
	 * All components returned will be of Type type.
	 * 
	 * @param type  
	 * @param favorite if true, only return the favorites.  otherwise return all matching.
	 * @return
	 */
	@Override
	public List<ComponentPreset> listForType( ComponentPreset.Type type, boolean favorite ) {
		blockUntilLoaded();

		if ( !favorite ) {
			return listForType(type);
		}

		List<ComponentPreset> result = new ArrayList<ComponentPreset>(list.size()/6);

		Set<String> favorites = Application.getPreferences().getComponentFavorites(type);

		for( ComponentPreset preset : list ) {
			if ( preset.get(ComponentPreset.TYPE).equals(type) && favorites.contains(preset.preferenceKey())) {
				result.add(preset);
			}
		}
		return result;
	}

	@Override
	public List<ComponentPreset> listForTypes( ComponentPreset.Type ... type ) {
		blockUntilLoaded();

		if( type == null || type.length == 0 ) {
			return Collections.<ComponentPreset>emptyList();
		}

		if (type.length == 1 ) {
			return listForType(type[0]);
		}

		List<ComponentPreset> result = new ArrayList<ComponentPreset>(list.size()/6);

		for( ComponentPreset preset : list ) {
			ComponentPreset.Type presetType = preset.get(ComponentPreset.TYPE);
			typeLoop: for( int i=0; i<type.length; i++ ) {
				if ( presetType.equals(type[i]) ) {
					result.add(preset);
					break typeLoop; // from inner loop.
				}
			}

		}
		return result;
	}

	@Override
	public List<ComponentPreset> listForTypes( List<ComponentPreset.Type> types ) {
		blockUntilLoaded();
		return listForTypes( types.toArray(new ComponentPreset.Type[types.size()]) );
	}

	@Override
	public List<ComponentPreset> find(String manufacturer, String partNo) {
		blockUntilLoaded();
		List<ComponentPreset> presets = new ArrayList<ComponentPreset>();
		for( ComponentPreset preset : list ) {
			if ( preset.getManufacturer().getSimpleName().equals(manufacturer) && preset.getPartNo().equals(partNo) ) {
				presets.add(preset);
			}
		}
		return presets;
	}

	@Override
	public void setFavorite( ComponentPreset preset, ComponentPreset.Type type, boolean favorite ) {
		blockUntilLoaded();
		Application.getPreferences().setComponentFavorite( preset, type, favorite );
		this.fireAddEvent(preset);
	}


	/**
	 * Used for loading the component preset database.  This method will be called in a background
	 * thread to load the presets asynchronously.
	 */
	protected abstract void load();

	/**
	 * Start loading the presets.
	 * 
	 * @throws  IllegalStateException	if this method has already been called.
	 */
	public void startLoading() {
		if (startedLoading) {
			throw new IllegalStateException("Already called startLoading");
		}
		startedLoading = true;
		if (asynchronous) {
			new LoadingThread().start();
		} else {
			load();
		}
		synchronized (this) {
			endedLoading = true;
			this.notifyAll();
		}
	}

	/**
	 * Background thread for loading the presets. 
	 */
	private class LoadingThread extends Thread {
		
		private LoadingThread() {
			this.setName("PresetLoadingThread");
			this.setPriority(MIN_PRIORITY);
		}
		@Override
		public void run() {
			load();
		}
	}

	/**
	 * Block the current thread until loading of the presets has been completed.
	 * 
	 * @throws IllegalStateException	if startLoading() has not been called.
	 */
	public void blockUntilLoaded() {
		inUse = true;
		if (!startedLoading) {
			throw new IllegalStateException("startLoading() has not been called");
		}
		if (!endedLoading) {
			synchronized (this) {
				while (!endedLoading) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						logger.warn("InterruptedException occurred, ignoring", e);
					}
				}
			}
		}
	}

}
