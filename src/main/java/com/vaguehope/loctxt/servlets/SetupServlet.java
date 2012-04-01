package com.vaguehope.loctxt.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.util.log.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.common.io.CharStreams;

public class SetupServlet extends HttpServlet {

	public static final String CONTEXT = "/setup";

	private static final String USERGRID_PARAM = "?client_id=b3U626j1PXrDEeGDNxIxOA3qXw&client_secret=b3U6Le_sU3TXJdKKWJ-C2KmGQCH3w80";
	private static final String USERGRID_URL = "http://api.usergrid.com/loctext/as" + USERGRID_PARAM;

	@Override
	protected void doPost (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String userId = req.getParameter("user_id");
		String homeLoc = req.getParameter("home_location");
		String recipTel = req.getParameter("recipient_tel");

		if (userId == null) throw new IllegalArgumentException("user_id not set.");
		if (homeLoc == null) throw new IllegalArgumentException("home_location not set.");
		if (recipTel == null) throw new IllegalArgumentException("recipient_tel not set.");

		String json = "{\"user_id\": \"" + userId + "\"," +
				"\"home_location\": \"" + homeLoc + "\"," +
				"\"recipient_tel\": \"" + recipTel + "\"" +
				"}";

		HttpPost post = new HttpPost(USERGRID_URL);
		StringEntity entity = new StringEntity(json);
		entity.setContentType("application/json");
		post.setEntity(entity);
		Log.info("Adding user: " + json);
		HttpResponse r = new DefaultHttpClient().execute(post);
		if (r.getStatusLine().getStatusCode() != 200) throw new RuntimeException(r.getStatusLine().getStatusCode() + " " + r.getStatusLine().getReasonPhrase());
		Log.info("Added user: " + userId);
	}
	
	@Override
	protected void doGet (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String users = getUsers();
		resp.getWriter().println(users);
	}
	
	public static String getUsers () throws IOException {
		Log.info("Getting users from: " + USERGRID_URL);
		HttpGet get = new HttpGet(USERGRID_URL);
		HttpResponse r = new DefaultHttpClient().execute(get);
		InputStream s = r.getEntity().getContent();
		try {
			String body = CharStreams.toString(new InputStreamReader(s));
			return body;
		}
		finally {
			s.close();
		}
	}

}
