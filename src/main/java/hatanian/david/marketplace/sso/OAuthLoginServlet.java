package hatanian.david.marketplace.sso;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OAuthLoginServlet extends HttpServlet {
    private MarketplaceUserService userService = new MarketplaceUserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        userService.redirectToGoogleAuthenticationPage(req, resp);
    }
}
