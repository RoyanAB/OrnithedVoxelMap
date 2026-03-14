package com.mamiyaotaru.voxelmap.util;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.biome.Biome;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class BiomeRepository {
    private static final IdentityHashMap<Biome, Integer> biomeToInt = new IdentityHashMap<>(256);
    private static int count = 1;
    private static final Biome[] biomes = new Biome[65536];
    private static final Random generator = new Random();
    private static final HashMap<Integer, Integer> IDtoColor = new HashMap<>(256);
    private static final TreeMap<String, Integer> nameToColor = new TreeMap<>();
    private static boolean dirty = false;

    static {
        biomeToInt.put(Biomes.DEFAULT, 0);
        Arrays.fill(biomes, Biomes.DEFAULT);
    }

    public static int getBiomeId(Biome biome) {
        Integer id = biomeToInt.get(biome);
        if (id == null) {
            id = count;
            biomes[id] = biome;
            biomeToInt.put(biome, id);
            count++;
        }

        return id;
    }

    public static Biome betBiomeByID(int id) {
        return biomes[id];
    }

    public static void loadBiomeColors() {
        File saveDir = new File(Minecraft.getInstance().gameDir, "/voxelmap/");
        File settingsFile = new File(saveDir, "biomecolors.txt");
        if (settingsFile.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(settingsFile));

                String sCurrentLine;
                while ((sCurrentLine = br.readLine()) != null) {
                    String[] curLine = sCurrentLine.split("=");
                    if (curLine.length == 2) {
                        String name = curLine[0];
                        int color = 0;

                        try {
                            color = Integer.decode(curLine[1]);
                        } catch (NumberFormatException ex) {
                            System.out.println("Error decoding integer string for biome colors; " + curLine[1]);
                            color = 0;
                        }

                        if (nameToColor.put(name, color) != null) {
                            dirty = true;
                        }
                    }
                }

                br.close();
            } catch (Exception e) {
                System.err.println("biome load error: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        try {
            InputStream is = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation("voxelmap", "conf/biomecolors.txt")).getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                String[] curLine = sCurrentLine.split("=");
                if (curLine.length == 2) {
                    String name = curLine[0];
                    int color = 0;

                    try {
                        color = Integer.decode(curLine[1]);
                    } catch (NumberFormatException ex) {
                        System.out.println("Error decoding integer string for biome colors; " + curLine[1]);
                        color = 0;
                    }

                    if (nameToColor.get(name) == null) {
                        nameToColor.put(name, color);
                        dirty = true;
                    }
                }
            }

            br.close();
            is.close();
        } catch (IOException e) {
            System.out.println("Error loading biome color config file from litemod!");
            e.printStackTrace();
        }
    }

    public static void saveBiomeColors() {
        if (dirty) {
            File saveDir = new File(Minecraft.getInstance().gameDir, "/voxelmap/");
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }

            File settingsFile = new File(saveDir, "biomecolors.txt");

            try {
                PrintWriter out = new PrintWriter(new FileWriter(settingsFile));

                for (Entry<String, Integer> entry : nameToColor.entrySet()) {
                    String name = entry.getKey();
                    Integer color = entry.getValue();
                    String hexColor = Integer.toHexString(color);

                    while (hexColor.length() < 6) {
                        hexColor = "0" + hexColor;
                    }

                    hexColor = "0x" + hexColor;
                    out.println(name + "=" + hexColor);
                }

                out.close();
            } catch (Exception e) {
                System.err.println("biome save error: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        dirty = false;
    }

    public static int getBiomeColor(int biomeID) {
        Integer color = IDtoColor.get(biomeID);
        if (color == null) {
            Biome biome = IRegistry.BIOME.get(biomeID);
            if (biome != null) {
                String name = IRegistry.BIOME.getKey(biome).toString();
                color = nameToColor.get(name);
                if (color == null) {
                    color = nameToColor.get(biome.getDisplayName().getUnformattedComponentText());
                    if (color != null) {
                        nameToColor.remove(biome.getDisplayName().getUnformattedComponentText());
                        nameToColor.put(name, color);
                        dirty = true;
                    }
                }

                if (color == null) {
                    int r = generator.nextInt(255);
                    int g = generator.nextInt(255);
                    int b = generator.nextInt(255);
                    color = r << 16 | g << 8 | b;
                    nameToColor.put(name, color);
                    dirty = true;
                }
            } else {
                System.out.println("non biome");
                color = 0;
            }

            IDtoColor.put(biomeID, color);
        }

        return color;
    }
}
