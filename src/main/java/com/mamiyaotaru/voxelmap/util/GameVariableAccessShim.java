package com.mamiyaotaru.voxelmap.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

import java.io.File;

public class GameVariableAccessShim {
    private static final MinecraftClient minecraft = MinecraftClient.getInstance();

    public static MinecraftClient getMinecraft() {
        return minecraft;
    }

    public static ClientWorld getWorld() {
        return minecraft.world;
    }

    public static File getDataDir() {
        return minecraft.runDirectory;
    }

    public static int xCoord() {
        return (int) (
                minecraft.getCameraEntity().getPos().getX() < 0.0 ? minecraft.getCameraEntity().getPos().getX() - 1.0 : minecraft.getCameraEntity().getPos().getX()
        );
    }

    public static int zCoord() {
        return (int) (
                minecraft.getCameraEntity().getPos().getZ() < 0.0 ? minecraft.getCameraEntity().getPos().getZ() - 1.0 : minecraft.getCameraEntity().getPos().getZ()
        );
    }

    public static int yCoord() {
        return (int) Math.ceil(minecraft.getCameraEntity().getPos().getY());
    }

    public static double xCoordDouble() {
        return minecraft.getCameraEntity().prevX + (minecraft.getCameraEntity().x - minecraft.getCameraEntity().prevX) * minecraft.getTickDelta();
    }

    public static double zCoordDouble() {
        return minecraft.getCameraEntity().prevZ + (minecraft.getCameraEntity().z - minecraft.getCameraEntity().prevZ) * minecraft.getTickDelta();
    }

    public static float rotationYaw() {
        return minecraft.getCameraEntity().prevYaw + (minecraft.getCameraEntity().yaw - minecraft.getCameraEntity().prevYaw) * minecraft.getTickDelta();
    }
}
