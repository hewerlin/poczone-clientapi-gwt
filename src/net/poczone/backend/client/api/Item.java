package net.poczone.backend.client.api;

import java.util.Collection;
import java.util.List;

public interface Item {
	String getID();

	String get(String key);

	Item put(String key, String value);

	List<String> getList(String key);

	Item put(String key, Collection<String> values);
	
	Long getLong(String key);
	
	Item put(String key, Long value);

	Boolean getBoolean(String key);
	
	Item put(String key, Boolean value);
	
	boolean isSynced();

	void delete();
}
