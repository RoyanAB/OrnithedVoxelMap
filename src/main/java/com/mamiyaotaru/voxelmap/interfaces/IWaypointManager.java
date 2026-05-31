package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.textures.TextureAtlas;
import com.mamiyaotaru.voxelmap.util.BackgroundImageInfo;
import com.mamiyaotaru.voxelmap.util.Waypoint;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.TreeSet;

@SuppressWarnings("unused")
public interface IWaypointManager {
	ArrayList<Waypoint> getWaypoints();

	void deleteWaypoint(Waypoint waypoint);

	void saveWaypoints();

	void addWaypoint(Waypoint waypoint);

	void check2dWaypoints();

	void handleDeath();

	void newWorld(World world);

	void setConnectedRealm(String string);

	String getCurrentWorldName();

	TreeSet<String> getKnownSubworldNames();

	boolean receivedAutoSubworldName();

	boolean isMultiworld();

	void setSubworldName(String name, boolean b);

	void setSubworldHash(String subworldHash);

	void changeSubworldName(String string1, String string2);

	void deleteSubworld(String name);

	void setOldNorth(boolean isOldNorth);

	String getCurrentSubworldDescriptor(boolean b);

	void renderWaypoints(float partialTicks);

	void onResourceManagerReload(IResourceManager iResourceManager);

	TextureAtlas getTextureAtlas();

	TextureAtlas getTextureAtlasChooser();

	void setHighlightedWaypoint(Waypoint waypoint, boolean b);

	Waypoint getHighlightedWaypoint();

	String getWorldSeed();

	void setWorldSeed(String string);

	BackgroundImageInfo getBackgroundImageInfo();
}
