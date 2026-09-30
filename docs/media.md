# Screenshot and showcase sources

Captured from OpenRocket `26.xx-SNAPSHOT` on macOS with Java 17 on 2026-09-29 (Europe/Brussels). UI screenshots and the theme GIF were refreshed from commit `905413641` with the local toolbar/menu icon changes in `Icons.java`. Photo Studio renders and the model rotation use commit `71cd80a57`. These are development-version images, not screenshots of the latest stable release.

The README's 2D design, finished 3D view, and vertical-motion plot all use the bundled **Three stage low power rocket**, with the same selected motor configuration, `[C6-5; B6-0; B6-0]`. That simulation was rerun before capture. The plot shows the sustainer's altitude and vertical velocity over the first 18 seconds, with stage separation and apogee events. Both design views use the Light theme. The theme comparison also uses this rocket. The getting-started screens use **A simple model rocket**; its simulations were rerun before capturing the results. Source designs were not saved or modified.

The gallery uses **Parallel booster staging** and **Tube fin rocket** from `core/src/main/resources/datafiles/examples`, plus the supplied `Atemis Original.ork` and `vortikon-reset.ork`. Atemis identifies Luis Ignoto Ledo as its designer and credits a Kmobrain model as the basis for the modified upper and motor sections. Vortikon has no designer field in the supplied file. The two supplied design files are not bundled with this documentation.

All rocket geometry, materials, decals, and UI contents come from OpenRocket. UI captures render the real Swing components at 2× resolution; native 3D viewports use OpenRocket's image-capture API. Images are resized for documentation, and the gallery and theme animation add labels outside the UI. No generative images were used. The menu bar is included in the window for these captures; a normal macOS launch places it in the system menu bar.

The updated tutorial images cover the getting-started interface overview, menus, task tabs, and six rocket views. The three preference theme previews are also updated. Later component-editing tutorial images and other specialized documentation screenshots retain their existing captures.

## Refreshing the media

Build the checkout with `./gradlew shadowJar`. Use a fixed window layout and the same design for all three themes. The capture session uses isolated in-memory preferences and does not save changes to source designs. Full UI images are distributed at 1600 pixels wide; retain lossless PNG for UI text. The existing tutorial filenames are preserved so translated documentation keeps resolving them.

For the four Photo Studio renders, use a 1000 × 650 logical viewport (2000 × 1300 on Retina), field of view 0.72 radians, view altitude and azimuth 0, pitch 0.35 radians, light altitude 0.65, light azimuth −0.65, light strength 1.1, and ambient light 0.35. Use a gradient from RGB (14, 24, 45) to (54, 77, 106), with flame, smoke, sparks, and motion blur disabled.

| Design | Roll (radians) | Yaw (radians) | View distance / design length |
| --- | ---: | ---: | ---: |
| Atemis | 3.4 | 0.18 | 1.10 |
| Vortikon | 0.45 | 0.28 | 1.15 |
| Parallel boosters | 0.6 | 0.20 | 1.10 |
| Tube-fin rocket | 0.4 | −0.30 | 1.10 |

For the turntable, export 90 frames while increasing Vortikon's roll by `2π / 90` each frame. Encode at 15 fps as H.264 with a 1200 × 780 frame, YUV420p, and fast-start metadata. The GIF alternative is 720 × 468 at 12 fps. It is a model rotation, not a flight simulation or an in-app video-export feature.

The theme GIF has three labeled frames, each held for 2.6 seconds. Static alternatives remain in `source/img/setup/preferences`. New gallery assets live in `source/img/showcase`; the MP4 lives in `source/_static/media` so Sphinx can serve it directly.

Menu captures include the menu bar, the expanded menu (and parent menu for submenus), and the surrounding application workspace. Native popup windows are rendered at their actual positions over the application's root pane. Gallery and animation labels use the bundled `swing/src/main/resources/fonts/Inter/Inter-Regular.ttf`, matching the application's regular UI font.
