# PocketLauncher
A mod loader and version manager for Minecraft PE

## Installation
You can get the latest build version in [Actions/Android CI](https://github.com/1503Dev/pocket-launcher/actions/workflows/android.yml)

## Build
1. Configure `local.properties` in the root directory of the project, add your own signing information
  ```ini
  sdk.dir = Your Android SDK directory
  SIGNING_KEY_ALIAS = Your signing key alias
  SIGNING_KEY_ALIAS_PASSWORD = Your signing key alias password
  SIGNING_KEY_PASSWORD = Your signing key password
  SIGNING_KEY_PATH_OVERRIDE = Your signing key path
  ```

2. Run the build command or use Android Studio to build the project
```bash
./gradlew build
```

## Note
- The project PocketLauncher and the organization 1503Dev don't belong to Mojang Studios and Microsoft, don't have any relationship with Mojang Studios and Microsoft
- If you want to use PocketLauncher to play Minecraft PE, Make sure you already have Minecraft PE

## License
Open source under the [LGPL-2.1](LICENSE) license
