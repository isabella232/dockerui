package io.hawt.dockerui;

import io.hawt.util.Strings;

import javax.servlet.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class ProxyServlet implements Servlet {

    /**
     * Key for host header
     */
    public static final String DEFAULT_HOST_AND_PORT = "http://localhost:4243";

    public static final String DEFAULT_SOCKET_PATH = "/var/run/docker.sock";

    private String hostAndPort = DEFAULT_HOST_AND_PORT;

    private org.eclipse.jetty.servlets.ProxyServlet.Transparent delegate;

    private ServletConfig config;

    /**
     * Initialize the <code>ProxyServlet</code>
     *
     * @param servletConfig The Servlet configuration passed in by the servlet container
     */
    public void init(ServletConfig servletConfig) throws ServletException {

        this.config = servletConfig;

        boolean useSocket = false;
        String dockerHost = System.getenv("DOCKER_HOST");
        if (Strings.isBlank(dockerHost)) {
            dockerHost = DEFAULT_HOST_AND_PORT;
        }
        hostAndPort = dockerHost;
        if (hostAndPort.startsWith("tcp:")) {
            hostAndPort = "http:" + hostAndPort.substring(4);
        }
        String socketPath = DEFAULT_SOCKET_PATH;
        if (useSocket) {
/*            servletContext.log("Using docker socket : " + socketPath);

            UnixSocketFactory socketFactory = new UnixSocketFactory(socketPath);
            Protocol http = new Protocol("http", socketFactory, 80);
            Protocol.registerProtocol("http", http);
*/


        } else {
            try {
                URI proxyUri = new URI(hostAndPort);
                delegate = new org.eclipse.jetty.servlets.ProxyServlet.Transparent("/dockerapi", proxyUri.getHost(), proxyUri.getPort());
                delegate.init(servletConfig);
            } catch (URISyntaxException e) {
                throw new ServletException(e);
            }
        }
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        delegate.service(req, res);
    }

    @Override
    public String getServletInfo() {
        return "Hawt Docker Proxy Servlet";
    }

    @Override
    public void destroy() {
        delegate.destroy();
    }
}