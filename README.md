# PocketLauncher
A mod loader and version manager for Minecraft PE

## Installation
You can get the latest build version in [Actions/Android CI](https://github.com/1503Dev/pocket-launcher/actions/workflows/android.yml)

## Build
1. Configure `local.properties` in the root directory of the project, add your own signing information
  ```ini local.properties
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
_**This project has enabled network security configuration from Reqable (for capturing network packets)**_  
_**If you need to build for production environment, you should build the `Release` variant, or remove the dependency on `com.reqable.android:user-certificate-trust` in `app/build.gradle` and remove the network security configuration in `app/src/main/AndroidManifest.xml`**_
```groovy app/build.gradle
dependencies {
    debugImplementation("com.reqable.android:user-certificate-trust:1.0.0")
    // Remove it
}
```
```xml app/src/main/AndroidManifest.xml
<manifest>
    <application
        android:networkSecurityConfig="@xml/network_security_config" <!--Remove it-->
        >
    </application>
</manifest>
```

## Note
- The project PocketLauncher and the organization 1503Dev don't belong to Mojang Studios and Microsoft, don't have any relationship with Mojang Studios and Microsoft
- If you want to use PocketLauncher to play Minecraft PE, Make sure you already have Minecraft PE

## License
Open source under the [LGPL-2.1](LICENSE) license
