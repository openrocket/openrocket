*********
3D Engine
*********

The OpenRocket 3D engine lives under ``info.openrocket.swing.gui.figure3d`` in the ``swing`` module.
The interactive 3D design view and Photo Studio share the same OpenGL host, scene graph, geometry code,
and renderer.

.. contents:: Table of Contents
   :depth: 2
   :local:
   :backlinks: none

Overview
========

The main data and rendering flow is:

.. code-block:: none

   RocketFigure3d / PhotoPanel
      -> SharedCanvasRenderScheduler
      -> GLScenePanel (lwjgl3-awt AWTGLCanvas)
      -> Scene3DOrchestrator
         -> Scene + controllers
         -> RocketSceneSynchronizer
            model thread: RocketMeshBuilder.buildSnapshot()
                          -> immutable RocketSceneSnapshot
            GL thread:    RocketMeshBuilder.applySnapshot()
                          -> SceneObject and GPU resources
         -> RealisticRenderer
      -> resolved scene texture
      -> AWT default framebuffer -> HUD -> buffer swap

The model and Swing controls do not make OpenGL calls. Context-owned work is queued through the
orchestrator and executed while the canvas context is current on the shared render thread.

Entry Points
============

The main entry points are:

* :file:`swing/src/main/java/info/openrocket/swing/gui/figure3d/RocketFigure3d.java`
* :file:`swing/src/main/java/info/openrocket/swing/gui/figure3d/photo/PhotoPanel.java`
* :file:`swing/src/main/java/info/openrocket/swing/gui/figure3d/photo/PhotoFrame.java`
* :file:`swing/src/main/java/info/openrocket/swing/gui/figure3d/ui/GLScenePanel.java`

``RocketFigure3d``
   Embeds a 3D canvas in the design window. It handles selection, the HUD, zoom state, and the three
   user-facing display modes. The UI's **Figure** mode maps to ``DisplaySettings.RenderMode.XRAY``;
   the other modes map to ``UNFINISHED`` and ``FINISHED``.

``PhotoPanel`` / ``PhotoFrame``
   Host Photo Studio. They configure the shared engine for finished rendering, Photo Studio camera
   controls, backgrounds, lighting, motion blur, and exhaust effects.

``GLScenePanel``
   Subclasses lwjgl3-awt's ``AWTGLCanvas``. Each panel owns one OpenGL context, its context-local
   resources, and a ``Scene3DOrchestrator``.

GL Host and Render Scheduling
=============================

``GLScenePanel`` requests an OpenGL 3.3 core, double-buffered context through lwjgl3-awt. The requested
and effective ``GLData`` are recorded by ``GLContextDiagnostics`` during initialization. The AWT default
framebuffer is deliberately single-sampled; scene MSAA is implemented by ``RealisticRenderer`` in its
own off-screen render target and resolved before presentation.

``SharedCanvasRenderScheduler`` serializes every active ``AWTGLCanvas`` onto one background thread named
``figure3d-render``. This avoids concurrent JAWT rendering across design windows and Photo Studio. The
design view renders only after it has been marked dirty, while Photo Studio renders continuously while
its panel is active.

Important lifecycle rules are:

* Add and remove canvases on the Swing event dispatch thread.
* Create and resize GL resources only while the owning context is current. ``GLScenePanel.disposeGL`` releases
  them during lwjgl3-awt's context-current canvas-disposal callback.
* Keep GPU object caches scoped to a canvas or renderer; contexts do not share object identifiers.
* Restore the canvas's LWJGL capabilities before rendering because capabilities are thread-local and the
  shared scheduler switches contexts between canvases.
* Treat the window size and the native framebuffer size as separate values, especially on HiDPI displays.

``GLScenePanel`` also detects context resets where the platform supports robustness and asks its wrapper
to rebuild the canvas. Startup redraw recovery handles cases where a valid off-screen frame has not yet
appeared in the AWT framebuffer.

