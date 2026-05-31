package com.mamiyaotaru.voxelmap.interfaces;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@SuppressWarnings("unused")
public interface ISettingsAndLightingChangeNotifier {
	Set<ISettingsAndLightingChangeListener> listeners = new CopyOnWriteArraySet<>();

	void addObserver(ISettingsAndLightingChangeListener iSettingsAndLightingChangeListener);

	void removeObserver(ISettingsAndLightingChangeListener iSettingsAndLightingChangeListener);

	void notifyOfChanges();
}
