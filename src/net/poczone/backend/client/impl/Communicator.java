package net.poczone.backend.client.impl;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class Communicator {
	private static long since;

	private static String getDataToken() {
		return Location.getParameter("dataToken");
	}

	private static String getBase() {
		return Location.getParameter("base");
	}

	public static void post(JSONObject commit, final AsyncCallback<Void> callback) {
		send("data/json/post", new Args().add("dataToken", getDataToken()).add("commit", commit.toString()),
				new AsyncCallback<JSONObject>() {
					@Override
					public void onSuccess(JSONObject result) {
						callback.onSuccess(null);
					}

					@Override
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}
				});
	}

	public static void getDiff(boolean wait, final AsyncCallback<JSONObject> callback) {
		send("data/json/getDiff",
				new Args().add("dataToken", getDataToken()).add("since", since + "").add("wait", wait + ""),
				new AsyncCallback<JSONObject>() {
					@Override
					public void onSuccess(JSONObject result) {
						since = (long) result.get("nextSince").isNumber().doubleValue();
						callback.onSuccess(result.get("diff").isObject());
					}

					@Override
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}
				});
	}

	private static void send(String endpoint, Args args, final AsyncCallback<JSONObject> callback) {
		RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, getBase() + endpoint);
		rb.setCallback(new RequestCallback() {
			@Override
			public void onResponseReceived(Request request, Response response) {
				JSONObject json = null;
				try {
					json = JSONParser.parseStrict(response.getText()).isObject();
				} catch (Exception e) {
				}

				if (json != null) {
					if (json.get("success").isBoolean().booleanValue()) {
						callback.onSuccess(json);
					} else {
						callback.onFailure(new RequestException("No success"));
					}
				} else {
					callback.onFailure(new RequestException("Response is not a JSON Object"));
				}
			}

			@Override
			public void onError(Request request, Throwable exception) {
				callback.onFailure(exception);
			}
		});

		rb.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		rb.setRequestData(args.toString());
		try {
			rb.send();
		} catch (RequestException e) {
			callback.onFailure(e);
		}
	}

	private static class Args {
		private StringBuilder sb = new StringBuilder();

		public Args add(String key, String value) {
			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append(key);
			sb.append("=");
			if (value != null) {
				sb.append(URL.encodePathSegment(value));
			}

			return this;
		}

		@Override
		public String toString() {
			return sb.toString();
		}
	}

	public static boolean can(String flag) {
		if (getBase() == null || getDataToken() == null) {
			return false;
		}

		String[] parts = getDataToken().split("/");
		return parts.length >= 3 && parts[1].indexOf(flag) >= 0;
	}
}
