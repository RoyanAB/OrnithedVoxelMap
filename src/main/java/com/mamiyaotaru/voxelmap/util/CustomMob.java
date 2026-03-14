package com.mamiyaotaru.voxelmap.util;

public class CustomMob {
    public String id = "notLoaded";
    public boolean enabled = true;
    public boolean isHostile = false;
    public boolean isNeutral = false;

    public CustomMob(String type, boolean enabled) {
        this.id = type;
        this.enabled = enabled;
    }

    public CustomMob(String type, boolean isHostile, boolean isNeutral) {
        this.id = type;
        this.isHostile = isHostile;
        this.isNeutral = isNeutral;
    }
}