Scene Orchestration and Threading
=================================

The orchestration layer is centered on:

* :file:`swing/src/main/java/info/openrocket/swing/gui/figure3d/scene/orchestration/Scene3DOrchestrator.java`
* :file:`swing/src/main/java/info/openrocket/swing/gui/figure3d/scene/orchestration/RocketSceneSynchronizer.java`
* :file:`swing/src/main/java/info/openrocket/swing/gui/figure3d/scene/graph/Scene.java`

``Scene3DOrchestrator`` owns the runtime ``Scene``, camera and input controllers, renderer, viewport,
decal cache, and the queue for work that requires a current GL context. Higher-level code uses it to
change the camera, update rendering configuration, rebuild the rocket, and request image export.

``RocketSceneSynchronizer`` listens for component changes and chooses the least expensive safe update:

* Appearance-only changes are coalesced and update existing appearances on the GL thread.
* Structural, geometry, visibility, and selected-configuration changes build a ``RocketSceneSnapshot``
  on the calling model thread. This captures a consistent model state before a queued GL task replaces
  the rocket-derived scene objects and particle emitters.

``RocketMeshBuilder.buildSnapshot`` is CPU-only. ``RocketMeshBuilder.applySnapshot`` creates
``SceneObject`` instances, appearances, textures, motors, particle emitters, and other context-owned
resources. Keeping these phases separate prevents the render thread from reading a rocket while it is
being edited.

Geometry and Materials
======================

Rocket geometry is generated by:

* :file:`swing/src/main/java/info/openrocket/swing/gui/figure3d/geometry/RocketMeshBuilder.java`
* the generators under :file:`swing/src/main/java/info/openrocket/swing/gui/figure3d/geometry/basic/`
  and :file:`swing/src/main/java/info/openrocket/swing/gui/figure3d/geometry/components/`
* :file:`swing/src/main/java/info/openrocket/swing/gui/figure3d/materials/AppearanceFactory.java`

``RocketMeshBuilder`` traverses the active and extra render instances in the selected
``FlightConfiguration``, chooses a generator for each component, applies instance transforms, and adds
motors and their optional particle-emitter plans.

``AppearanceFactory`` converts the core appearance model into ``Appearance3D`` data, including color,
opacity, decals, and textures. Mesh shape or placement problems generally start in ``RocketMeshBuilder``
or a component generator; material, texture, or transparency problems generally start in
``AppearanceFactory``, ``TransparencyPolicy``, the material binder, or the shaders.

Rendering Configuration
=======================

:file:`swing/src/main/java/info/openrocket/swing/gui/figure3d/scene/properties/RenderingConfiguration.java`
groups the mutable runtime settings:

``DisplaySettings``
   Display mode, x-ray opacity, and internal-surface visibility.

``GraphicsQualitySettings``
   Overall quality, MSAA, FXAA, shadows, ambient occlusion, surface roughness, culling, and the option to
   reduce expensive effects during interaction.

``VisualEffectsSettings``
   Carets and helper markers, particle effects, motion blur, rocket-drag behavior, and related controls.

Application and document preferences are mapped onto this configuration by ``Figure3DPreferences``.
See :ref:`graphics_preferences` for the user-facing defaults.

Render Pipeline
===============

The main renderer is
:file:`swing/src/main/java/info/openrocket/swing/gui/figure3d/rendering/RealisticRenderer.java`. A normal
display frame proceeds as follows:

1. Update the camera, fixed GL state, and flame-driven dynamic lights.
2. Render the shadow map when shadows are enabled.
3. Clear the main off-screen target, render the background, and render opaque geometry.
4. Resolve opaque MSAA into the single-sample target.
5. Render translucent geometry with weighted blended order-independent transparency and composite it
   over the opaque result.
