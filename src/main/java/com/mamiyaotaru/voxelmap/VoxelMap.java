package com.mamiyaotaru.voxelmap;

import com.google.common.base.Charsets;
import com.mamiyaotaru.voxelmap.interfaces.*;
import com.mamiyaotaru.voxelmap.persistent.PersistentMap;
import com.mamiyaotaru.voxelmap.persistent.PersistentMapSettingsManager;
import com.mamiyaotaru.voxelmap.util.*;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloadListener;
import net.minecraft.resource.ZipResourcePack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.Unit;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.dimension.DimensionType;

import java.io.File;
import java.net.URL;
import java.security.CodeSource;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class VoxelMap extends AbstractVoxelMap implements ResourceReloadListener {
    private MapSettingsManager mapOptions = null;
    private RadarSettingsManager radarOptions = null;
    private PersistentMapSettingsManager persistentMapOptions = null;
    private IMap map = null;
    private IRadar radar = null;
    private IRadar radarSimple = null;
    private PersistentMap persistentMap = null;
    private ISettingsAndLightingChangeNotifier settingsAndLightingChangeNotifier = null;
    private WorldUpdateListener worldUpdateListener = null;
    private IColorManager colorManager = null;
    private IWaypointManager waypointManager = null;
    private IDimensionManager dimensionManager = null;
    private ClientWorld world;
    private String worldName = "";
    private Long newServerTime = 0L;
    private boolean checkMOTD = false;
    private ChatHudLine mostRecentLine = null;
    private final UUID devUUID = UUID.fromString("9b37abb9-2487-4712-bb96-21a1e0b2023c");
    private String passMessage = null;
    private ZipResourcePack zipPack = null;

    public VoxelMap() {
        instance = this;
    }

    public void lateInit(boolean showUnderMenus, boolean isFair) {
        if (!MinecraftClient.getInstance().getResourceManager().getAllNamespaces().contains("voxelmap")) {
            CodeSource src = VoxelMap.class.getProtectionDomain().getCodeSource();
            if (src != null) {
                URL jar = src.getLocation();
                File pack = new File(jar.getFile());
                this.zipPack = new ZipResourcePack(pack);
            }
        }

        GLUtils.textureManager = MinecraftClient.getInstance().getTextureManager();
        this.mapOptions = new MapSettingsManager();
        this.mapOptions.showUnderMenus = showUnderMenus;
        this.radarOptions = new RadarSettingsManager();
        this.mapOptions.addSecondaryOptionsManager(this.radarOptions);
        this.persistentMapOptions = new PersistentMapSettingsManager();
        this.mapOptions.addSecondaryOptionsManager(this.persistentMapOptions);
        BiomeRepository.loadBiomeColors();
        this.colorManager = new ColorManager(this);
        this.waypointManager = new WaypointManager(this);
        this.dimensionManager = new DimensionManager(this);
        this.persistentMap = new PersistentMap(this);
        this.mapOptions.loadAll();

        try {
            if (isFair) {
                this.radarOptions.radarAllowed = false;
                this.radarOptions.radarMobsAllowed = false;
                this.radarOptions.radarPlayersAllowed = false;
            } else {
                this.radarOptions.radarAllowed = true;
                this.radarOptions.radarMobsAllowed = true;
                this.radarOptions.radarPlayersAllowed = true;
                this.radar = new Radar(this);
                this.radarSimple = new RadarSimple(this);
            }
        } catch (Exception e) {
            this.radarOptions.radarAllowed = false;
            this.radarOptions.radarMobsAllowed = false;
            this.radarOptions.radarPlayersAllowed = false;
            this.radar = null;
            this.radarSimple = null;
        }

        this.map = new Map(this);
        this.settingsAndLightingChangeNotifier = new SettingsAndLightingChangeNotifier();
        this.worldUpdateListener = new WorldUpdateListener();
        this.worldUpdateListener.addListener(this.map);
        this.worldUpdateListener.addListener(this.persistentMap);
        ReloadableResourceManager resourceManager = (ReloadableResourceManager) MinecraftClient.getInstance().getResourceManager();
        resourceManager.registerListener(this);
        this.apply(resourceManager);
    }

    public CompletableFuture<Void> reload(
            Synchronizer synchronizer, ResourceManager resourceManager, Profiler loadProfiler, Profiler applyProfiler, Executor loadExecutor, Executor applyExecutor
    ) {
        return synchronizer.whenPrepared(Unit.INSTANCE).thenRunAsync(() -> this.apply(resourceManager), applyExecutor);
    }

    private CompletableFuture<Void> apply(ResourceManager resourceManager) {
        this.waypointManager.onResourceManagerReload(resourceManager);
        if (this.radar != null) {
            this.radar.onResourceManagerReload(resourceManager);
        }

        if (this.radarSimple != null) {
            this.radarSimple.onResourceManagerReload(resourceManager);
        }

        this.colorManager.onResourceManagerReload(resourceManager);
        return null;
    }

    public void onTickInGame(MinecraftClient mc) {
        this.map.onTickInGame(mc);
        if (this.passMessage != null) {
            mc.inGameHud.getChatHud().addMessage(new LiteralText(this.passMessage));
            this.passMessage = null;
        }
    }

    public void onSetupCameraTransform() {
        this.map.getFogColor();
    }

    public void onTick(MinecraftClient mc, boolean clock) {
        if (this.checkMOTD) {
            this.checkPermissionMessages(mc);
        }

        if (GameVariableAccessShim.getWorld() != null && !GameVariableAccessShim.getWorld().equals(this.world)
                || this.world != null && !this.world.equals(GameVariableAccessShim.getWorld())) {
            this.world = GameVariableAccessShim.getWorld();
            this.waypointManager.newWorld(this.world);
            this.persistentMap.newWorld(this.world);
            if (this.world != null) {
                String channelList = "worldinfo:world_id";
                PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
                buffer.writeBytes(channelList.getBytes(Charsets.UTF_8));
                mc.getNetworkHandler().sendPacket(new CustomPayloadC2SPacket(new Identifier("minecraft:register"), buffer));
                ByteBuf data1 = Unpooled.buffer(4);
                data1.writeInt(42);
                PacketByteBuf packetBuffer1 = new PacketByteBuf(data1);
                CustomPayloadC2SPacket packet1 = new CustomPayloadC2SPacket(new Identifier("worldinfo:world_id"), packetBuffer1);
                mc.player.networkHandler.sendPacket(packet1);
                ByteBuf data2 = Unpooled.buffer(4);
                data2.writeInt(43);
                PacketByteBuf packetBuffer2 = new PacketByteBuf(data2);
                new CustomPayloadC2SPacket(new Identifier("journeymap:world_info"), packetBuffer2);
                mc.player.getSkinTexture();
                java.util.Map<Type, MinecraftProfileTexture> skinMap = mc.getSkinProvider().getTextures(mc.player.getGameProfile());
                if (skinMap.containsKey(Type.SKIN)) {
                    mc.getSkinProvider().loadSkin(skinMap.get(Type.SKIN), Type.SKIN);
                }

                if (!this.worldName.equals(this.waypointManager.getCurrentWorldName())) {
                    this.worldName = this.waypointManager.getCurrentWorldName();
                    this.radarOptions.radarAllowed = true;
                    this.radarOptions.radarPlayersAllowed = this.radarOptions.radarAllowed;
                    this.radarOptions.radarMobsAllowed = this.radarOptions.radarAllowed;
                    this.mapOptions.cavesAllowed = true;
                    if (!mc.isIntegratedServerRunning()) {
                        this.newServerTime = System.currentTimeMillis();
                        this.checkMOTD = true;
                    }
                }

                this.map.newWorld(this.world);
            }
        }

        TickCounter.onTick(clock);
        this.persistentMap.onTick(mc);
    }

    private void checkPermissionMessages(MinecraftClient mc) {
        if (GameVariableAccessShim.getWorld() != null && mc.player != null && mc.inGameHud != null && System.currentTimeMillis() - this.newServerTime < 5000L) {
            UUID playerUUID = mc.player.getUuid();
            Object guiNewChat = mc.inGameHud.getChatHud();
            if (guiNewChat == null) {
                System.out.println("failed to get guiNewChat");
            } else {
                Object chatListObj = ReflectionUtils.getPrivateFieldValueByType(guiNewChat, ChatHud.class, List.class, 1);
                if (chatListObj == null) {
                    System.out.println("could not get chatlist");
                } else {
                    List<ChatHudLine> chatList = (List<ChatHudLine>) chatListObj;
                    boolean killRadar = false;
                    boolean killCaves = false;

                    for (int t = 0; t < chatList.size(); t++) {
                        ChatHudLine checkMe = chatList.get(t);
                        if (checkMe.equals(this.mostRecentLine)) {
                            break;
                        }

                        String msg = checkMe.getText().asFormattedString();
                        String error = "";
                        msg = msg.replaceAll("§r", "");
                        if (msg.contains("§3 §6 §3 §6 §3 §6 §d")) {
                            killCaves = true;
                            error = error + "Server disabled cavemapping.  ";
                        }

                        if (msg.contains("§3 §6 §3 §6 §3 §6 §e")) {
                            killRadar = true;
                            error = error + "Server disabled radar.  ";
                        }

                        if (!error.equals("")) {
                            this.passMessage = error;
                        }
                    }

                    this.radarOptions.radarAllowed = this.radarOptions.radarAllowed && (!killRadar || this.devUUID.equals(playerUUID));
                    this.radarOptions.radarPlayersAllowed = this.radarOptions.radarAllowed;
                    this.radarOptions.radarMobsAllowed = this.radarOptions.radarAllowed;
                    this.mapOptions.cavesAllowed = this.mapOptions.cavesAllowed && (!killCaves || this.devUUID.equals(playerUUID));
                    this.mostRecentLine = chatList.size() > 0 ? chatList.get(0) : null;
                }
            }
        } else if (System.currentTimeMillis() - this.newServerTime >= 5000L) {
            this.checkMOTD = false;
        }
    }

    @Override
    public MapSettingsManager getMapOptions() {
        return this.mapOptions;
    }

    @Override
    public RadarSettingsManager getRadarOptions() {
        return this.radarOptions;
    }

    @Override
    public PersistentMapSettingsManager getPersistentMapOptions() {
        return this.persistentMapOptions;
    }

    @Override
    public IMap getMap() {
        return this.map;
    }

    @Override
    public ISettingsAndLightingChangeNotifier getSettingsAndLightingChangeNotifier() {
        return this.settingsAndLightingChangeNotifier;
    }

    @Override
    public IRadar getRadar() {
        if (this.radarOptions.showRadar) {
            if (this.radarOptions.radarMode == 1) {
                return this.radarSimple;
            }

            if (this.radarOptions.radarMode == 2) {
                return this.radar;
            }
        }

        return null;
    }

    @Override
    public IColorManager getColorManager() {
        return this.colorManager;
    }

    @Override
    public IWaypointManager getWaypointManager() {
        return this.waypointManager;
    }

    @Override
    public IDimensionManager getDimensionManager() {
        return this.dimensionManager;
    }

    @Override
    public IPersistentMap getPersistentMap() {
        return this.persistentMap;
    }

    @Override
    public void setPermissions(
            boolean hasFullRadarPermission, boolean hasPlayersOnRadarPermission, boolean hasMobsOnRadarPermission, boolean hasCavemodePermission
    ) {
        boolean override = false;

        try {
            UUID devUUID = UUID.fromString("9b37abb9-2487-4712-bb96-21a1e0b2023c");
            UUID playerUUID = MinecraftClient.getInstance().player.getUuid();
            override = playerUUID.equals(devUUID);
        } catch (Exception var8) {
        }

        this.radarOptions.radarAllowed = hasFullRadarPermission || override;
        this.radarOptions.radarPlayersAllowed = hasPlayersOnRadarPermission || override;
        this.radarOptions.radarMobsAllowed = hasMobsOnRadarPermission || override;
        this.mapOptions.cavesAllowed = hasCavemodePermission || override;
    }

    @Override
    public synchronized void newSubWorldName(String name, boolean fromServer) {
        this.waypointManager.setSubworldName(name, fromServer);
        this.map.newWorldName();
    }

    @Override
    public synchronized void newSubWorldHash(String hash) {
        this.waypointManager.setSubworldHash(hash);
    }

    @Override
    public String getWorldSeed() {
        if (MinecraftClient.getInstance().isIntegratedServerRunning()) {
            String seed = "";

            try {
                seed = Long.toString(MinecraftClient.getInstance().getServer().getWorld(DimensionType.OVERWORLD).getSeed());
            } catch (Exception var3) {
            }

            return seed;
        } else {
            return this.waypointManager.getWorldSeed();
        }
    }

    @Override
    public void setWorldSeed(String newSeed) {
        if (!MinecraftClient.getInstance().isIntegratedServerRunning()) {
            this.waypointManager.setWorldSeed(newSeed);
        }
    }

    @Override
    public void sendPlayerMessageOnMainThread(String s) {
        this.passMessage = s;
    }

    @Override
    public WorldUpdateListener getWorldUpdateListener() {
        return this.worldUpdateListener;
    }
}
