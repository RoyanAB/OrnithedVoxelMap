package com.mamiyaotaru.voxelmap.interfaces;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

/**
 * The in-game minimap.
 *
 * <p>Extends {@link IChangeObserver} so that the minimap also reacts to block
 * and chunk changes in the world.
 */
public interface IMap extends IChangeObserver {
	/**
	 * Requests that the entire map be re-rendered on the next update.
	 *
	 * @param forceFullRender {@code true} to force a full re-render
	 */
	void forceFullRender(boolean forceFullRender);

	/**
	 * Renders the minimap for the current frame.
	 *
	 * @param mc the Minecraft client instance
	 */
	void drawMinimap(Minecraft mc);

	/**
	 * Returns the player's fractional X offset within the current map image,
	 * in map pixels, used to shift the map texture so the player stays
	 * centered.
	 *
	 * @return the fractional X offset
	 */
	float getPercentX();

	/**
	 * Returns the player's fractional Z offset within the current map image,
	 * in map pixels, used to shift the map texture so the player stays
	 * centered.
	 *
	 * @return the fractional Z offset
	 */
	float getPercentY();

	/**
	 * Called when a new world is loaded, resetting the map's state.
	 *
	 * @param world the newly loaded world
	 */
	void newWorld(World world);

	/**
	 * Called once per client tick from the main game loop.
	 *
	 * @param mc    the Minecraft client instance
	 * @param clock {@code true} when the game clock advanced this tick (i.e.
	 *              the game is not paused)
	 */
	void onTick(Minecraft mc, boolean clock);

	/**
	 * Called once per rendered frame, after the game overlay is drawn (only
	 * while the client is in a world), to handle input, update state, and
	 * draw the minimap.
	 *
	 * @param mc the Minecraft client instance
	 */
	void onTickInGame(Minecraft mc);

	/**
	 * Returns the current lightmap colors used to shade the map.
	 *
	 * @return the lightmap color array
	 */
	int[] getLightmapArray();

	/**
	 * Captures the current fog color so it can be used when computing the sky
	 * color for the map.
	 */
	void getFogColor();
}
