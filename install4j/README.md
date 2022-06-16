# OpenRocket Installer Files
Originally a separate repository of Justin Hanna's, now a directory
with its own commit history in the openrocket repository.

# OpenRocket Supported Installers
The [OpenRocket](http://www.openrocket.info) project will do its best
to publish installers for the following platforms.

* Windows, 64-bit
* macOS, 64-bit
* Linux, 64-bit

# Maintainers
* Neil Weinstock 
* Justin Hanney
* Joe Pfeiffer

# Instructions on updating the macOS drag-and-drop installer
This is an example of updating the installer from 22.02.beta.01 to 22.02.beta.02:
1. Download the 22.02.beta.01.dmg file
2. Make a read/write .dmg file using the terminal command `hdiutil convert OpenRocket_macos_22_02_beta_01.dmg -format UDRW -o 22.02.beta.01_rw.dmg`
3. Enlarge the writable DMG, by first checking the current size: `hdiutil resize OpenRocket_macos_22_02_beta_01_rw.dmg`, e.g. you get 370000 in the 'cur' column, then just resize it to e.g. 400000: `hdiutil resize -sectors 400000 OpenRocket_macos_22_02_beta_01_rw.dmg`
4. Mount the DMG: `hdiutil attach OpenRocket_macos_22_02_beta_01_rw.dmg`
5. Open the OpenRocket-disk from your desktop and change the app name from 22.02.beta.01 to 22.02.beta.02
6. Copy the .DS_Store to `openrocket/install4j/22.xx/macOS_resources` by running the command `cp /Volumes/OpenRocket/.DS_Store openrocket/install4j/22.xx/macOS_resources/DS_Store`
7. Eject the OpenRocket DMG disk from your desktop (important step)
8. Delete `OpenRocket_macos_22_02_beta_01_rw.dmg`
9. You're all done!
