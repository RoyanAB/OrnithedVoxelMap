package com.mamiyaotaru.voxelmap.gui.overridden;

import java.util.ArrayList;

public abstract class PopupGuiScreen extends GuiScreenMinimap implements IPopupGuiScreen {
	private final ArrayList<Popup> popups = new ArrayList<>();

	@Override
	public void drawMap() {
	}

	@Override
	public void onGuiClosed() {
	}

	public void createPopup(int mouseX, int mouseY, int directX, int directY, ArrayList<Popup.PopupEntry> entries) {
		this.popups.add(new Popup(mouseX, mouseY, directX, directY, entries, this));
	}

	public void clearPopups() {
		this.popups.clear();
	}

	public boolean clickedPopup(int mouseX, int mouseY) {
		boolean clicked = false;
		ArrayList<Popup> deadPopups = new ArrayList<>();

		for (Popup popup : this.popups) {
			boolean clickedPopup = popup.clickedMe(mouseX, mouseY);
			if (!clickedPopup) {
				deadPopups.add(popup);
			} else if (popup.shouldClose()) {
				deadPopups.add(popup);
			}

			clicked = clicked || clickedPopup;
		}

		this.popups.removeAll(deadPopups);
		return clicked;
	}

	@Override
	public boolean overPopup(int mouseX, int mouseY) {
		boolean over = false;

		for (Popup popup : this.popups) {
			boolean overPopup = popup.overMe(mouseX, mouseY);
			over = over || overPopup;
		}

		return over;
	}

	@Override
	public boolean popupOpen() {
		return !this.popups.isEmpty();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);

		for (Popup popup : this.popups) {
			popup.drawPopup(mouseX, mouseY);
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (!this.clickedPopup(mouseX, mouseY)) {
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}
}
