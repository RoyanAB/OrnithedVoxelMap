package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.util.MutableBlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.awt.image.BufferedImage;

/**
 * Resolves the colors used to draw blocks on the minimap and world map.
 *
 * <p>The manager samples block and biome colors from the game's block models,
 * terrain atlas, and (optionally) Optifine/MCPatcher color properties, then
 * caches the resulting ARGB values for fast lookup while the map is being
 * rendered.
 */
public interface IColorManager {
	/**
	 * Notifies the manager that the game's resource manager was reloaded, so
	 * that cached colors will be recomputed the next time they are needed.
	 *
	 * @param resourceManager the resource manager that was reloaded
	 */
	void onResourceManagerReload(IResourceManager resourceManager);

	/**
	 * Returns the color picker image used by the waypoint color-selection GUI.
	 *
	 * @return the color picker image, or {@code null} if it could not be loaded
	 */
	BufferedImage getColorPicker();

	/**
	 * Renders a block state and returns its representative image, used to
	 * build radar icons (for example, a block worn as an entity's helmet).
	 *
	 * @param blockState the block state to render
	 * @param stack      the item stack associated with the block, or {@code null}
	 * @param world      the current world, or {@code null}
	 * @return the rendered block image, or {@code null} if it could not be produced
	 */
	BufferedImage getBlockImage(IBlockState blockState, ItemStack stack, World world);

	/**
	 * Checks whether the resource packs changed since the last check and, if
	 * so, reloads the cached colors.
	 *
	 * @return {@code true} if colors were reloaded
	 */
	boolean checkForChanges();

	/**
	 * Alpha-composites two ARGB colors using source-over blending, where
	 * {@code color1} is drawn over {@code color2}.
	 *
	 * @param color1 the top color
	 * @param color2 the bottom color
	 * @return the combined ARGB color
	 */
	int colorAdder(int color1, int color2);

	/**
	 * Multiplies the ARGB channels of two colors together (each channel is
	 * scaled back into the 0-255 range).
	 *
	 * @param color1 the first color
	 * @param color2 the second color
	 * @return the multiplied ARGB color
	 */
	int colorMultiplier(int color1, int color2);

	/**
	 * Returns the color for a block state with the game's default biome tint
	 * already applied, falling back to {@link #getBlockColor} when no
	 * pre-tinted color has been cached.
	 *
	 * @param blockPos     the position of the block (used when the color has to be
	 *                    resolved from the world)
	 * @param blockStateID the numeric block state ID
	 * @return the tinted ARGB color, or 0 if colors are not loaded
	 */
	int getBlockColorWithDefaultTint(MutableBlockPos blockPos, int blockStateID);

	/**
	 * Returns the color for a block state within a specific biome, falling
	 * back to the generic block color when no biome-specific color exists.
	 *
	 * @param blockPos     the position of the block
	 * @param blockStateID the numeric block state ID
	 * @param biomeID      the numeric biome ID, or -1 if unknown
	 * @return the ARGB color, or 0 if colors are not loaded
	 */
	int getBlockColor(MutableBlockPos blockPos, int blockStateID, int biomeID);

	/**
	 * Sets the color used for air and the sky.
	 *
	 * @param skyColor the new sky/air ARGB color
	 */
	void setSkyColor(int skyColor);

	/**
	 * Returns the current air (sky) color.
	 *
	 * @return the air ARGB color
	 */
	int getAirColor();

	/**
	 * Computes the biome tint to apply to a block state at a given position.
	 *
	 * <p>The tint is averaged over the 3x3 block neighborhood: a custom tint
	 * table is used when Optifine/MCPatcher tinting is available, otherwise
	 * the built-in biome colorizers are used for unloaded chunks and the
	 * game's own block-color tinting for loaded chunks.
	 *
	 * @param mapData      the map data used to look up biomes for unloaded chunks
	 * @param world        the current world
	 * @param blockState   the block state being tinted
	 * @param blockStateID the numeric block state ID
	 * @param blockPos     the position of the block being tinted
	 * @param loopBlockPos a reusable mutable position used internally
	 * @param startX       the world X coordinate of the map's origin
	 * @param startZ       the world Z coordinate of the map's origin
	 * @return the ARGB tint color, or -1 if no tint applies
	 */
	int getBiomeTint(AbstractMapData mapData, World world, IBlockState blockState, int blockStateID, MutableBlockPos blockPos, MutableBlockPos loopBlockPos, int startX, int startZ);
}
