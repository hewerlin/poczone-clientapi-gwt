package net.poczone.backend.client.api;

import java.util.List;

import com.google.gwt.event.shared.HandlerRegistration;

import net.poczone.backend.client.api.events.DataInitializedHandler;
import net.poczone.backend.client.api.events.DataUpdatedHandler;
import net.poczone.backend.client.api.events.SyncedStateChangedHandler;

public interface ItemStore extends Iterable<Item> {
	void startReceiver();

	Item create();

	Item get(String id);

	Item getOrCreate(String id);

	List<Item> getAll();

	boolean has(String id);

	boolean isSynced();

	boolean isInitialized();

	boolean canRead();

	boolean canWrite();

	HandlerRegistration addDataInitializedHandler(DataInitializedHandler handler);

	HandlerRegistration addDataUpdatedHandler(DataUpdatedHandler handler);

	HandlerRegistration addSyncedStateChangedHandler(SyncedStateChangedHandler handler);
}
