package com.mamiyaotaru.voxelmap.forgemod;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

public class CoreModContainer extends DummyModContainer {
   public CoreModContainer() {
      super(new ModMetadata());
      ModMetadata meta = this.getMetadata();
      meta.modId = "com.mamiyaotaru.voxelmap.forgemod";
      meta.name = "Mixin CORE";
      meta.authorList = Lists.newArrayList(new String[]{"MamiyOtaru"});
   }

   public boolean registerBus(EventBus bus, LoadController controller) {
      bus.register(this);
      return true;
   }
}
