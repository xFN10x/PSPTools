# PSPTools

![hackatime](https://hackatime-badge.hackclub.com/U0923KXMGUR/PSPTools) [![Java CI with Gradle](https://github.com/xFN10x/PSPTools/actions/workflows/gradle.yml/badge.svg)](https://github.com/xFN10x/PSPTools/actions/workflows/gradle.yml)

Various tools for managing your Playstation® Portable, Playstation® Vita, or any other playsation save. (PS3-PS5)

Features include:

- ***Launch Page***
  - **Save Manager** - View all the saves on your PSP, listien to the XMB music, watch the XMB video, and back them up.
  - **Save Tools** - Various tools regarding saves.
    - **Save Database** - Downloads saves from the [apollo database](https://github.com/bucanero/apollo-saves).
    - **Save Patching** - Patch saves all within the app, using the [apollo patch database](https://github.com/bucanero/apollo-patches).
  - **Game Manager** - View games, ISO games included, on your PSP. View the XMB icons, and watch those videos aswell.
- ***Extra***
  - **Open Custom SFO Manager** - Use this to open any folder that is SFO based. (For example, the PSP/SAVEDATA folder is SFO based, since each folder in SAVEDATA has an SFO.)
  - **View SFO** - View the raw data of any SFO. Useful for developers.
  - **Select Demo PSP** - Use this to test out the program without the need for any folder setting up, or a PSP. (When using the demo PSP, you cannot do save patching.)

> [!TIP] Using JAR
>
> If you don't have a prebuilt version of PSPTools made for your platform, you can use the cross-platform jar. To do this you need to have java 25 installed. You can get one at <https://learn.microsoft.com/en-ca/java/openjdk/download#openjdk-25>

> [!NOTE] For Flavourtown Users/Shipwrights
> For testing, please use the demo mode. Or, if you actually own a PSP or Vita, you can try that to get the full experiance.


## Special Thanks

- **[bucanero](https://github.com/bucanero)** - Save patching & save database ([apollo-lib](https://github.com/bucanero/apollo-lib), [apollo-patches](https://github.com/bucanero/apollo-patches), [apollo-saves](https://github.com/bucanero/apollo-saves)) Support them here: <https://www.patreon.com/dparrino>
- **[georgemoralis](https://github.com/georgemoralis)** - [JPCSP](https://github.com/jpcsp/jpcsp)'s UMD ISO reader, used for reading ISO files.

## Building
PSPTools is made with gradle, so building is easy.

 Simply clone this repo, or [download the code](https://github.com/xFN10x/PSPTools/archive/refs/heads/main.zip), then run the following command in a terminal:

 ```terminal
 ./gradlew build jpackage
 ```

This command should output an uber-JAR in the build/builtJars folder, and an executeable build in build/builtDist