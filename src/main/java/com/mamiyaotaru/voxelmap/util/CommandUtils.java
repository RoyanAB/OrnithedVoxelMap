package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.gui.GuiAddWaypoint;
import com.mamiyaotaru.voxelmap.gui.GuiSelectPlayer;
import com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandUtils {
	private static final String newWaypointCommand = "/newWaypoint ";
	private static final int newWaypointCommandLength = "/newWaypoint ".length();
	private static final String teleportCommand = "/ztp ";
	private static final int teleportCommandLength = "/ztp ".length();
	private static final Random generator = new Random();
	public static Pattern pattern = Pattern.compile("\\[(\\w+\\s*:\\s*[-#]?[^\\[\\]]+)(,\\s*\\w+\\s*:\\s*[-#]?[^\\[\\]]+)+\\]", 2);

	public static boolean checkForWaypoints(ITextComponent chat, String message) {
		message = chat.getUnformattedText();
		ArrayList<String> waypointStrings = getWaypointStrings(message);
		if (waypointStrings.size() <= 0) {
			return true;
		}

		ArrayList<TextComponentString> textComponents = new ArrayList<>();
		int count = 0;

		for (String waypointString : waypointStrings) {
			int waypointStringLocation = message.indexOf(waypointString);
			if (waypointStringLocation > count) {
				textComponents.add(new TextComponentString(message.substring(count, waypointStringLocation)));
			}

			TextComponentString clickableWaypoint = new TextComponentString(waypointString);
			Style chatStyle = clickableWaypoint.getStyle();
			chatStyle.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/newWaypoint " + waypointString.substring(1, waypointString.length() - 1)));
			chatStyle.setColor(TextFormatting.AQUA);
			TextComponentString hover = new TextComponentString(
				I18nUtils.getString("minimap.waypointshare.tooltip1") + "\n" + I18nUtils.getString("minimap.waypointshare.tooltip2")
			);
			chatStyle.setHoverEvent(new HoverEvent(net.minecraft.util.text.event.HoverEvent.Action.SHOW_TEXT, hover));
			textComponents.add(clickableWaypoint);
			count = waypointStringLocation + waypointString.length();
		}

		if (count < message.length() - 1) {
			textComponents.add(new TextComponentString(message.substring(count)));
		}

		TextComponentString finalTextComponent = new TextComponentString("");

		for (ITextComponent textComponent : textComponents) {
			finalTextComponent.appendSibling(textComponent);
		}

		Minecraft.getMinecraft().player.sendMessage(finalTextComponent);
		return false;
	}

	public static ArrayList<String> getWaypointStrings(String message) {
		ArrayList<String> list = new ArrayList<>();
		if (message.contains("[") && message.contains("]")) {
			Matcher matcher = pattern.matcher(message);

			while (matcher.find()) {
				String match = matcher.group();
				if (createWaypointFromChat(match.substring(1, match.length() - 1)) != null) {
					list.add(match);
				}
			}
		}

		return list;
	}

	private static Waypoint createWaypointFromChat(String details) {
		Waypoint waypoint = null;
		String[] pairs = details.split(",");

		try {
			String name = "";
			Integer x = null;
			Integer z = null;
			Integer y = 64;
			boolean enabled = true;
			float red = generator.nextFloat();
			float green = generator.nextFloat();
			float blue = generator.nextFloat();
			String suffix = "";
			String world = "";
			TreeSet<Integer> dimensions = new TreeSet<>();

			for (int t = 0; t < pairs.length; t++) {
				int splitIndex = pairs[t].indexOf(":");
				if (splitIndex != -1) {
					String key = pairs[t].substring(0, splitIndex).toLowerCase().trim();
					String value = pairs[t].substring(splitIndex + 1).trim();
					if (key.equals("name")) {
						name = TextUtils.descrubName(value);
					} else if (key.equals("x")) {
						x = Integer.parseInt(value);
					} else if (key.equals("z")) {
						z = Integer.parseInt(value);
					} else if (key.equals("y")) {
						y = Integer.parseInt(value);
					} else if (key.equals("enabled")) {
						enabled = Boolean.parseBoolean(value);
					} else if (key.equals("red")) {
						red = Float.parseFloat(value);
					} else if (key.equals("green")) {
						green = Float.parseFloat(value);
					} else if (key.equals("blue")) {
						blue = Float.parseFloat(value);
					} else if (key.equals("color")) {
						int color = Integer.decode(value);
						red = (color >> 16 & 0xFF) / 255.0F;
						green = (color >> 8 & 0xFF) / 255.0F;
						blue = (color >> 0 & 0xFF) / 255.0F;
					} else if (key.equals("suffix") || key.equals("icon")) {
						suffix = value;
					} else if (key.equals("world")) {
						world = TextUtils.descrubName(value);
					} else if (key.equals("dimensions")) {
						String[] dimensionStrings = value.split("#");

						for (int s = 0; s < dimensionStrings.length; s++) {
							dimensions.add(Integer.parseInt(dimensionStrings[s]));
						}
					} else if (key.equals("dimension") || key.equals("dim")) {
						dimensions.add(Integer.parseInt(value));
					}
				}
			}

			if (world == "") {
				world = AbstractVoxelMap.getInstance().getWaypointManager().getCurrentSubworldDescriptor(false);
			}

			if (dimensions.size() == 0) {
				dimensions.add(Minecraft.getMinecraft().player.dimension);
			}

			if (x != null && z != null) {
				if (dimensions.size() == 1 && dimensions.first() == -1) {
					x = x * 8;
					z = z * 8;
				}

				waypoint = new Waypoint(name, x, z, y, enabled, red, green, blue, suffix, world, dimensions);
			}
		} catch (NumberFormatException e) {
			waypoint = null;
		}

		return waypoint;
	}

	public static void waypointClicked(String command) {
		boolean control = Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157);
		String details = command.substring(newWaypointCommandLength);
		Waypoint newWaypoint = createWaypointFromChat(details);
		if (newWaypoint != null) {
			for (Waypoint existingWaypoint : AbstractVoxelMap.getInstance().getWaypointManager().getWaypoints()) {
				if (newWaypoint.getX() == existingWaypoint.getX() && newWaypoint.getZ() == existingWaypoint.getZ()) {
					if (control) {
						Minecraft.getMinecraft().displayGuiScreen(new GuiAddWaypoint(null, AbstractVoxelMap.getInstance(), existingWaypoint, true));
					} else {
						AbstractVoxelMap.getInstance().getWaypointManager().setHighlightedWaypoint(existingWaypoint, false);
					}

					return;
				}
			}

			if (control) {
				Minecraft.getMinecraft().displayGuiScreen(new GuiAddWaypoint(null, AbstractVoxelMap.getInstance(), newWaypoint, false));
			} else {
				AbstractVoxelMap.getInstance().getWaypointManager().setHighlightedWaypoint(newWaypoint, false);
			}
		}
	}

	public static void sendWaypoint(Waypoint waypoint) {
		int dimension = Minecraft.getMinecraft().player.dimension;
		int color = ((int) (waypoint.red * 255.0F) & 0xFF) << 16 | ((int) (waypoint.green * 255.0F) & 0xFF) << 8 | (int) (waypoint.blue * 255.0F) & 0xFF;
		String hexColor = Integer.toHexString(color);

		while (hexColor.length() < 6) {
			hexColor = "0" + hexColor;
		}

		hexColor = "#" + hexColor;
		String world = AbstractVoxelMap.getInstance().getWaypointManager().getCurrentSubworldDescriptor(false);
		if (waypoint.world != null && waypoint.world != "") {
			world = waypoint.world;
		}

		String suffix = waypoint.imageSuffix;
		Object[] args = new Object[]{TextUtils.scrubNameRegex(waypoint.name), waypoint.getX(), waypoint.getY(), waypoint.getZ(), dimension};
		String message = String.format("[name:%s, x:%s, y:%s, z:%s, dim:%s", args);
		if (world != null && !world.equals("")) {
			message = message + ", world:" + world;
		}

		if (suffix != null && !suffix.equals("")) {
			message = message + ", icon:" + suffix;
		}

		message = message + "]";
		Minecraft.getMinecraft().displayGuiScreen(new GuiSelectPlayer(null, AbstractVoxelMap.getInstance(), message, true));
	}

	public static void sendCoordinate(int x, int y, int z) {
		String message = String.format("[x:%s, y:%s, z:%s]", x, y, z);
		Minecraft.getMinecraft().displayGuiScreen(new GuiSelectPlayer(null, AbstractVoxelMap.getInstance(), message, false));
	}

	public static void teleport(String command) {
		String details = command.substring(teleportCommandLength);

		for (Waypoint wp : AbstractVoxelMap.getInstance().getWaypointManager().getWaypoints()) {
			if (wp.name.equalsIgnoreCase(details) && wp.inDimension && wp.inWorld) {
				boolean mp = !Minecraft.getMinecraft().isIntegratedServerRunning();
				int y = wp.getY() > 0 ? wp.getY() : (Minecraft.getMinecraft().player.dimension != -1 ? 128 : 64);
				Minecraft.getMinecraft()
					.player
					.sendChatMessage("/tp " + Minecraft.getMinecraft().player.getName() + " " + wp.getX() + " " + y + " " + wp.getZ());
				if (mp) {
					Minecraft.getMinecraft().player.sendChatMessage("/tppos " + wp.getX() + " " + y + " " + wp.getZ());
				}

				return;
			}
		}
	}

	public static int getSafeHeight(int x, int y, int z, World worldObj) {
		boolean inNetherDimension = (worldObj.provider.getDimensionType() == DimensionType.NETHER);
		BlockPos blockPos = new BlockPos(x, y, z);
		Chunk chunk = worldObj.getChunkFromBlockCoords(blockPos);
		worldObj.getChunkProvider().provideChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
		if (inNetherDimension) {
			int safeY = -1;

			for (int t = 0; t < 127; t++) {
				if (y + t < 127 && isBlockStandable(worldObj, x, y + t, z) && isBlockOpen(worldObj, x, y + t + 1, z) && isBlockOpen(worldObj, x, y + t + 2, z)) {
					safeY = y + t + 1;
					t = 128;
				}

				if (y - t > 0 && isBlockStandable(worldObj, x, y - t, z) && isBlockOpen(worldObj, x, y - t + 1, z) && isBlockOpen(worldObj, x, y - t + 2, z)) {
					safeY = y - t + 1;
					t = 128;
				}
			}

			y = safeY;
		} else if (y <= 0) {
			y = chunk.getHeight(blockPos);
		}

		return y;
	}

	private static boolean isBlockStandable(World worldObj, int par1, int par2, int par3) {
		BlockPos blockPos = new BlockPos(par1, par2, par3);
		IBlockState blockState = worldObj.getBlockState(blockPos);
		Block block = blockState.getBlock();
		if (blockState.getMaterial() == Material.AIR) {
			return block instanceof BlockFence;
		} else {
			return block != null && blockState.getMaterial().isOpaque();
		}
	}

	private static boolean isBlockOpen(World worldObj, int par1, int par2, int par3) {
		BlockPos blockPos = new BlockPos(par1, par2, par3);
		IBlockState blockState = worldObj.getBlockState(blockPos);
		Block block = blockState.getBlock();
		if (blockState.getMaterial() == Material.AIR) {
			return !(block instanceof BlockFence);
		} else {
			return block == null || blockState.getLightOpacity() == 0;
		}
	}
}
