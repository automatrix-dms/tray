package qz.utils;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qz.ui.DmsSettingDialog;
//import sun.net.www.http.HttpClient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static qz.common.DmsContants.DMS_GRAPHQL_URL;


public class GraphQLUtilities {
    private static final Logger log = LoggerFactory.getLogger(GraphQLUtilities.class);
    public final static String GRAPHQL_URL = MiscUtilities.getProps().getProperty(DMS_GRAPHQL_URL);

    public static HttpClient getHttpClient() throws IOException {
        SslContextFactory sslContextFactory = new SslContextFactory();
        HttpClient httpClient = new HttpClient(sslContextFactory);
        httpClient.setFollowRedirects(false);
        return httpClient;
    }

    public static JSONObject requestGraphQL(String query, String bearerToken) {
        JSONObject queryObject = new JSONObject();
        try {
            queryObject.put("query", query);
            StringContentProvider content = new StringContentProvider(queryObject.toString());
            log.info("content " + content.toString());
            HttpClient client = getHttpClient();
            client.start();
            Request request = client
                                .POST(GRAPHQL_URL);
            request.header("Authorization", "Bearer " + bearerToken);
            request.header("Accept", "application/json");
            request.header("Content-Type", "application/json");
            request.content(content);
            ContentResponse response = request.send();
            log.info("Status " + response.getStatus());
            if (response.getStatus() != 200) {
                return null;
            }

            log.info("response " + response);
            String stringResponse = new String(response.getContent());
            client.stop();
            JSONObject responseJson = new JSONObject(stringResponse);
            if (responseJson.has("errors")) {
                log.error("errors: " + responseJson.get("errors"));
                return null;
            }
            log.info("stringResponse " + stringResponse);
            return responseJson;
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject subscription () {
        return new JSONObject();
    }
}
