package com.vaguehope.loctxt;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.eclipse.jetty.util.UrlEncoded;
import org.eclipse.jetty.util.log.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;

import com.google.common.io.CharStreams;
import com.vaguehope.loctxt.servlets.SetupServlet;
import com.vaguehope.loctxt.vodafone.VodafoneAPI;

public class Cron extends TimerTask {

	private static final long DELAY = 5L * 1000L; // 10 seconds.
	private static final long INTERVAL = 5L * 60L * 1000L; // 5 minutes.

	protected static final Logger LOG = Logger.getLogger(Cron.class.getName());

	private final Timer timer = new Timer();

	public void start () {
		this.timer.scheduleAtFixedRate(this, DELAY, INTERVAL);
	}

	public void dispose () {
		this.timer.cancel();
	}

	private static final String PROTECTED_RESOURCE_URL = "http://api.developer.vodafone.com/v2/location/queries/location?address=tel%3A{n}&requestedAccuracy=1500";
	private static final String VTOKEN = "42bd391d98f84385cc0fb562b231cae2"; // BAD!

	private static final String GLOCNAMEURL = "http://maps.googleapis.com/maps/api/geocode/json?latlng={ll}&sensor=false";

	@Override
	public void run () {
		LOG.info("CRON run");
		try {
			String body = SetupServlet.getUsers();
			JSONObject data = (JSONObject) new JSONTokener(body).nextValue();
			JSONArray as = data.getJSONArray("entities");
			for (int i = 0; i < as.length(); i++) {
				try {
					JSONObject a = (JSONObject) as.get(i);
					String user_id = a.getString("user_id");
					String home_location = a.getString("home_location");
					String recipient_tel = a.getString("recipient_tel");
					String my_tel = a.has("my_tel") ? a.getString("my_tel") : recipient_tel;

					String url = PROTECTED_RESOURCE_URL.replace("{n}", my_tel);
					Log.info("Get locaion for " + user_id + " url=" + url);
					OAuthRequest request = new OAuthRequest(Verb.GET, url);
					request.addHeader("Authorization", "OAuth " + VTOKEN);
					Response response = request.send();
					String respBody = response.getBody();
					LOG.info("Raw loc response: " + respBody);
					JSONObject locData = (JSONObject) new JSONTokener(respBody).nextValue();
					JSONObject loc = (JSONObject) ((JSONObject) ((JSONObject) locData.get("terminalLocationList")).get("terminalLocation")).get("currentLocation");
					String currentLat = loc.getString("latitude");
					String currentLong = loc.getString("longitude");

					final String locName = getLocName(currentLat, currentLong);

					VodafoneAPI.sendSms(recipient_tel, "Hello from " + locName + "!");
					
					new Thread () {
						@Override
						public void run() {
							HttpGet get = new HttpGet("http://parabis.com/loctext?location=" + UrlEncoded.encodeString(locName));
							DefaultHttpClient httpClient = new DefaultHttpClient();
							HttpParams httpParams = httpClient.getParams();
							HttpConnectionParams.setConnectionTimeout(httpParams, 15000);
							HttpConnectionParams.setSoTimeout(httpParams, 15000);
							try {
								httpClient.execute(get);
							}
							catch (Exception e) {
								LOG.log(Level.WARNING, "Email send failed.", e);
							}
						};
					}.start();
					Log.info("Completed user " + user_id + " in " + locName + ".");
				}
				catch (Exception e) {
					LOG.log(Level.WARNING, "Cron failed", e);
				}
			}
		}
		catch (Exception e) {
			LOG.log(Level.WARNING, "Cron failed", e);
		}
	}

	private static String getLocName (String lat, String lng) throws ClientProtocolException, IOException, JSONException {
		Double llat = Double.valueOf(lat);
		Double llng = Double.valueOf(lng);

		String locNameUrl = GLOCNAMEURL.replace("{ll}", llat + "," + llng);
		LOG.info("Google loc API: " + locNameUrl);
		HttpGet get = new HttpGet(locNameUrl);
		HttpResponse r = new DefaultHttpClient().execute(get);
		InputStream s = r.getEntity().getContent();
		String locNameBody = CharStreams.toString(new InputStreamReader(s));

		JSONObject locData = (JSONObject) new JSONTokener(locNameBody).nextValue();
		JSONArray results = (JSONArray) locData.get("results");
		JSONObject result = (JSONObject) results.get(0);
		JSONArray address_components = result.getJSONArray("address_components");
		for (int i = 0; i < address_components.length(); i++) {
			JSONObject address = (JSONObject) address_components.get(i);
			JSONArray types = address.getJSONArray("types");
			for (int j = 0; j < types.length(); j++) {
				String type = (String) types.get(j);
				if ("locality".equals(type)) { return address.getString("long_name"); }
			}
		}
		return "{" + llat + "," + lng + "}";
	}

	public static void main (String[] args) throws ClientProtocolException, IOException, JSONException {
		System.out.println(getLocName("40.714224", "-73.961452"));
	}

}
