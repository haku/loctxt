package com.vaguehope.loctxt.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserServlet extends HttpServlet {

	public static final String CONTEXT = "/setup";

	private static final String USERGRID_PARAM = "?client_id=b3U626j1PXrDEeGDNxIxOA3qXw&client_secret=b3U6Le_sU3TXJdKKWJ-C2KmGQCH3w80";
	private static final String USERGRID_URL = "http://api.usergrid.com/loctext/a" + USERGRID_PARAM;

	@Override
	protected void doPost (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String userId = req.getParameter("user_id");
		String homeLoc = req.getParameter("home_location");
		String recipTel = req.getParameter("recipient_tel");
		
		if (userId == null) throw new IllegalArgumentException("user_id not set.");
		if (homeLoc == null) throw new IllegalArgumentException("home_location not set.");
		if (recipTel == null) throw new IllegalArgumentException("recipient_tel not set.");
		
		
	}

}
