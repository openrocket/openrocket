package info.openrocket.core.rocketcomponent.position;

public interface AxialPositionable {

	public double getAxialOffset();

	public void setAxialOffset(final double newAxialOffset);

	public AxialMethod getAxialMethod();

	public void setAxialMethod(AxialMethod newMethod);
}
