package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.util.I18nUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Mouse;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Iterator;

public class GuiButtonRowListPlayers extends GuiListExtended {
	final GuiSelectPlayer parentGui;
	private final ArrayList<GuiButtonRowListPlayers.Row> rows = new ArrayList<>();
	private final Minecraft mc = Minecraft.getMinecraft();
	private final ArrayList<NetworkPlayerInfo> players;
	GuiButtonRowListPlayers.Row everyoneRow;
	private ArrayList<NetworkPlayerInfo> playersFiltered;

	public GuiButtonRowListPlayers(GuiSelectPlayer guiSelectPlayer) {
		super(Minecraft.getMinecraft(), guiSelectPlayer.getWidth(), guiSelectPlayer.getHeight(), 89, guiSelectPlayer.getHeight() - 65 + 4, 25);
		this.parentGui = guiSelectPlayer;
		NetHandlerPlayClient netHandlerPlayClient = Minecraft.getMinecraft().player.connection;
		this.players = new ArrayList<>(netHandlerPlayClient.getPlayerInfoMap());
		this.sort();
		GuiButton everyoneButton = new GuiButton(-1, this.parentGui.getWidth() / 2 - 75, 0, 150, 20, I18nUtils.getString("minimap.waypointshare.all"));
		this.everyoneRow = new GuiButtonRowListPlayers.Row(everyoneButton);
		this.updateFilter("");
	}

	public String getPlayerName(NetworkPlayerInfo networkPlayerInfoIn) {
		return networkPlayerInfoIn.getDisplayName() != null
			? networkPlayerInfoIn.getDisplayName().getUnformattedText()
			: TextFormatting.getTextWithoutFormattingCodes(networkPlayerInfoIn.getGameProfile().getName());
	}

	private GuiButton createButtonFor(Minecraft mcIn, int x, int y, NetworkPlayerInfo networkPlayerInfo, int index) {
		if (networkPlayerInfo == null) {
			return null;
		}

		String name = this.getPlayerName(networkPlayerInfo);
		return new GuiButton(index, x, y, 150, 20, name);
	}

	@Override
	public GuiButtonRowListPlayers.Row getListEntry(int index) {
		return this.rows.get(index);
	}

	@Override
	protected int getSize() {
		return this.rows.size();
	}

	@Override
	public int getListWidth() {
		return 400;
	}

	@Override
	protected int getScrollBarX() {
		return super.getScrollBarX() + 32;
	}

	protected void sort() {
		final Collator collator = I18nUtils.getLocaleAwareCollator();
		this.players.sort((player1, player2) -> {
			String name1 = GuiButtonRowListPlayers.this.getPlayerName(player1);
			String name2 = GuiButtonRowListPlayers.this.getPlayerName(player2);
			return collator.compare(name1, name2);
		});
	}

	protected void updateFilter(String filterString) {
		this.playersFiltered = new ArrayList<>(this.players);
		Iterator<NetworkPlayerInfo> iterator = this.playersFiltered.iterator();

		while (iterator.hasNext()) {
			NetworkPlayerInfo networkPlayerInfo = iterator.next();
			String name = this.getPlayerName(networkPlayerInfo);
			if (!name.toLowerCase().contains(filterString)) {
				iterator.remove();
			}
		}

		this.rows.clear();
		this.rows.add(this.everyoneRow);

		for (int i = 0; i < this.playersFiltered.size(); i += 2) {
			NetworkPlayerInfo networkPlayerInfo1 = this.playersFiltered.get(i);
			NetworkPlayerInfo networkPlayerInfo2 = i < this.playersFiltered.size() - 1 ? this.playersFiltered.get(i + 1) : null;
			GuiButton guibutton1 = this.createButtonFor(this.mc, this.parentGui.getWidth() / 2 - 155, 0, networkPlayerInfo1, i);
			GuiButton guibutton2 = this.createButtonFor(this.mc, this.parentGui.getWidth() / 2 - 155 + 160, 0, networkPlayerInfo2, i + 1);
			this.rows.add(new GuiButtonRowListPlayers.Row(guibutton1, guibutton2));
		}
	}

