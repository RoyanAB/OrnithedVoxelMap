package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.textures.TextureAtlas;
import com.mamiyaotaru.voxelmap.util.BackgroundImageInfo;
import com.mamiyaotaru.voxelmap.util.Waypoint;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.TreeSet;

public interface IWaypointManager {
	ArrayList<Waypoint> getWaypoints();

	void deleteWaypoint(Waypoint waypoint);

	void saveWaypoints();

	void addWaypoint(Waypoint waypoint);

	void check2dWaypoints();

	void handleDeath();

	void newWorld(World world);

	void setConnectedRealm(String realmName);

	String getCurrentWorldName();

	TreeSet<String> getKnownSubworldNames();

	boolean receivedAutoSubworldName();

	boolean isMultiworld();

	void setSubworldName(String subworldName, boolean fromServer);

	void setSubworldHash(String subworldHash);

	void changeSubworldName(String oldName, String newName);

	void deleteSubworld(String subworldName);

	void setOldNorth(boolean oldNorth);

	String getCurrentSubworldDescriptor(boolean autoSubworld);

	void renderWaypoints(float partialTicks);

	void onResourceManagerReload(IResourceManager resourceManager);

	TextureAtlas getTextureAtlas();

	TextureAtlas getTextureAtlasChooser();

	void setHighlightedWaypoint(Waypoint waypoint, boolean toggle);

	Waypoint getHighlightedWaypoint();

	String getWorldSeed();

	void setWorldSeed(String seed);

	BackgroundImageInfo getBackgroundImageInfo();
}
