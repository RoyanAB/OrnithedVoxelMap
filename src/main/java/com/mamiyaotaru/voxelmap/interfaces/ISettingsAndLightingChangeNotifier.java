package com.mamiyaotaru.voxelmap.interfaces;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Notifies registered {@link ISettingsAndLightingChangeListener listeners} when
 * settings or lighting conditions change and the map needs to be re-rendered.
 */
public interface ISettingsAndLightingChangeNotifier {
	/** The set of listeners that will be notified of actionable changes. */
	Set<ISettingsAndLightingChangeListener> listeners = new CopyOnWriteArraySet<>();

	/**
	 * Registers a listener to be notified of changes.
	 *
	 * @param listener the listener to register
	 */
	void addObserver(ISettingsAndLightingChangeListener listener);

	/**
	 * Removes a previously registered listener.
	 *
	 * @param listener the listener to remove
	 */
	void removeObserver(ISettingsAndLightingChangeListener listener);

	/**
	 * Notifies all registered listeners that a change has occurred.
	 */
	void notifyOfChanges();
}
