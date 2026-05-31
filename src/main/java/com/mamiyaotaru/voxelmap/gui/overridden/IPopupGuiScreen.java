package com.mamiyaotaru.voxelmap.gui.overridden;

@SuppressWarnings("unused")
public interface IPopupGuiScreen {
	boolean overPopup(int mouseX, int mouseY);

	boolean popupOpen();

	void popupAction(Popup popup, int action);
}
