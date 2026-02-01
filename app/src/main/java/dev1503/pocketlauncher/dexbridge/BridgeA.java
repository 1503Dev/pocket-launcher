package dev1503.pocketlauncher.dexbridge;

import dev1503.pocketlauncher.InstanceInfo;
import dev1503.pocketlauncher.KVConfig;
import dev1503.pocketlauncher.Log;
import dev1503.pocketlauncher.Utils;

public class BridgeA {
    private final String TAG = "BridgeA";

    private InstanceInfo instanceInfo;

    private final KVConfig kvGlobalGameConfig;
    private final KVConfig kvLauncherSettings;

    private final Utils utils = Utils.INSTANCE;
    private MinecraftActivity self;

    public BridgeA(MinecraftActivity self) {
        this.self = self;
        this.kvGlobalGameConfig = new KVConfig(self, utils.getDataDirPath(self) + "global_game_config.json");
        this.kvLauncherSettings = new KVConfig(self, utils.getDataDirPath(self) + "launcher_settings.json");

        utils.setKvLauncherSettings(kvLauncherSettings);
        utils.setKvGlobalGameConfig(kvGlobalGameConfig);

        InstanceInfo selectedInstance = utils.getSelectedInstance(self);
        if (selectedInstance == null) {
            selectedInstance = new InstanceInfo(null, "", 0, 0, "", "", "", "", null, false);
        }

        this.instanceInfo = new InstanceInfo(
                selectedInstance.getName(),
                selectedInstance.getVersionName(),
                selectedInstance.getVersionCode(),
                selectedInstance.getInstallTime(),
                selectedInstance.getSource(),
                selectedInstance.getDirPath(),
                selectedInstance.getEntityType(),
                selectedInstance.getEntity(),
                self,
                true
        );

        Log.i(TAG, "Init");
    }

    public boolean isDataIsolationEnabled() {
        return instanceInfo.getDataIsolation();
    }
    public String getDataDirPath() {
        if (isDataIsolationEnabled()) {
            return instanceInfo.getDirPath() + "data/";
        } else {
            return utils.getDirIPath(self, "global_game_data");
        }
    }
}