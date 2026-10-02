# OpenRocket artwork

`branding/` contains high-resolution logos, banners and macOS icon artwork. Editable originals and older exports are preserved in `branding/source/` and `branding/old/`. These files are not application resources and are not bundled in the distribution JARs.

The application icons used at runtime remain in `swing/src/main/resources/pix/icon/`. Installer-only icons are in `install4j/resources/icons/`. The macOS icon can be generated from `branding/macos-squircle-full.png` using Image2Icon; its editable original is `branding/source/macos-squircle.afdesign`. Save the generated `.icns` file in the installer icon directory.
