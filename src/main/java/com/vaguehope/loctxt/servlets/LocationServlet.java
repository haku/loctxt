package com.vaguehope.loctxt.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.log.Log;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;

public class LocationServlet extends HttpServlet {

	public static final String CONTEXT = "/loc";

	private static final String PROTECTED_RESOURCE_URL = "http://api.developer.vodafone.com/v2/location/queries/location?address=tel%3A{n}&requestedAccuracy=1500";

	private static final long serialVersionUID = -9142463359734093064L;

	@Override
	protected void doGet (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String accessToken = VodafoneOauthServlet.getSessionAccessToken(req);
		if (accessToken == null) throw new IllegalStateException("No access token saved.");

		String n = req.getParameter("n");
		if (n == null) throw new IllegalArgumentException("attr 'n' not set.");

		String url = PROTECTED_RESOURCE_URL.replace("{n}", n);
		Log.info("url=" + url);
		OAuthRequest request = new OAuthRequest(Verb.GET, url);
		request.addHeader("Authorization", "OAuth " + accessToken);
		Response response = request.send();

		PrintWriter w = resp.getWriter();
		w.println(response.getCode());
		w.println(response.getBody());
	}

}
