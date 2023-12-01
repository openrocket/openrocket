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
This is an example of updating the installer from 22.02 to 23.09.beta.01:

If you use the `macOS_resources/template_dmg_rw.dmg` file, you can skip to step 4

1. Make sure install4j is not opened
2. Download the OpenRocket-22.02-macOS.dmg file 
3. Make a read/write .dmg file using the terminal command `hdiutil convert OpenRocket-22.02-macOS.dmg -format UDRW -o 22.02.beta.05_rw.dmg`

4. Enlarge the writable DMG, by first checking the current size: `hdiutil resize template_dmg_rw.dmg`, e.g. you get 430000 in the 'cur' column, then just resize it to e.g. 500000: `hdiutil resize -sectors 500000 template_dmg_rw.dmg`
5. Mount the DMG: `hdiutil attach template_dmg_rw.dmg`
6. Open the OpenRocket-disk from your desktop and change the app name from 22.02 to 23.09 
7. Copy the .DS_Store to `openrocket/install4j/23.09/macOS_resources` by running the command `cp /Volumes/OpenRocket/.DS_Store openrocket/install4j/23.09/macOS_resources/DS_Store`
8. Eject the OpenRocket DMG disk from your desktop (important step)
9. (optional) Delete `template_dmg_rw.dmg`
10. You're all done!

# Whitelisting OpenRocket on Windows
Even when you've code signed the Windows installer, Microsoft Defender Smart Screen can still give warnings that the installer is from an unknown publisher. This warning will go away after a couple of months or after the installer has been downloaded enough times. However, you can also whitelist the installer by submitting it to Microsoft for malware analysis.

You can do so through the following link: https://www.microsoft.com/en-us/wdsi/filesubmission

Select 'Software developer' and use the following settings:
- Select the Microsoft security product used to scan the file
  - Microsoft Defender SmartScreen
- Company name
  - OpenRocket
- Do you have a Microsoft support case number?
  - No
- Software Assurance ID
  - Don't fill this stuff in
- Select the file
  - Upload the .exe installer
- Should this file be removed from our database at a certain date?
  - No
- What do you believe this file is?
  - Incorrectly detected as PUA (potentially unwanted application)
- Detection name
  - Unknown Publisher Security Warning
- Additional information
  - Hello, I am a software developer for the open-source program OpenRocket. We are about to release a new version of our program, and have already code-signed it, but Windows SmartScreen still marks the software as an unrecognized app. I think this has to do with building trust because the program isn't downloaded enough yet. However, for our last release, which was published in February of this year, one of our team members contacted Microsoft Support to ask for an acceleration of the trust program, which was successful; you generously whitelisted us. My question now is: is it possible to do this again for our new release?