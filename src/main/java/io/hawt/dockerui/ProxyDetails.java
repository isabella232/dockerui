package io.hawt.dockerui;

/*
import io.hawt.system.AuthInfo;
import io.hawt.system.Authenticator;
import io.hawt.system.ExtractAuthInfoCallback;
*/

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * A helper object to store the proxy location details
 */
public class ProxyDetails {
    private final ServletContext servletContext;
    private boolean debug = true;
    private String stringProxyURL;
    private String hostAndPort;
    private String path = "";
    private String userName;
    private String password;
    private String host;
    private int port = 80;

    public static final String USER_PARAM = "_user";
    public static final String PWD_PARAM = "_pwd";

    private static Set<String> ignoreHeaderNames = new HashSet<String>(Arrays.asList(USER_PARAM, PWD_PARAM, "_url", "url"));


    public ProxyDetails(String hostAndPort, HttpServletRequest httpServletRequest, ServletContext servletContext) {
        this.hostAndPort = hostAndPort;
        this.servletContext = servletContext;
        String path = httpServletRequest.getPathInfo();
        if (path == null) {
            path = "";
        }
        stringProxyURL = hostAndPort + path;


        try {
            // Handle the query string
/*
            if (httpServletRequest.getQueryString() != null) {
                stringProxyURL += "?" + URIUtil.encodeQuery(httpServletRequest.getQueryString());
            }
*/

            // lets add the query parameters
            Enumeration<?> iter = httpServletRequest.getParameterNames();
            while (iter.hasMoreElements()) {
                Object next = iter.nextElement();
                if (next instanceof String) {
                    String name = next.toString();
                    if (!ignoreHeaderNames.contains(name)) {
                        String[] values = httpServletRequest.getParameterValues(name);
                        for (String value : values) {
                            String prefix = "?";
                            if (stringProxyURL.contains("?")) {
                                prefix = "&";
                            }
                            stringProxyURL += prefix + name + "=" + value;
                        }
                    }
                }
            }

            if (debug) {
                try {
                    servletContext.log("Proxying to " + stringProxyURL + " as user: " + userName);
                } catch (Exception e) {
                    // ignore
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Returns the lowest index of the given list of values
     */
    protected int indexOf(String text, String... values) {
        int answer = -1;
        for (String value : values) {
            int idx = text.indexOf(value);
            if (idx >= 0) {
                if (answer < 0 || idx < answer) {
                    answer = idx;
                }
            }
        }
        return answer;
    }

    public HttpClient createHttpClient(HttpMethod httpMethodProxyRequest) {
        HttpClient client = new HttpClient();

        if (userName != null) {
            //client.getParams().setAuthenticationPreemptive(true);
            httpMethodProxyRequest.setDoAuthentication(true);

            Credentials defaultcreds = new UsernamePasswordCredentials(userName, password);
            client.getState().setCredentials(new AuthScope(host, port, AuthScope.ANY_REALM), defaultcreds);
        }
        return client;
    }

    public String getStringProxyURL() {
        return stringProxyURL;
    }

    public String getProxyHostAndPort() {
        return hostAndPort;
    }

    public String getProxyPath() {
        return path;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getHostAndPort() {
        return hostAndPort;
    }

    public String getPath() {
        return path;
    }

    public boolean isValid() {
        return hostAndPort != null;
    }
}
