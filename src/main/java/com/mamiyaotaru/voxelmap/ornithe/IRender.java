package com.mamiyaotaru.voxelmap.ornithe;

import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public interface IRender {
    <T extends Entity> Identifier publicGetEntityTexture(T var1);
}
