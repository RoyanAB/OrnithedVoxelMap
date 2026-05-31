package com.mamiyaotaru.voxelmap.persistent;

@SuppressWarnings("unused")
public interface IThreadCompleteListener {
	void notifyOfThreadComplete(AbstractNotifyingRunnable notifyingRunnable);
}
