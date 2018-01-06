package net.poczone.backend.client.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

import net.poczone.backend.client.api.Item;

public class ItemImpl implements Item {
	private String id;

	public ItemImpl(String id) {
		this.id = id;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public String get(String key) {
		JSONValue json = getStore().getForRead(id).get(key);
		return json != null && json.isString() != null ? json.isString().stringValue() : "";
	}

	@Override
	public Item put(String key, String value) {
		if (value != null) {
			getStore().getForWrite(id).put(key, new JSONString(value));
		} else {
			getStore().getForWrite(id).put(key, null);
		}
		return this;
	}

	@Override
	public List<String> getList(String key) {
		JSONValue jsonValue = getStore().getForRead(id).get(key);
		JSONArray array = jsonValue != null ? jsonValue.isArray() : null;

		List<String> strings = new ArrayList<String>();
		if (array == null) {
			return strings;
		}

		for (int i = 0; i < array.size(); i++) {
			JSONString string = array.get(i).isString();
			strings.add(string != null ? string.stringValue() : null);
		}

		return strings;
	}

	@Override
	public Item put(String key, Collection<String> values) {
		if (values == null) {
			getStore().getForWrite(id).put(key, null);
			return this;
		}

		JSONArray array = new JSONArray();
		for (String string : values) {
			array.set(array.size(), string != null ? new JSONString(string) : JSONNull.getInstance());
		}

		getStore().getForWrite(id).put(key, array);
		return this;
	}

	@Override
	public Long getLong(String key) {
		JSONValue json = getStore().getForRead(id).get(key);
		return json != null && json.isNumber() != null ? (long) json.isNumber().doubleValue() : null;
	}

	@Override
	public Item put(String key, Long value) {
		getStore().getForWrite(id).put(key, value != null ? new JSONNumber(value) : null);
		return this;
	}

	@Override
	public Boolean getBoolean(String key) {
		JSONValue json = getStore().getForRead(id).get(key);
		return json != null && json.isBoolean() != null ? json.isBoolean().booleanValue() : null;
	}

	@Override
	public Item put(String key, Boolean value) {
		getStore().getForWrite(id).put(key, value != null ? JSONBoolean.getInstance(value) : null);
		return this;
	}

	@Override
	public boolean isSynced() {
		return getStore().isSynced(id);
	}

	@Override
	public void delete() {
		getStore().delete(id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Item) && ((Item) obj).getID().equals(getID());
	}

	private static ItemStoreImpl getStore() {
		return ItemStoreImpl.instance;
	}
}
