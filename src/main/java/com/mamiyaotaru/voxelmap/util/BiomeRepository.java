package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.ornithe.VoxelMapMod;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

import java.io.*;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

public class BiomeRepository {
	private static final Random generator = new Random();
	private static final HashMap<Integer, Integer> IDtoColor = new HashMap<>(256);
	private static final TreeMap<String, Integer> nameToColor = new TreeMap<>();
	private static boolean dirty = false;

	public static void loadBiomeColors() {
		File saveDir = new File(Minecraft.getMinecraft().gameDir, "/voxelmap/");
		File settingsFile = new File(saveDir, "biomeColors.txt");
		if (settingsFile.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(settingsFile));

				String sCurrentLine;
				while ((sCurrentLine = br.readLine()) != null) {
					String[] curLine = sCurrentLine.split(":");
					if (curLine.length == 2) {
						String name = curLine[0];
						int color;

						try {
							color = Integer.decode(curLine[1]);
						} catch (NumberFormatException ex) {
							VoxelMapMod.LOGGER.info("Error decoding integer string for biome colors; {}", curLine[1]);
							color = 0;
						}

						if (nameToColor.put(name, color) != null) {
							dirty = true;
						}
					}
				}

				br.close();
			} catch (Exception e) {
				VoxelMapMod.LOGGER.error("biome load error: {}", e.getLocalizedMessage());
				e.printStackTrace();
			}
		}

		try {
			InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("voxelmap", "conf/biomecolors.txt")).getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				String[] curLine = sCurrentLine.split(":");
				if (curLine.length == 2) {
					String name = curLine[0];
					int color;

					try {
						color = Integer.decode(curLine[1]);
					} catch (NumberFormatException ex) {
						VoxelMapMod.LOGGER.info("Error decoding integer string for biome colors; {}", curLine[1]);
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
			VoxelMapMod.LOGGER.info("Error loading biome color config file!");
			e.printStackTrace();
		}
	}

	public static void saveBiomeColors() {
		if (dirty) {
			File saveDir = new File(Minecraft.getMinecraft().gameDir, "/voxelmap/");
			if (!saveDir.exists()) {
				saveDir.mkdirs();
			}

			File settingsFile = new File(saveDir, "biomeColors.txt");

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
					out.println(name + ":" + hexColor);
				}

				out.close();
			} catch (Exception e) {
				VoxelMapMod.LOGGER.error("biome save error: {}", e.getLocalizedMessage());
				e.printStackTrace();
			}
		}

		dirty = false;
	}

	public static int getBiomeColor(int biomeID) {
		Integer color = IDtoColor.get(biomeID);
		if (color == null) {
			Biome biome = Biome.getBiomeForId(biomeID);
			if (biome != null) {
				String name = biome.getBiomeName();
				color = nameToColor.get(name);
				if (color == null) {
					int r = generator.nextInt(255);
					int g = generator.nextInt(255);
					int b = generator.nextInt(255);
					color = r << 16 | g << 8 | b;
					nameToColor.put(name, color);
					dirty = true;
				}
			} else {
				VoxelMapMod.LOGGER.info("non biome");
				color = 0;
			}

			IDtoColor.put(biomeID, color);
		}

		return color;
	}
}
