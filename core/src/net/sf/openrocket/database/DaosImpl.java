package net.sf.openrocket.database;


public class DaosImpl implements Daos {

	private BodyTubePresetDao bodyTubePresetDao;
	
	public DaosImpl() throws Exception {
		bodyTubePresetDao = new BodyTubePresetDao();
		bodyTubePresetDao.initialize();
		
	}
	
	@Override
	public BodyTubePresetDao getBodyTubePresetDao() {
		return bodyTubePresetDao;
	}

}
