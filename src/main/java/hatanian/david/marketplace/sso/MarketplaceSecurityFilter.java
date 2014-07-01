package hatanian.david.marketplace.sso;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MarketplaceSecurityFilter implements Filter {
    private MarketplaceUserService userService = new MarketplaceUserService();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //Nothing to do
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        if (userService.getCurrentUserEmail(httpServletRequest) == null) {
            userService.redirectToInternalAuthenticationPage(httpServletRequest, (HttpServletResponse) response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        //Nothing to do
    }
}
