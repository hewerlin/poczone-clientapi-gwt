package net.poczone.backend.client.api.events;

import com.google.gwt.event.shared.EventHandler;

public interface DataUpdatedHandler extends EventHandler {
	void onDataUpdated(DataUpdatedEvent event);
}
