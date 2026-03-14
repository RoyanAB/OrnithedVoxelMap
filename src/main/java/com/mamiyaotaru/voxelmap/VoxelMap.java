package com.mamiyaotaru.voxelmap;

import com.google.common.base.Charsets;
import com.mamiyaotaru.voxelmap.interfaces.*;
import com.mamiyaotaru.voxelmap.persistent.PersistentMap;
import com.mamiyaotaru.voxelmap.persistent.PersistentMapSettingsManager;
import com.mamiyaotaru.voxelmap.util.*;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.List;
import java.util.UUID;

public class VoxelMap extends AbstractVoxelMap implements IResourceManagerReloadListener {
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
    private World world;
    private String worldName = "";
    private Long newServerTime = 0L;
    private boolean checkMOTD = false;
    private ChatLine mostRecentLine = null;
    private final UUID devUUID = UUID.fromString("9b37abb9-2487-4712-bb96-21a1e0b2023c");
    private String passMessage = null;

    public VoxelMap() {
        instance = this;
    }

    public void lateInit(boolean showUnderMenus, boolean isFair) {
        GLUtils.textureManager = Minecraft.getInstance().getTextureManager();
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
        IReloadableResourceManager resourceManager = (IReloadableResourceManager) Minecraft.getInstance().getResourceManager();
        resourceManager.addReloadListener(this);
    }

    public void onResourceManagerReload(IResourceManager resourceManager) {
        this.waypointManager.onResourceManagerReload(resourceManager);
        if (this.radar != null) {
            this.radar.onResourceManagerReload(resourceManager);
        }

        if (this.radarSimple != null) {
            this.radarSimple.onResourceManagerReload(resourceManager);
        }

        this.colorManager.onResourceManagerReload(resourceManager);
    }

    public void onTickInGame(Minecraft mc) {
        this.map.onTickInGame(mc);
        if (this.passMessage != null) {
            mc.player.sendMessage(new TextComponentString(this.passMessage));
            this.passMessage = null;
        }
    }

    public void onSetupCameraTransform() {
        this.map.getFogColor();
    }

    public void onTick(Minecraft mc, boolean clock) {
        if (this.checkMOTD) {
            this.checkPermissionMessages(mc);
        }

        if (GameVariableAccessShim.getWorld() != null && !GameVariableAccessShim.getWorld().equals(this.world)
                || this.world != null && !this.world.equals(GameVariableAccessShim.getWorld())) {
            if (this.world != null) {
                this.world.removeEventListener(this.worldUpdateListener);
            }

            this.world = GameVariableAccessShim.getWorld();
            this.waypointManager.newWorld(this.world);
            this.persistentMap.newWorld(this.world);
            if (this.world != null) {
                String channelList = "worldinfo:world_id";
                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
                buffer.writeBytes(channelList.getBytes(Charsets.UTF_8));
                mc.player.connection.sendPacket(new CPacketCustomPayload(new ResourceLocation("register"), buffer));
//            ForgeModVoxelMap.WORLD_ID.sendToServer(new WorldIDPacket());
                mc.player.getLocationSkin();
                java.util.Map<Type, MinecraftProfileTexture> skinMap = mc.getSkinManager().loadSkinFromCache(mc.player.getGameProfile());
                if (skinMap.containsKey(Type.SKIN)) {
                    mc.getSkinManager().loadSkin(skinMap.get(Type.SKIN), Type.SKIN);
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
                this.world.addEventListener(this.worldUpdateListener);
            }
        }

        this.map.onTick(mc, clock);
        this.persistentMap.onTick(mc);
    }

    private void checkPermissionMessages(Minecraft mc) {
        if (GameVariableAccessShim.getWorld() != null && mc.player != null && mc.ingameGUI != null && System.currentTimeMillis() - this.newServerTime < 5000L) {
            UUID playerUUID = mc.player.getUniqueID();
            Object guiNewChat = mc.ingameGUI.getChatGUI();
            if (guiNewChat == null) {
                System.out.println("failed to get guiNewChat");
            } else {
                Object chatListObj = ReflectionUtils.getPrivateFieldValueByType(guiNewChat, GuiNewChat.class, List.class, 1);
                if (chatListObj == null) {
                    System.out.println("could not get chatlist");
                } else {
                    List<ChatLine> chatList = (List<ChatLine>) chatListObj;
                    boolean killRadar = false;
                    boolean killCaves = false;

                    for (int t = 0; t < chatList.size(); t++) {
                        ChatLine checkMe = chatList.get(t);
                        if (checkMe.equals(this.mostRecentLine)) {
                            break;
                        }

                        String msg = checkMe.getChatComponent().getFormattedText();
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
        } else {
            this.checkMOTD = false;
        }
    }

    public void setConnectedRealm(String id) {
        this.waypointManager.setConnectedRealm(id);
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
            UUID playerUUID = Minecraft.getInstance().player.getUniqueID();
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
        if (Minecraft.getInstance().isIntegratedServerRunning()) {
            String seed = "";

            try {
                seed = Long.toString(Minecraft.getInstance().getIntegratedServer().getWorld(DimensionType.OVERWORLD).getSeed());
            } catch (Exception var3) {
            }

            return seed;
        } else {
            return this.waypointManager.getWorldSeed();
        }
    }

    @Override
    public void setWorldSeed(String newSeed) {
        if (!Minecraft.getInstance().isIntegratedServerRunning()) {
            this.waypointManager.setWorldSeed(newSeed);
        }
    }

    @Override
    public void sendPlayerMessageOnMainThread(String s) {
        this.passMessage = s;
    }
}
