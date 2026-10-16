package com.mamiyaotaru.voxelmap.forgemod;

import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.MixinEnvironment.CompatibilityLevel;

@Name("MixinCore")
public class CoreMod implements IFMLLoadingPlugin {
   public CoreMod() {
      MixinBootstrap.init();
      MixinEnvironment.setCompatibilityLevel(CompatibilityLevel.JAVA_8);
      Mixins.addConfiguration("mixins.voxelmap.json");
   }

   public String[] getASMTransformerClass() {
      return null;
   }

   public String getModContainerClass() {
      return "com.mamiyaotaru.voxelmap.forgemod.CoreModContainer";
   }

   public String getSetupClass() {
      return null;
   }

   public void injectData(Map<String, Object> data) {
   }

   public String getAccessTransformerClass() {
      return null;
   }
}
