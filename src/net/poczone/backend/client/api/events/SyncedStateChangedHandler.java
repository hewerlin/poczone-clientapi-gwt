package net.poczone.backend.client.api.events;

import com.google.gwt.event.shared.EventHandler;

public interface SyncedStateChangedHandler extends EventHandler {
	void onSyncedStateChanged(SyncedStateChangedEvent event);
}
