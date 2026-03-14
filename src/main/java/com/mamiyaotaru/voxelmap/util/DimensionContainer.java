package com.mamiyaotaru.voxelmap.util;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;

public class DimensionContainer implements Comparable<DimensionContainer> {
    public DimensionType type;
    public String name = "notLoaded";
    public int id = 0;
    public ResourceLocation resourceLocation;

    public DimensionContainer(DimensionType type, String name, int ID, ResourceLocation resourceLocation) {
        this.type = type;
        this.name = name;
        this.id = ID;
        this.resourceLocation = resourceLocation;
    }

    public String getStorageName() {
        String storageName = null;
        if (this.resourceLocation != null) {
            if (this.resourceLocation.getNamespace().equals("minecraft")) {
                storageName = this.resourceLocation.getPath();
            } else {
                storageName = this.resourceLocation.toString();
            }

            if (this.id != -9999 && (this.type == null || this.type.getId() != this.id)) {
                storageName = storageName + " " + this.id;
            }
        } else if (this.id != -9999) {
            storageName = "" + this.id;
        } else {
            storageName = "???";
        }

        return storageName;
    }

    public String getDisplayName() {
        return !this.name.equals("Failed Dimension") && !this.name.equals("Unknown Dimension") ? TextUtils.prettify(this.name) : "Dimension " + this.id;
    }

    public int compareTo(DimensionContainer other) {
        return this.id - other.id;
    }
}
