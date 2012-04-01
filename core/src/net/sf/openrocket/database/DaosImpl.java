package net.sf.openrocket.database;


public class DaosImpl implements Daos {

	private ComponentPresetDao bodyTubePresetDao;
	
	public DaosImpl() throws Exception {
		bodyTubePresetDao = new ComponentPresetDao();
		bodyTubePresetDao.initialize();
		
	}
	
	@Override
	public ComponentPresetDao getBodyTubePresetDao() {
		return bodyTubePresetDao;
	}

}
