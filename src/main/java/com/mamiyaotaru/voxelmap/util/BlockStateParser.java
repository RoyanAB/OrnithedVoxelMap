package com.mamiyaotaru.voxelmap.util;

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

@SuppressWarnings("unused")
public class BlockStateParser {
	public static void parseLine(String line, BiMap<IBlockState, Integer> map) {
		String[] lineParts = line.split(" ");
		int id = Integer.parseInt(lineParts[0]);
		IBlockState blockState = parseStateString(lineParts[1]);
		if (blockState != null) {
			map.forcePut(blockState, id);
		}
	}

	private static IBlockState parseStateString(String stateString) {
		IBlockState blockState = null;
		int bracketIndex = stateString.indexOf("[");
		String resourceString = stateString.substring(0, bracketIndex == -1 ? stateString.length() : bracketIndex);
		String[] resourceStringParts = resourceString.split(":");
		ResourceLocation resourceLocation = null;
		if (resourceStringParts.length == 1) {
			resourceLocation = new ResourceLocation(resourceStringParts[0]);
		} else if (resourceStringParts.length == 2) {
			resourceLocation = new ResourceLocation(resourceStringParts[0], resourceStringParts[1]);
		}

		Block block = Block.REGISTRY.getObject(resourceLocation);
		if (block != Blocks.AIR || resourceString.equals("minecraft:air")) {
			blockState = block.getDefaultState();
			if (bracketIndex != -1) {
				String propertiesString = stateString.substring(stateString.indexOf("[") + 1, stateString.lastIndexOf("]"));
				String[] propertiesStringParts = propertiesString.split(",");

				for (String propertiesStringPart : propertiesStringParts) {
					String[] propertyStringParts = propertiesStringPart.split("=");
					IProperty<?> property = block.getBlockState().getProperty(propertyStringParts[0]);
					if (property != null) {
						blockState = withValue(blockState, property, propertyStringParts[1]);
					}
				}
			}
		}

		return blockState;
	}

	private static <T extends Comparable<T>> IBlockState withValue(IBlockState blockState, IProperty<T> property, String valueString) {
		Optional<T> value = property.parseValue(valueString);
		if (value.isPresent()) {
			blockState = blockState.withProperty(property, value.get());
		}

		return blockState;
	}
}
