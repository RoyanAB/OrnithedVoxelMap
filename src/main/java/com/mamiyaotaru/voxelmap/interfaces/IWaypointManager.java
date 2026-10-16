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

	void deleteWaypoint(Waypoint var1);

	void saveWaypoints();

	void addWaypoint(Waypoint var1);

	void check2dWaypoints();

	void handleDeath();

	void newWorld(World var1);

	void setConnectedRealm(String var1);

	String getCurrentWorldName();

	TreeSet<String> getKnownSubworldNames();

	boolean receivedAutoSubworldName();

	boolean isMultiworld();

	void setSubworldName(String var1, boolean var2);

	void setSubworldHash(String var1);

	void changeSubworldName(String var1, String var2);

	void deleteSubworld(String var1);

	void setOldNorth(boolean var1);

	String getCurrentSubworldDescriptor(boolean var1);

	void renderWaypoints(float var1);

	void onResourceManagerReload(IResourceManager var1);

	TextureAtlas getTextureAtlas();

	TextureAtlas getTextureAtlasChooser();

	void setHighlightedWaypoint(Waypoint var1, boolean var2);

	Waypoint getHighlightedWaypoint();

	String getWorldSeed();

	void setWorldSeed(String var1);

	BackgroundImageInfo getBackgroundImageInfo();
}
