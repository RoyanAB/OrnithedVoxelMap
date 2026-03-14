package com.mamiyaotaru.voxelmap.ornithe;

import com.mamiyaotaru.voxelmap.VoxelMap;
import com.mamiyaotaru.voxelmap.persistent.ThreadManager;
import com.mamiyaotaru.voxelmap.util.BiomeRepository;
import com.mamiyaotaru.voxelmap.util.CommandUtils;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import com.mamiyaotaru.voxelmap.util.ReflectionUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.PacketByteBuf;

import java.nio.charset.StandardCharsets;

public class FabricModVoxelMap {
    public static FabricModVoxelMap instance;
    private boolean initialized = false;
    private RenderTickCounter timer = null;
    private VoxelMap master = null;

    public FabricModVoxelMap(FabricModVoxelMapBootstrapper riftModVoxelMap) {
        instance = this;
        this.master = new VoxelMap();
    }

    public static void onRenderHand(float partialTicks, long timeSlice) {
        try {
            instance.master.getWaypointManager().renderWaypoints(partialTicks);
        } catch (Exception var4) {
        }
    }

    public void lateInit() {
        this.initialized = true;
        this.timer = (RenderTickCounter) ReflectionUtils.getPrivateFieldValueByType(MinecraftClient.getInstance(), MinecraftClient.class, RenderTickCounter.class);
        this.master.lateInit(true, false);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                FabricModVoxelMap.this.onShutDown();
            }
        });
    }

    public void clientTick(MinecraftClient client) {
        if (!this.initialized) {
            boolean OK = !I18nUtils.getString("minimap.ui.welcome2").equals("minimap.ui.welcome2");

            if (MinecraftClient.getInstance() == null || client.getResourceManager() == null || client.getTextureManager() == null) {
                OK = false;
            }

            if (OK) {
                this.lateInit();
            }
        }

        if (this.initialized) {
            boolean newTick = this.timer.ticksThisFrame > 0;
            this.master.onTick(client, newTick);
        }
    }

    public void renderOverlay() {
        if (!this.initialized) {
            this.lateInit();
        }

        this.master.onTickInGame(MinecraftClient.getInstance());
    }

    public boolean onChat(Text chat) {
        return CommandUtils.checkForWaypoints(chat, "");
    }

    public boolean onSendChatMessage(String message) {
        if (message.startsWith("/newWaypoint")) {
            CommandUtils.waypointClicked(message);
            return false;
        } else if (message.startsWith("/ztp")) {
            CommandUtils.teleport(message);
            return false;
        } else {
            return true;
        }
    }

    public void onShutDown() {
        System.out.print("Saving all world maps");
        instance.master.getPersistentMap().purgeCachedRegions();
        instance.master.getMapOptions().saveAll();
        BiomeRepository.saveBiomeColors();
        long shutdownTime = System.currentTimeMillis();

        while (
                ThreadManager.executorService.getQueue().size() + ThreadManager.executorService.getActiveCount() > 0
                        && System.currentTimeMillis() - shutdownTime < 10000L
        ) {
            System.out.print(".");

            try {
                Thread.sleep(200L);
            } catch (InterruptedException var4) {
            }
        }

        System.out.println();
    }

    public boolean handleCustomPayload(CustomPayloadS2CPacket packet) {
        if (packet != null && packet.getChannel() != null) {
            String channel = packet.getChannel().getPath();
            PacketByteBuf buffer = packet.getData();
            if (channel.equals("world_info") || channel.equals("world_id")) {
                byte id = buffer.readByte();
                byte length = buffer.readByte();
                byte[] bytes = new byte[length];
                buffer.readBytes(bytes);
                String subWorldName = new String(bytes, StandardCharsets.UTF_8);
                this.master.newSubWorldName(subWorldName, true);
                return true;
            }
        }

        return false;
    }
}
