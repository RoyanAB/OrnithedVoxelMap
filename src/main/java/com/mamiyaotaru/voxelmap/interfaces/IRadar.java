package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.util.LayoutVariables;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;

/**
 * Draws the entity radar overlay on top of the minimap.
 *
 * <p>Implemented by both the full radar (which renders mob and player icons)
 * and the simple radar (which renders generic contact pings).
 */
public interface IRadar {
	/**
	 * Notifies the radar that resources were reloaded so it can rebuild its
	 * icon atlas.
	 *
	 * @param resourceManager the resource manager that was reloaded
	 */
	void onResourceManagerReload(IResourceManager resourceManager);

	/**
	 * Called each rendered frame, while the minimap is being drawn (and not in
	 * fullscreen mode), to update and render the radar overlay.
	 *
	 * @param mc              the Minecraft client instance
	 * @param layoutVariables the minimap's current layout and scale information
	 */
	void OnTickInGame(Minecraft mc, LayoutVariables layoutVariables);
}
