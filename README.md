# PSPTools
**Yes, i know this readme is outdated, Ill update it soon!**

![hackatime](https://hackatime-badge.hackclub.com/U0923KXMGUR/PSPTools) [![Java CI with Gradle](https://github.com/xFN10x/PSPTools/actions/workflows/gradle.yml/badge.svg)](https://github.com/xFN10x/PSPTools/actions/workflows/gradle.yml)

Various tools for managing your Playstation®  Portable, with support for PS3

Features include:

- **Save Manager** - View normal and possibly corrupted saves.
- **Save Importer/Downloader** -  Import saves from zip and folder. Download saves from a [Save Database](https://github.com/bucanero/apollo-saves), or a URL.
- **Save Patcher** - Patch save files for PSP, (and ps3!) with Apollo Save Tool.
- **Game Manager/Adder** (Planned: 1.2) - Add games/ISOs/homebrew from a URL, or zip. No need for manual extracting.
- **AutoPSX (PS1 EBOOT Creator)** (Planned: 1.3) - Create PS1 EBOOTS from BIN/CUE, m3u, zip, 7z and more, without the need of anything else.

### Using JAR
If you don't have a prebuilt version of PSPTools made for your platform, you can use the cross-platform jar. You need to follow these steps to make sure it works properly.

#### Install JRE
A JRE is required to run the jar. You may already have one installed, but if the program doesn't start, use [this](https://learn.microsoft.com/en-ca/java/openjdk/download#openjdk-25) (You should also run `java -jar "path/of/PSPTools.jar"`, and make an issue with the error.)

## Special Thanks

- **[bucanero](https://github.com/bucanero)** - Save patching & save database ([apollo-lib](https://github.com/bucanero/apollo-lib), [apollo-patches](https://github.com/bucanero/apollo-patches), [apollo-saves](https://github.com/bucanero/apollo-saves)) Support them here: <https://www.patreon.com/dparrino>
- **[georgemoralis](https://github.com/georgemoralis)** - [JPCSP](https://github.com/jpcsp/jpcsp)'s UMD ISO reader, used for ISO games and videos.
