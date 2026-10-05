# OpenRocket Installer Files
Originally a separate repository of Justin Hanna's, now a directory
with its own commit history in the openrocket repository.

# OpenRocket Supported Installers
The [OpenRocket](http://www.openrocket.info) project will do its best
to publish installers for the following platforms.

* Windows, 64-bit (x64 & ARM64)
* macOS, 64-bit (Intel & Apple Silicon)
* Linux, 64-bit (x64 & ARM64)

# Building installer JARs

Run `./gradlew verifyDistributionJars distributionSmokeTest` before building
installers. This creates and verifies the universal JAR and all six platform
JARs in `build/libs`, then smoke-tests the universal JAR and the variant matching
the current JVM's OS and architecture. Each media definition in the current
install4j project selects `OpenRocket-<version>-<platform>.jar` automatically;
the launcher uses the same filename. Set install4j's application version to
`build.version`, or pass `--release=<version>` to `install4jc`.

For a single platform, use a task such as `shadowJarWindowsX64` or
`shadowJarMacosArm64`. Publish `OpenRocket-<version>.jar` as the standalone
cross-platform download. The platform variants are installer inputs. The
[Building and Releasing guide](../docs/source/dev_guide/building_releasing.rst)
lists all tasks, platforms and media IDs, including testing on other architectures.

The install4j project is `openrocket.install4j`. Installer icons are shared in
`resources/icons`; editable branding artwork is kept in `../artwork`. The GitHub
workflows build with the install4j version pinned in
`../.github/actions/setup-install4j/action.yml`; keep it in line with the
install4j version the project is saved with.

# Maintainers
* Neil Weinstock 
* Justin Hanney
* Joe Pfeiffer
* Sibo Van Gool

# Windows code signing with SignPath

The current install4j project builds unsigned Windows installers. Release
installers are built and signed by the GitHub Actions workflow
`.github/workflows/build-windows.yml`; the Windows certificate and private key
are not stored in this repository or in GitHub.

Before the first signing build, configure the SignPath project, trusted
GitHub build system, artifact configuration, signing policy, CI user, and API
token. Also configure the required GitHub Actions secrets and variables. The
complete setup and release procedure is documented in the
[Building and Releasing guide](https://openrocket.readthedocs.io/en/latest/dev_guide/building_releasing.html#windows-code-signing-with-signpath).

For a release, run the **Build and sign Windows installers** workflow from the approved
release branch. A SignPath approver must verify the repository, commit, and
workflow-run origin before approving the request. Publish only the
`openrocket-windows-signed-<run number>` artifact. The similarly named
`openrocket-windows-unsigned-<run number>` artifact is only SignPath input and
must never be released.

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
