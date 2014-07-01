package hatanian.david.marketplace.sso;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

public class MarketplaceUserService {
    private static final String EMAIL_SESSION_PROPERTY = "email";
    private static final String STATE_SESSION_PROPERTY = "state";
    private static final String CLIENT_ID = "368362145081-3461m4a8ut52gdf2q6ujrhjv0iu4kro9.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "WPBSIRCG3hDLejxdHH8TDwrY";

    //This is the only scope that triggers the seamless SSO behavior
    private static final String AUTH_SCOPE = "email profile";
    private static final String REDIRECT_SESSION_PROPERTY = "redirect";
    private static final String OAUTH_CALLBACK_URL = "oauth2callback";
    private static final String OAUTH_LOGIN_URL = "oauth2login";

    public String getCurrentUserEmail(HttpServletRequest req) {
        return (String) req.getSession(true).getAttribute(EMAIL_SESSION_PROPERTY);
    }

    private void setOAuthStateInSession(HttpServletRequest request, String state) {
        request.getSession(true).setAttribute(STATE_SESSION_PROPERTY, state);
    }

    public void redirectToInternalAuthenticationPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        storeRedirectInfoInSession(request, response);
        response.sendRedirect("/" + OAUTH_LOGIN_URL);
    }

    public void redirectToGoogleAuthenticationPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString();
        setOAuthStateInSession(request, state);
        String url = buildOAuthUrl(request, state);
        response.sendRedirect(url);
    }

    private void storeRedirectInfoInSession(HttpServletRequest request, HttpServletResponse response) {
        request.getSession(true).setAttribute(REDIRECT_SESSION_PROPERTY, request.getRequestURL() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
    }

    private String buildOAuthUrl(HttpServletRequest request, String state) {
        GoogleAuthorizationCodeFlow flow = getGoogleAuthorizationCodeFlow();
        return flow.newAuthorizationUrl().setState(state).setRedirectUri(buildRedirectUri(request)).build();
    }

    private GoogleAuthorizationCodeFlow getGoogleAuthorizationCodeFlow() {
        return new GoogleAuthorizationCodeFlow.Builder(new NetHttpTransport(), new GsonFactory(), CLIENT_ID, CLIENT_SECRET, Collections.singletonList(AUTH_SCOPE)).build();
    }

    private String buildRedirectUri(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/" + OAUTH_CALLBACK_URL;
    }

    public void validateOAuthResponseAndRedirect(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String state = getMandatoryParameter("state", req);
        String code = getMandatoryParameter("code", req);

        validateState(state, req);
        String userEmail = getUserEmailFromOAuthCode(code, req);
        storeEmailInSession(userEmail, req);
        resp.sendRedirect(getRedirectUrlFromSession(req));
    }

    private String getUserEmailFromOAuthCode(String code, HttpServletRequest request) throws IOException {
        GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow = getGoogleAuthorizationCodeFlow();
        GoogleTokenResponse tokenResponse = googleAuthorizationCodeFlow.newTokenRequest(code).setRedirectUri(buildRedirectUri(request)).execute();
        return tokenResponse.parseIdToken().getPayload().getEmail();
    }

    private void validateState(String state, HttpServletRequest req) {
        if (!state.equals(req.getSession().getAttribute(STATE_SESSION_PROPERTY))) {
            throw new IllegalArgumentException("The provided state does not match the one in session");
        }
    }

    private void storeEmailInSession(String userEmail, HttpServletRequest req) {
        req.getSession(true).setAttribute(EMAIL_SESSION_PROPERTY, userEmail);
    }

    private String getRedirectUrlFromSession(HttpServletRequest req) {
        return (String) req.getSession(true).getAttribute(REDIRECT_SESSION_PROPERTY);
    }

    private String getMandatoryParameter(String paramName, HttpServletRequest req) throws ServletException {
        String param = req.getParameter(paramName);
        if (param == null) {
            throw new ServletException("Unable to get the parameter " + paramName);
        }
        return param;
    }
}
