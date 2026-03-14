package com.mamiyaotaru.voxelmap.util;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonExtensionBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BlockRepository {
    public static Block air;
    public static Block voidAir;
    public static Block caveAir;
    public static int airID = 0;
    public static int voidAirID = 0;
    public static int caveAirID = 0;
    public static PistonExtensionBlock pistonTechBlock;
    public static Block water;
    public static Block lava;
    public static Block ice;
    public static Block grassBlock;
    public static Block oakLeaves;
    public static Block spruceLeaves;
    public static Block birchLeaves;
    public static Block jungleLeaves;
    public static Block acaciaLeaves;
    public static Block darkOakLeaves;
    public static Block grass;
    public static Block fern;
    public static Block tallGrass;
    public static Block largeFern;
    public static Block reeds;
    public static Block vine;
    public static Block lilypad;
    public static Block tallFlower;
    public static Block cobweb;
    public static Block stickyPiston;
    public static Block piston;
    public static Block redstone;
    public static Block sign;
    public static Block ladder;
    public static Block wallSign;
    public static Block oakDoor;
    public static Block ironDoor;
    public static Block spruceDoor;
    public static Block birchDoor;
    public static Block jungleDoor;
    public static Block darkOakDoor;
    public static Block acaciaDoor;
    public static Block barrier;
    public static Block chorusPlant;
    public static Block chorusFlower;
    public static FluidState dry = Fluids.EMPTY.getDefaultState();
    public static HashSet<Block> biomeBlocks;
    public static Block[] biomeBlocksArray = new Block[]{
            grassBlock,
            oakLeaves,
            spruceLeaves,
            birchLeaves,
            jungleLeaves,
            acaciaLeaves,
            darkOakLeaves,
            grass,
            fern,
            tallGrass,
            largeFern,
            reeds,
            vine,
            lilypad,
            tallFlower,
            water
    };
    public static HashSet<Block> shapedBlocks;
    public static Block[] doorsArray = new Block[]{oakDoor, ironDoor, spruceDoor, birchDoor, jungleDoor, darkOakDoor, acaciaDoor};
    public static Block[] shapedBlocksArray = new Block[]{sign, oakDoor, ladder, wallSign, ironDoor, vine};
    private static final Reference2IntOpenHashMap<BlockState> stateToInt = new Reference2IntOpenHashMap(1024);
    private static final ReferenceArrayList<BlockState> blockStates = new ReferenceArrayList(16384);
    private static int count = 1;
    private static final ReadWriteLock incrementLock = new ReentrantReadWriteLock();

    static {
        stateToInt.defaultReturnValue(-1);
        BlockState airBlockState = Blocks.AIR.getDefaultState();
        stateToInt.put(airBlockState, 0);
        blockStates.add(airBlockState);
    }

    public static void getBlocks() {
        air = Blocks.AIR;
        airID = getStateId(air.getDefaultState());
        voidAir = Blocks.VOID_AIR;
        voidAirID = getStateId(voidAir.getDefaultState());
        caveAir = Blocks.CAVE_AIR;
        caveAirID = getStateId(caveAir.getDefaultState());
        pistonTechBlock = (PistonExtensionBlock) Blocks.MOVING_PISTON;
        water = Blocks.WATER;
        lava = Blocks.LAVA;
        ice = Blocks.ICE;
        grassBlock = Blocks.GRASS_BLOCK;
        oakLeaves = Blocks.OAK_LEAVES;
        spruceLeaves = Blocks.SPRUCE_LEAVES;
        birchLeaves = Blocks.BIRCH_LEAVES;
        jungleLeaves = Blocks.JUNGLE_LEAVES;
        acaciaLeaves = Blocks.ACACIA_LEAVES;
        darkOakLeaves = Blocks.DARK_OAK_LEAVES;
        grass = Blocks.GRASS;
        fern = Blocks.FERN;
        tallGrass = Blocks.TALL_GRASS;
        largeFern = Blocks.LARGE_FERN;
        reeds = Blocks.SUGAR_CANE;
        vine = Blocks.VINE;
        lilypad = Blocks.LILY_PAD;
        cobweb = Blocks.COBWEB;
        stickyPiston = Blocks.STICKY_PISTON;
        piston = Blocks.PISTON;
        redstone = Blocks.REDSTONE_WIRE;
        sign = Blocks.OAK_SIGN;
        wallSign = Blocks.OAK_WALL_SIGN;
        ladder = Blocks.LADDER;
        oakDoor = Blocks.OAK_DOOR;
        ironDoor = Blocks.IRON_DOOR;
        spruceDoor = Blocks.SPRUCE_DOOR;
        birchDoor = Blocks.BIRCH_DOOR;
        jungleDoor = Blocks.JUNGLE_DOOR;
        darkOakDoor = Blocks.DARK_OAK_DOOR;
        acaciaDoor = Blocks.ACACIA_DOOR;
        barrier = Blocks.BARRIER;
        chorusPlant = Blocks.CHORUS_PLANT;
        chorusFlower = Blocks.CHORUS_FLOWER;
        biomeBlocksArray = new Block[]{
                grassBlock,
                oakLeaves,
                spruceLeaves,
                birchLeaves,
                jungleLeaves,
                acaciaLeaves,
                darkOakLeaves,
                grass,
                fern,
                tallGrass,
                largeFern,
                reeds,
                vine,
                lilypad,
                tallFlower,
                water
        };
        biomeBlocks = new HashSet<>(Arrays.asList(biomeBlocksArray));
        doorsArray = new Block[]{oakDoor, ironDoor, spruceDoor, birchDoor, jungleDoor, darkOakDoor, acaciaDoor};
        shapedBlocksArray = new Block[]{sign, ladder, wallSign, vine};
        shapedBlocks = new HashSet<>(Arrays.asList(shapedBlocksArray));
        shapedBlocks.addAll(Arrays.asList(doorsArray));
    }

    public static int getStateId(BlockState blockState) {
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

    public static BlockState getStateById(int id) {
        return blockStates.get(id);
    }
}