	public void buttonClicked(int id) {
		if (id == -1) {
			this.parentGui.allClicked = true;
			String title = I18nUtils.getString("minimap.waypointshare.sharewitheveryone");
			String explanation = I18nUtils.getString("minimap.waypointshare.sharewitheveryone2");
			String affirm = I18nUtils.getString("gui.yes");
			String deny = I18nUtils.getString("gui.cancel");
			GuiYesNo confirmScreen = new GuiYesNo(this.parentGui, title, explanation, affirm, deny, 0);
			this.mc.displayGuiScreen(confirmScreen);
		} else {
			Mouse.next();
			NetworkPlayerInfo networkPlayerInfo = this.playersFiltered.get(id);
			String name = this.getPlayerName(networkPlayerInfo);
			this.parentGui.sendMessageToPlayer(name);
		}
	}

	public class Row implements IGuiListEntry {
		private final Minecraft client = Minecraft.getMinecraft();
		private GuiButton button = null;
		private GuiButton button1 = null;
		private GuiButton button2 = null;

		public Row(GuiButton button) {
			this.button = button;
		}

		public Row(GuiButton button1, GuiButton button2) {
			this.button1 = button1;
			this.button2 = button2;
		}

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
			this.drawButton(this.button, slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partialTicks);
			this.drawButton(this.button1, slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partialTicks);
			this.drawButton(this.button2, slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partialTicks);
		}

		private void drawButton(GuiButton button, int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
			if (button != null) {
				button.y = y;
				button.drawButton(this.client, mouseX, mouseY, partialTicks);
				if (button.id != -1) {
					this.drawIconForButton(button);
				}

				if (button.isMouseOver() && mouseY >= GuiButtonRowListPlayers.this.top && mouseY <= GuiButtonRowListPlayers.this.bottom) {
					String tooltip = I18nUtils.getString("minimap.waypointshare.sharewithname", button.displayString);
					GuiSelectPlayer.setTooltip(GuiButtonRowListPlayers.this.parentGui, tooltip);
				}
			}
		}

		private void drawIconForButton(GuiButton button) {
			NetworkPlayerInfo networkPlayerInfo = GuiButtonRowListPlayers.this.playersFiltered.get(button.id);
			GameProfile gameProfile = networkPlayerInfo.getGameProfile();
			EntityPlayer entityPlayer = GuiButtonRowListPlayers.this.mc.world.getPlayerEntityByUUID(gameProfile.getId());
			GuiButtonRowListPlayers.this.mc.getTextureManager().bindTexture(networkPlayerInfo.getLocationSkin());
			Gui.drawScaledCustomSizeModalRect(button.x + 6, button.y + 6, 8.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);
			if (entityPlayer != null && entityPlayer.isWearing(EnumPlayerModelParts.HAT)) {
				Gui.drawScaledCustomSizeModalRect(button.x + 6, button.y + 6, 40.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);
			}
		}

		@Override
		public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
			if (this.button != null && this.button.mousePressed(this.client, mouseX, mouseY)) {
				GuiButtonRowListPlayers.this.buttonClicked(this.button.id);
				return true;
			} else if (this.button1 != null && this.button1.mousePressed(this.client, mouseX, mouseY)) {
				GuiButtonRowListPlayers.this.buttonClicked(this.button1.id);
				return true;
			} else if (this.button2 != null && this.button2.mousePressed(this.client, mouseX, mouseY)) {
				GuiButtonRowListPlayers.this.buttonClicked(this.button2.id);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
			if (this.button != null) {
				this.button.mouseReleased(x, y);
			}

			if (this.button1 != null) {
				this.button1.mouseReleased(x, y);
			}

			if (this.button2 != null) {
				this.button2.mouseReleased(x, y);
			}
		}

		public void updatePosition(int slotIndex, int x, int y, float partialTicks) {
		}
	}
}
