package com.mamiyaotaru.voxelmap.util;

import com.google.common.collect.BiMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.state.IProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;

import java.util.Optional;

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
        int curlyBracketOpenIndex = resourceString.indexOf("{");
        int curlyBracketCloseIndex = resourceString.indexOf("}");
        resourceString = resourceString.substring(
                curlyBracketOpenIndex == -1 ? 0 : curlyBracketOpenIndex + 1, curlyBracketCloseIndex == -1 ? resourceString.length() : curlyBracketCloseIndex
        );
        String[] resourceStringParts = resourceString.split(":");
        ResourceLocation resourceLocation = null;
        if (resourceStringParts.length == 1) {
            resourceLocation = new ResourceLocation(resourceStringParts[0]);
        } else if (resourceStringParts.length == 2) {
            resourceLocation = new ResourceLocation(resourceStringParts[0], resourceStringParts[1]);
        }

        Block block = IRegistry.BLOCK.getOrDefault(resourceLocation);
        if (block != Blocks.AIR || resourceString.equals("minecraft:air")) {
            blockState = block.getDefaultState();
            if (bracketIndex != -1) {
                String propertiesString = stateString.substring(stateString.indexOf("[") + 1, stateString.lastIndexOf("]"));
                String[] propertiesStringParts = propertiesString.split(",");

                for (int t = 0; t < propertiesStringParts.length; t++) {
                    String[] propertyStringParts = propertiesStringParts[t].split("=");
                    IProperty<?> property = block.getStateContainer().getProperty(propertyStringParts[0]);
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
            blockState = blockState.with(property, value.get());
        }

        return blockState;
    }
}
