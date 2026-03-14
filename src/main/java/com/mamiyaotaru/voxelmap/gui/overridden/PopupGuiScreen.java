package com.mamiyaotaru.voxelmap.gui.overridden;

import java.io.IOException;
import java.util.ArrayList;

public abstract class PopupGuiScreen extends GuiScreenMinimap implements IPopupGuiScreen {
	private final ArrayList<Popup> popups = new ArrayList<>();

	@Override
	public void drawMap() {
	}

	@Override
	public void onGuiClosed() {
	}

	public void createPopup(int x, int y, int directX, int directY, ArrayList<Popup.PopupEntry> entries) {
		this.popups.add(new Popup(x, y, directX, directY, entries, this));
	}

	public void clearPopups() {
		this.popups.clear();
	}

	public boolean clickedPopup(int x, int y) {
		boolean clicked = false;
		ArrayList<Popup> deadPopups = new ArrayList<>();

		for (Popup popup : this.popups) {
			boolean clickedPopup = popup.clickedMe(x, y);
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
	public boolean overPopup(int x, int y) {
		boolean over = false;

		for (Popup popup : this.popups) {
			boolean overPopup = popup.overMe(x, y);
			over = over || overPopup;
		}

		return over;
	}

	@Override
	public boolean popupOpen() {
		return this.popups.size() > 0;
	}

	public void drawScreen(int x, int y, float dunno) {
		super.drawScreen(x, y, dunno);

		for (Popup popup : this.popups) {
			popup.drawPopup(x, y);
		}
	}

	protected void mouseClicked(int par1, int par2, int par3) {
		if (!this.clickedPopup(par1, par2)) {
			super.mouseClicked(par1, par2, par3);
		}
	}
}
