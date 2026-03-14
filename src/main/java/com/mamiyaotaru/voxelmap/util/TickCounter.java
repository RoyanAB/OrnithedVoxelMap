package com.mamiyaotaru.voxelmap.util;

public class TickCounter {
    public static int tickCounter = 0;

    public static void onTick(boolean clock) {
        if (clock) {
            tickCounter = tickCounter == Integer.MAX_VALUE ? 0 : tickCounter + 1;
        }
    }
}
