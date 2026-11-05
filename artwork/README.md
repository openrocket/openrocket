# OpenRocket artwork

`branding/` contains high-resolution logos, banners and macOS icon artwork. Editable originals and older exports are preserved in `branding/source/` and `branding/old/`. These files are not application resources and are not bundled in the distribution JARs. Files that the application does not load at runtime belong here, not in `core/src/main/resources` or `swing/src/main/resources`, which are packaged into the JARs.

`branding/splashscreen/` contains the splash screen sources and earlier versions. The current source is `splashscreen-2026.xcf` (GIMP); the old logo layers are kept hidden in it. Export it to `swing/src/main/resources/pix/splashscreen.png` at 662×336, and a 2× version to `splashscreen@2x.png` for HiDPI screens. Leave the area to the right under the "OpenRocket" title free: the version number is drawn there at runtime by `Splash.java`.

The application icons used at runtime remain in `swing/src/main/resources/pix/icon/`. Installer-only icons are in `install4j/resources/icons/`. The macOS icon can be generated from `branding/macos-squircle-full.png` using Image2Icon; its editable original is `branding/source/macos-squircle.afdesign`. Save the generated `.icns` file in the installer icon directory.

`texture-edge-modes/GenerateEdgeModePreviews.java` generates the example images shown when hovering the texture edge mode options in the appearance panel. Run it from the repository root with `java artwork/texture-edge-modes/GenerateEdgeModePreviews.java`; it writes the images and their `@2x` versions to `swing/src/main/resources/pix/edgemodes/`.
