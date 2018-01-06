package net.poczone.backend.client.api.events;

import com.google.gwt.event.shared.GwtEvent;

public class DataInitializedEvent extends GwtEvent<DataInitializedHandler> {
	public static final Type<DataInitializedHandler> TYPE = new Type<DataInitializedHandler>();

	@Override
	public Type<DataInitializedHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(DataInitializedHandler handler) {
		handler.onDataInitialized(this);
	}
}
