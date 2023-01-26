# OpenRocket Installer Files
Originally a separate repository of Justin Hanna's, now a directory
with its own commit history in the openrocket repository.

# OpenRocket Supported Installers
The [OpenRocket](http://www.openrocket.info) project will do its best
to publish installers for the following platforms.

* Windows, 64-bit
* macOS, 64-bit (Intel & Apple Silicon)
* Linux, 64-bit

# Maintainers
* Neil Weinstock 
* Justin Hanney
* Joe Pfeiffer
* Sibo Van Gool

# Instructions on updating the macOS drag-and-drop installer
This is an example of updating the installer from 22.02.beta.05 to 22.02:
1. Make sure install4j is not opened
2. Download the OpenRocket-22.02.beta.05-macOS.dmg file 
3. Make a read/write .dmg file using the terminal command `hdiutil convert OpenRocket-22.02.beta.05-macOS.dmg -format UDRW -o 22.02.beta.05_rw.dmg`
4. Enlarge the writable DMG, by first checking the current size: `hdiutil resize 22.02.beta.05_rw.dmg`, e.g. you get 430000 in the 'cur' column, then just resize it to e.g. 500000: `hdiutil resize -sectors 500000 22.02.beta.05_rw.dmg`
5. Mount the DMG: `hdiutil attach 22.02.beta.05_rw.dmg`
6. Open the OpenRocket-disk from your desktop and change the app name from 22.02.beta.05 to 22.02 
7. Copy the .DS_Store to `openrocket/install4j/22.02/macOS_resources` by running the command `cp /Volumes/OpenRocket/.DS_Store openrocket/install4j/22.02/macOS_resources/DS_Store`
8. Eject the OpenRocket DMG disk from your desktop (important step)
9. Delete `22.02.beta.05_rw.dmg`
10. You're all done!
