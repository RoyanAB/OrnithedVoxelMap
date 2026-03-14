package com.mamiyaotaru.voxelmap.ornithe;

import net.ornithemc.osl.entrypoints.api.client.ClientModInitializer;

public class FabricModVoxelMapBootstrapper implements ClientModInitializer {
    private FabricModVoxelMap argumentPasser;

    @Override
    public void initClient() {
        this.argumentPasser = new FabricModVoxelMap(this);
    }
}
