package com.mamiyaotaru.voxelmap.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockPistonMoving;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BlockRepository {
	public static Block air;
	public static int airID = 0;
	public static BlockPistonMoving pistonTechBlock;
	public static BlockLiquid water;
	public static BlockLiquid flowingWater;
	public static Block lava;
	public static Block flowingLava;
	public static Block ice;
	public static Block grass;
	public static Block leaves;
	public static Block tallGrass;
	public static Block reeds;
	public static Block vine;
	public static Block lilypad;
	public static Block leaves2;
	public static Block tallFlower;
	public static Block cobweb;
	public static Block stickyPiston;
	public static Block piston;
	public static Block redstone;
	public static Block sign;
	public static Block ladder;
	public static Block wallSign;
	public static Block woodDoor;
	public static Block ironDoor;
	public static Block spruceDoor;
	public static Block birchDoor;
	public static Block jungleDoor;
	public static Block darkOakDoor;
	public static Block acaciaDoor;
	public static Block barrier;
	public static Block chorusPlant;
	public static Block chorusFlower;
	public static HashSet<Block> biomeBlocks;
	public static Block[] biomeBlocksArray = new Block[]{grass, leaves, tallGrass, reeds, vine, lilypad, leaves2, tallFlower, water, flowingWater};
	public static HashSet<Block> shapedBlocks;
	public static Block[] doorsArray = new Block[]{woodDoor, ironDoor, spruceDoor, birchDoor, jungleDoor, darkOakDoor, acaciaDoor};
	public static Block[] shapedBlocksArray = new Block[]{sign, woodDoor, ladder, wallSign, ironDoor, vine};
	public static BiMap<IBlockState, Integer> defaultStorageStateIDs = HashBiMap.create(2000);
	public static BiMap<Block, Integer> blockIDs = HashBiMap.create(256);
	private static final Reference2IntOpenHashMap<IBlockState> stateToInt = new Reference2IntOpenHashMap(1024);
	private static final ReferenceArrayList<IBlockState> blockStates = new ReferenceArrayList(16384);
	private static int count = 1;
	private static final ReadWriteLock incrementLock = new ReentrantReadWriteLock();

	static {
		stateToInt.defaultReturnValue(-1);
		IBlockState airBlockState = Blocks.AIR.getDefaultState();
		stateToInt.put(airBlockState, 0);
		blockStates.add(airBlockState);
	}

	public static void getBlocks() {
		air = Block.getBlockFromName("minecraft:air");
		airID = getStateId(air.getDefaultState());
		pistonTechBlock = (BlockPistonMoving) Block.getBlockFromName("minecraft:piston_extension");
		water = (BlockLiquid) Block.getBlockFromName("minecraft:water");
		flowingWater = (BlockLiquid) Block.getBlockFromName("minecraft:flowing_water");
		lava = Block.getBlockFromName("minecraft:lava");
		flowingLava = Block.getBlockFromName("minecraft:flowing_lava");
		ice = Block.getBlockFromName("minecraft:ice");
		grass = Block.getBlockFromName("minecraft:grass");
		leaves = Block.getBlockFromName("minecraft:leaves");
		tallGrass = Block.getBlockFromName("minecraft:tallgrass");
		reeds = Block.getBlockFromName("minecraft:reeds");
		vine = Block.getBlockFromName("minecraft:vine");
		lilypad = Block.getBlockFromName("minecraft:waterlily");
		leaves2 = Block.getBlockFromName("minecraft:leaves2");
		tallFlower = Block.getBlockFromName("minecraft:double_plant");
		cobweb = Block.getBlockFromName("minecraft:web");
		stickyPiston = Block.getBlockFromName("minecraft:sticky_piston");
		piston = Block.getBlockFromName("minecraft:piston");
		redstone = Block.getBlockFromName("minecraft:redstone_wire");
		sign = Block.getBlockFromName("minecraft:standing_sign");
		ladder = Block.getBlockFromName("minecraft:ladder");
		wallSign = Block.getBlockFromName("minecraft:wall_sign");
		woodDoor = Block.getBlockFromName("minecraft:wooden_door");
		ironDoor = Block.getBlockFromName("minecraft:iron_door");
		spruceDoor = Block.getBlockFromName("minecraft:spruce_door");
		birchDoor = Block.getBlockFromName("minecraft:birch_door");
		jungleDoor = Block.getBlockFromName("minecraft:jungle_door");
		darkOakDoor = Block.getBlockFromName("minecraft:dark_oak_door");
		acaciaDoor = Block.getBlockFromName("minecraft:acacia_door");
		barrier = Block.getBlockFromName("minecraft:barrier");
		chorusPlant = Block.getBlockFromName("minecraft:chorus_plant");
		chorusFlower = Block.getBlockFromName("minecraft:chorus_flower");
		biomeBlocksArray = new Block[]{grass, leaves, tallGrass, reeds, vine, lilypad, leaves2, tallFlower, water, flowingWater};
		biomeBlocks = new HashSet<>(Arrays.asList(biomeBlocksArray));
		doorsArray = new Block[]{woodDoor, ironDoor, spruceDoor, birchDoor, jungleDoor, darkOakDoor, acaciaDoor};
		shapedBlocksArray = new Block[]{sign, ladder, wallSign, vine};
		shapedBlocks = new HashSet<>(Arrays.asList(shapedBlocksArray));
		shapedBlocks.addAll(Arrays.asList(doorsArray));
	}

	public static int getStateId(IBlockState blockState) {
		incrementLock.readLock().lock();
		int id = stateToInt.getInt(blockState);
		incrementLock.readLock().unlock();
		if (id == -1) {
			incrementLock.writeLock().lock();
			id = stateToInt.getInt(blockState);
			if (id == -1) {
				id = count;
				blockStates.add(blockState);
				stateToInt.put(blockState, id);
				count++;
			}

			incrementLock.writeLock().unlock();
		}

		return id;
	}

	public static IBlockState getStateById(int id) {
		return blockStates.get(id);
	}

	public static void loadDefaultStorageStateIDs() {
		for (ResourceLocation resourceLocation : Block.REGISTRY.getKeys()) {
			Block block = Block.REGISTRY.getObject(resourceLocation);
			if (block != Blocks.AIR || resourceLocation.getNamespace().equals("minecraft") && resourceLocation.getPath().equals("air")) {
				ImmutableList<IBlockState> blockStates = block.getBlockState().getValidStates();
				Iterator<IBlockState> stateIterator = blockStates.iterator();

				while (stateIterator.hasNext()) {
					IBlockState blockState = stateIterator.next();
					defaultStorageStateIDs.forcePut(blockState, Block.getStateId(blockState));
				}
			}
		}
	}

	public static void loadBlockIDs() {
		try {
			String filename = "conf/blockIDs.txt";
			InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("voxelmap", filename)).getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				try {
					parseLine(sCurrentLine, blockIDs);
				} catch (Exception ex) {
					System.out.println("Error parsing blockID storage line: " + sCurrentLine);
				}
			}

			br.close();
			is.close();
		} catch (IOException e) {
			System.out.println("Error loading old block IDs config file from litemod!");
			e.printStackTrace();
		}
	}

	public static void parseLine(String line, BiMap<Block, Integer> map) {
		String[] lineParts = line.split(" ");
		int id = Integer.parseInt(lineParts[0]);
		Block block = Block.getBlockFromName(lineParts[1]);
		if (block != null) {
			map.forcePut(block, id);
		}
	}
}
