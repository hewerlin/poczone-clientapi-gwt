package net.poczone.backend.client.api.events;

import com.google.gwt.event.shared.GwtEvent;

public class SyncedStateChangedEvent extends GwtEvent<SyncedStateChangedHandler> {
	public static final Type<SyncedStateChangedHandler> TYPE = new Type<SyncedStateChangedHandler>();

	private boolean synced;

	public SyncedStateChangedEvent(boolean synced) {
		this.synced = synced;
	}

	public boolean isSynced() {
		return synced;
	}

	@Override
	public Type<SyncedStateChangedHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(SyncedStateChangedHandler handler) {
		handler.onSyncedStateChanged(this);
	}
}
