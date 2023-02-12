package org.rockhopper.smarthome.wes.jwes.communicator;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WesHttpClient {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
	
    private HttpClientContext context;
    private CloseableHttpClient httpClient;
    private HttpHost targetHost;

    public WesHttpClient(String host, int port) {
        context = HttpClientContext.create();
        httpClient = HttpClients.createDefault();
        targetHost = new HttpHost(host, port, "http"); //$NON-NLS-1$
    }

    public void login(String username, String password) {
        if ((username != null) && (password != null)) {
            AuthCache authCache = new BasicAuthCache();
            // Generate BASIC scheme object and add it to the local auth cache
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(targetHost, basicAuth);

            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);

            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()), credentials);

            context.setCredentialsProvider(credsProvider);
            context.setAuthCache(authCache);
        }
    }

    public synchronized void close() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                // Nothing more we can do!
            }
        }
    }

    public String httpGet(String path) {
        if (httpClient == null) {
            return null;
        }
        String content = null;
        HttpGet httpGet = new HttpGet(path);
        try (CloseableHttpResponse response = httpClient.execute(targetHost, httpGet, context)) {
        	logger.debug("StatusLine: {}", response.getStatusLine());
            HttpEntity entity = response.getEntity();

            content = EntityUtils.toString(entity, Charset.forName("UTF-8")); //$NON-NLS-1$
            logger.trace(">----");
            logger.trace(content);
            logger.trace("<----");

            EntityUtils.consume(entity);
        } catch (ClientProtocolException e) {
        	// TODO: Improve this!
        	throw new RuntimeException(e);
        } catch (IOException e) {
        	// TODO: Improve this!
        	throw new RuntimeException(e);
        }
        return content;
    }
}
