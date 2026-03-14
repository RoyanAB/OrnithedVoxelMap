package com.mamiyaotaru.voxelmap.util;

import net.minecraft.util.math.BlockPos;

public class MutableBlockPos extends BlockPos {
    public int field_177962_a;
    public int field_177960_b;
    public int field_177961_c;

    public MutableBlockPos(int x, int y, int z) {
        super(0, 0, 0);
        this.field_177962_a = x;
        this.field_177960_b = y;
        this.field_177961_c = z;
    }

    public com.mamiyaotaru.voxelmap.util.MutableBlockPos withXYZ(int x, int y, int z) {
        this.field_177962_a = x;
        this.field_177960_b = y;
        this.field_177961_c = z;
        return this;
    }

    public void setXYZ(int x, int y, int z) {
        this.field_177962_a = x;
        this.field_177960_b = y;
        this.field_177961_c = z;
    }

    public int getX() {
        return this.field_177962_a;
    }

    public int getY() {
        return this.field_177960_b;
    }

    public int getZ() {
        return this.field_177961_c;
    }
}
