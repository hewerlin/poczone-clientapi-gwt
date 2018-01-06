package net.poczone.backend.client.api.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.event.shared.GwtEvent;

public class DataUpdatedEvent extends GwtEvent<DataUpdatedHandler> {
	public static final Type<DataUpdatedHandler> TYPE = new Type<DataUpdatedHandler>();

	private List<String> updatedIDs;

	public DataUpdatedEvent(Collection<String> updatedIDs) {
		this.updatedIDs = new ArrayList<String>(updatedIDs);
	}

	public List<String> getUpdatedIDs() {
		return updatedIDs;
	}
	
	@Override
	public Type<DataUpdatedHandler> getAssociatedType() {
		return TYPE;
	}
	
	@Override
	protected void dispatch(DataUpdatedHandler handler) {
		handler.onDataUpdated(this);
	}
}
