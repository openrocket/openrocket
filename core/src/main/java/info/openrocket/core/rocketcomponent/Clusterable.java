package info.openrocket.core.rocketcomponent;

import info.openrocket.core.util.ChangeSource;

public interface Clusterable extends ChangeSource, Instanceable {

	public ClusterConfiguration getClusterConfiguration();

	public void setClusterConfiguration(ClusterConfiguration cluster);

	public double getClusterSeparation();

}
