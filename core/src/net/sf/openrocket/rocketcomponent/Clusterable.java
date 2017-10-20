package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.util.ChangeSource;

public interface Clusterable extends ChangeSource, Instanceable {

	@Deprecated
	// redundant with Instanceable#getInstanceCount()
	public int getClusterCount();
	
	public ClusterConfiguration getClusterConfiguration();
	
	public void setClusterConfiguration(ClusterConfiguration cluster);
	
	public double getClusterSeparation();
	
	
}
