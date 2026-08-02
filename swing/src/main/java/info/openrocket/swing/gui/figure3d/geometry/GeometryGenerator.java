package info.openrocket.swing.gui.figure3d.geometry;

/**
 * Interface for generating 3D geometry meshes in the OpenRocket 3D visualization system.
 * 
 * <p>This interface serves as a marker and base contract for all geometry generator classes
 * that create 3D mesh data for rendering rocket components and basic shapes. Implementing
 * classes should provide static factory methods that return {@link Mesh} objects containing
 * vertex and index data suitable for 3D rendering.</p>
 * 
 * <p>The generated meshes follow OpenRocket's coordinate system conventions and are designed
 * to be rendering API-agnostic, making them suitable for both real-time visualization and
 * export to various file formats.</p>
 */
public interface GeometryGenerator {

}
