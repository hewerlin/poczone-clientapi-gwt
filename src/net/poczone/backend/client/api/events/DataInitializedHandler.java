package net.poczone.backend.client.api.events;

import com.google.gwt.event.shared.EventHandler;

public interface DataInitializedHandler extends EventHandler {
	void onDataInitialized(DataInitializedEvent event);
}