6. Render sparks, smoke, flames, CG/CP carets, and the camera point-of-interest marker as enabled.
7. Apply ambient occlusion, motion blur, outlines, and FXAA as configured.
8. Copy the final post-processed image back into the renderer's resolved target when necessary.
9. Let ``GLScenePanel`` present the resolved texture to the AWT default framebuffer, draw the HUD, and
   swap buffers.

When interaction-effect reduction is active, shadows, ambient occlusion, motion blur, and outlines are
skipped while the user is dragging, scrolling, or resizing. Image export reads the resolved scene before
the HUD is drawn.

Render-pass implementations live under
:file:`swing/src/main/java/info/openrocket/swing/gui/figure3d/rendering/passes/`. Important passes and
helpers include ``ShadowPass``, ``BackgroundPass``, ``GeometryPass``,
``WeightedBlendedTransparency``, ``CaretsPass``, ``CameraPointOfInterestPass``,
``AmbientOcclusionPass``, ``MotionBlurPass``, ``OutlinePass``, and ``FXAAPass``. Shaders are stored under
:file:`swing/src/main/resources/shaders/`.

Design View and Photo Studio
============================

The design view adds selection and picking, CG/CP carets, an AWT-rendered HUD, and figure/unfinished/
finished display modes. It uses demand-driven rendering so inactive design windows do not consume a
continuous share of the render thread.

Photo Studio uses the same scene and renderer in finished mode, but supplies its own camera semantics,
background and light settings, animation, and optional flame, smoke, and spark effects. It runs a
continuous render loop while active because particles and motion blur change with time.

Multi-Window and Platform Notes
===============================

Every canvas has an independent context and resource lifetime, but all canvases render through the
shared scheduler. Code that adds GL state or caching must therefore be both context-local and safe when
the next scheduled frame belongs to another canvas.

The host uses LWJGL's native OpenGL context API so lwjgl3-awt's X11/GLX backend continues to work under
XWayland rather than switching to EGL. The macOS backend does not accept every optional context
attribute, so the host does not request a debug context or an sRGB default framebuffer there. Robust
context reset detection is requested only on supported Windows configurations.

Package Map
===========

The tracked packages under ``info.openrocket.swing.gui.figure3d`` are:

.. code-block:: none

   figure3d
   ├── animation           # flight pose and playback helpers
   ├── constants           # camera and rendering constants
   ├── geometry
   │   ├── basic           # reusable primitive generators
   │   └── components      # rocket-component mesh generators
   ├── input               # keyboard, drag, and input state
   ├── materials           # appearances, textures, and conversion
   ├── math                # raycasting helpers
   ├── particles
   │   ├── flame
   │   ├── smoke
   │   └── spark
   ├── photo
   │   └── sky
   │       └── builtin
   ├── rendering
   │   ├── backgrounds
   │   └── passes
   ├── scene
   │   ├── controllers
   │   ├── events
   │   ├── graph
   │   ├── orchestration
   │   └── properties
   ├── ui                  # GL canvas and HUD
   └── utils               # GL, color, and vector helpers

Where To Start
==============

* New component geometry: ``RocketMeshBuilder`` and ``geometry/components``.
* Materials, decals, or transparency: ``AppearanceFactory``, ``TransparencyPolicy``,
  ``DefaultMaterialBinder``, ``TextureStateManager``, and the shaders.
* Camera, orbit, pan, picking, or selection: the scene controllers, ``Scene3DOrchestrator``, and
  ``GLScenePanel`` input handling.
* Render ordering or post-processing: ``RealisticRenderer`` and ``rendering/passes``.
* Photo Studio behavior: ``PhotoPanel``, ``PhotoFrame``, and ``PhotoSettings``.
* Context creation, resize, blank-frame, or multi-window failures: ``GLScenePanel``,
  ``SharedCanvasRenderScheduler``, and the two wrapper panels.

Related Documentation
=====================

* :doc:`architecture`
* :doc:`codebase_walkthrough`
* :doc:`testing_and_debugging`
* :doc:`../setup/preferences`
