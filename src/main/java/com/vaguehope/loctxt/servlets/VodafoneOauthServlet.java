package com.vaguehope.loctxt.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Request;
import org.scribe.model.Response;
import org.scribe.model.Verb;

import com.vaguehope.loctxt.vodafone.VodafoneAPI;

public class VodafoneOauthServlet extends HttpServlet {

	public static final String CONTEXT = "/voauth";

	private static final String scope = "GET-/location/queries/location";
	private static final String responseType = "code";

	private static final String SESSION_API = "vapi";
	private static final String SESSION_ACCESSTOKEN = "vtoken";

	private static final long serialVersionUID = -7840979457642008927L;
	private static final Logger LOG = Logger.getLogger(VodafoneOauthServlet.class.getName());

	@Override
	protected void doGet (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		StringBuffer callbackUrlBuf = req.getRequestURL();
		callbackUrlBuf.replace(callbackUrlBuf.lastIndexOf("/"), callbackUrlBuf.length(), "").append(CONTEXT);
		String callbackUrl = callbackUrlBuf.toString();

		String vkey = System.getenv("vkey");
		if (vkey == null) throw new IllegalStateException("vkey not set.");
		ServiceBuilder service = new ServiceBuilder()
				.apiKey(vkey)
				.callback(callbackUrl)
				.scope(scope)
				.responseType(responseType);

		String code = req.getParameter("code");
		if (code != null && !code.isEmpty()) {
			LOG.info("callbackUrl=" + callbackUrl);
			VodafoneAPI api = getSessionAPI(req);
			LOG.info("api code=" + code);
			if (api == null) throw new IllegalStateException("Session does not exist.");

			String accessTokenURL = api.getAccessTokenUrl(service, code);
			Request areq = new Request(Verb.POST, accessTokenURL);
			areq.addBodyParameter("redirect_uri", callbackUrl);
			areq.addBodyParameter("grant_type", "authorization_code");
			areq.addBodyParameter("code", code);
			Response aresp = areq.send();
			try {
				JSONObject object = (JSONObject) new JSONTokener(aresp.getBody()).nextValue();
				String accessToken = object.getString("access_token");
				LOG.info("api code=" + code + " --> accessToken=" + accessToken);
				setSessionAccessToken(req, accessToken);
				resp.getWriter().println("authorised: accessToken=" + accessToken);
			}
			catch (JSONException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e);
			}
		}
		else {
			VodafoneAPI api = new VodafoneAPI();
			setSessionAPI(req, api);
			String authorizationUrl = api.getAuthorizationUrl(service);
			resp.sendRedirect(authorizationUrl);
		}
	}

	private static void setSessionAccessToken (HttpServletRequest req, String code) {
		req.getSession().setAttribute(SESSION_ACCESSTOKEN, code);
	}

	public static String getSessionAccessToken (HttpServletRequest req) {
		Object a = req.getSession().getAttribute(SESSION_ACCESSTOKEN);
		if (a == null) return null;
		return a.toString();
	}

	private static void setSessionAPI (HttpServletRequest req, VodafoneAPI api) {
		req.getSession().setAttribute(SESSION_API, api);
	}

	private static VodafoneAPI getSessionAPI (HttpServletRequest req) {
		Object a = req.getSession().getAttribute(SESSION_API);
		if (a == null) return null;
		return (VodafoneAPI) a;
	}

}
