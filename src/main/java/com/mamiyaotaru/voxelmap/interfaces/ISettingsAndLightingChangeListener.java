package com.mamiyaotaru.voxelmap.interfaces;

/**
 * Listens for actionable changes announced by an
 * {@link ISettingsAndLightingChangeNotifier}, such as a settings change or a
 * lighting change that requires the map to be re-rendered.
 */
public interface ISettingsAndLightingChangeListener {
	/**
	 * Called when a settings or lighting change has occurred that requires the
	 * listener to take action (for example, re-render the map).
	 *
	 * @param notifier the notifier that announced the change
	 */
	void notifyOfActionableChange(ISettingsAndLightingChangeNotifier notifier);
}
