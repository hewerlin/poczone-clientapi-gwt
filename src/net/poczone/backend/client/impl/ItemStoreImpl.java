package net.poczone.backend.client.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import net.poczone.backend.client.api.Item;
import net.poczone.backend.client.api.ItemStore;
import net.poczone.backend.client.api.events.DataInitializedEvent;
import net.poczone.backend.client.api.events.DataInitializedHandler;
import net.poczone.backend.client.api.events.DataUpdatedEvent;
import net.poczone.backend.client.api.events.DataUpdatedHandler;
import net.poczone.backend.client.api.events.SyncedStateChangedEvent;
import net.poczone.backend.client.api.events.SyncedStateChangedHandler;

public class ItemStoreImpl implements ItemStore {
	private static final int FAILURE_RETRY_MILLIS = 5000;

	private EventBus eventBus = GWT.create(SimpleEventBus.class);

	private boolean started;
	private boolean initialized;
	private boolean publishedSyncedState;

	private Set<String> localDataUpdateIDs = new TreeSet<String>();

	private JSONObject toBeSaved = new JSONObject();
	private JSONObject saving = new JSONObject();
	private JSONObject saved = new JSONObject();

	private Timer saveTimer = new Timer() {
		@Override
		public void run() {
			save();
		}
	};

	protected static ItemStoreImpl instance = new ItemStoreImpl();

	public static ItemStore get() {
		return instance;
	}

	private ItemStoreImpl() {
	}

	@Override
	public boolean canRead() {
		return Communicator.can("R");
	}

	@Override
	public boolean canWrite() {
		return Communicator.can("W");
	}

	@Override
	public void startReceiver() {
		if (started) {
			return;
		}
		started = true;

		if (canRead()) {
			getDiff(1);
		}
	}

	private void getDiff(int millis) {
		new Timer() {
			@Override
			public void run() {
				Communicator.getDiff(initialized, new AsyncCallback<JSONObject>() {
					@Override
					public void onSuccess(JSONObject result) {
						getDiff(1);
						handleDataReceived(result);
					}

					@Override
					public void onFailure(Throwable caught) {
						getDiff(FAILURE_RETRY_MILLIS);
					}
				});
			}
		}.schedule(millis);
	}

	private void handleDataReceived(JSONObject diff) {
		for (String id : diff.keySet()) {
			JSONObject object = diff.get(id).isObject();
			saved.put(id, object);
		}

		if (!initialized) {
			initialized = true;
			eventBus.fireEvent(new DataInitializedEvent());
		}

		if (diff.size() > 0) {
			Set<String> updatedIDs = diff.keySet();
			eventBus.fireEvent(new DataUpdatedEvent(updatedIDs));
		}
	}

	@Override
	public Iterator<Item> iterator() {
		return getAll().iterator();
	}

	@Override
	public List<Item> getAll() {
		Set<String> ids = new TreeSet<String>();
		ids.addAll(saved.keySet());
		ids.addAll(saving.keySet());
		ids.addAll(toBeSaved.keySet());

		List<Item> items = new ArrayList<Item>();
		for (String id : ids) {
			if (has(id)) {
				items.add(get(id));
			}
		}
		return items;
	}

	@Override
	public Item create() {
		String id;
		do {
			id = IDGenerator.generateID();
		} while (has(id));

		return getOrCreate(id);
	}

	@Override
	public Item get(String id) {
		return has(id) ? new ItemImpl(id) : null;
	}

	@Override
	public Item getOrCreate(String id) {
		if (!has(id)) {
			getForWrite(id);
		}
		return get(id);
	}

	@Override
	public boolean has(String id) {
		for (JSONObject data : new JSONObject[] { toBeSaved, saving, saved }) {
			if (data.get(id) != null && data.get(id).isObject() != null) {
				return true;
			}
			if (data.get(id) != null && data.get(id).isNull() != null) {
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean isSynced() {
		return saving.size() == 0 && toBeSaved.size() == 0;
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public HandlerRegistration addDataInitializedHandler(final DataInitializedHandler handler) {
		if (isInitialized()) {
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					handler.onDataInitialized(new DataInitializedEvent());
				}
			});
		}

		return eventBus.addHandler(DataInitializedEvent.TYPE, handler);
	}

	@Override
	public HandlerRegistration addDataUpdatedHandler(DataUpdatedHandler handler) {
		return eventBus.addHandler(DataUpdatedEvent.TYPE, handler);
	}

	@Override
	public HandlerRegistration addSyncedStateChangedHandler(SyncedStateChangedHandler handler) {
		return eventBus.addHandler(SyncedStateChangedEvent.TYPE, handler);
	}

	protected JSONObject getForRead(String id) {
		for (JSONObject data : new JSONObject[] { toBeSaved, saving, saved }) {
			JSONValue value = data.get(id);
			if (value != null) {
				return value.isObject();
			}
		}
		return new JSONObject();
	}

	protected JSONObject getForWrite(String id) {
		if (localDataUpdateIDs.add(id) && localDataUpdateIDs.size() == 1) {
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					eventBus.fireEvent(new DataUpdatedEvent(localDataUpdateIDs));
					localDataUpdateIDs.clear();
				}
			});
		}

		if (toBeSaved.get(id) == null) {
			JSONObject clone = clone(getForRead(id));
			toBeSaved.put(id, clone);
			scheduleSave(1);
		}

		return toBeSaved.get(id).isObject();
	}

	private static JSONObject clone(JSONObject original) {
		return JSONParser.parseStrict(original.toString()).isObject();
	}

	protected boolean isSynced(String id) {
		return toBeSaved.get(id) == null && saving.get(id) == null;
	}

	protected void delete(String id) {
		toBeSaved.put(id, JSONNull.getInstance());
		scheduleSave(1);
	}

	private void scheduleSave(int millis) {
		saveTimer.schedule(millis);
	}

	private void save() {
		boolean synced = isSynced();
		if (publishedSyncedState != synced) {
			publishedSyncedState = synced;
			eventBus.fireEvent(new SyncedStateChangedEvent(synced));
		}

		if (saving.size() > 0 || toBeSaved.size() == 0) {
			return;
		}

		saving = toBeSaved;
		toBeSaved = new JSONObject();

		Communicator.post(saving, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				handleSaveSuccess();
			}

			@Override
			public void onFailure(Throwable caught) {
				handleSaveError();
			}
		});
	}

	private void handleSaveSuccess() {
		for (String id : saving.keySet()) {
			saved.put(id, saving.get(id).isObject());
		}
		Set<String> updatedIDs = saving.keySet();
		saving = new JSONObject();

		scheduleSave(1);
		eventBus.fireEvent(new DataUpdatedEvent(updatedIDs));
	}

	private void handleSaveError() {
		for (String id : saving.keySet()) {
			if (toBeSaved.get(id) == null) {
				toBeSaved.put(id, saving.get(id));
			}
		}
		saving = new JSONObject();
		scheduleSave(FAILURE_RETRY_MILLIS);
	}
}
