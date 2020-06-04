package qz.utils;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qz.ws.PrintSocketClient;

import java.net.URI;
import java.util.concurrent.*;

@WebSocket
public class DmsWebSocketClient {
    private static final Logger log = LoggerFactory.getLogger(DmsWebSocketClient.class);
    private final CountDownLatch closeLatch;
    private Session session;
    //private ApolloClient apolloClient = new ApolloClient.builder();

    public DmsWebSocketClient () {
        this.closeLatch = new CountDownLatch(1);
    }

    public boolean awaitClose (int duration, TimeUnit unit) throws InterruptedException {
        return this.closeLatch.await(duration, unit);
    }
    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        log.info(String.format("Connection closed: %d - %s%n", statusCode, reason));
        this.session = null;
        this.closeLatch.countDown();
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        log.info(String.format("Got connect: %s%n",session));
        this.session = session;
        try {
            Future<Void> fut;
            String subscription = "subscription {\n" +
                    "  hasNewPrint(\n" +
                    "    printersName: [\"Printer HP 2000\", \"Printer DELL\"]\n" +
                    "    lotId: 3\n" +
                    "    clientId: \"b/B/VbxE2og2ZTg1YzZhZS1mOTUwLTRhMzMtYTFhOC02NTY1ZjRkZjVmOWUzOTU4Nw==\"\n" +
                    "  )";
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("query", subscription);
            session.getRemote().sendString(jsonObject.toString());
            //fut = session.getRemote().sendStringByFuture(jsonObject.toString());
            //fut.get(2, TimeUnit.SECONDS);
            //fut = session.getRemote().sendStringByFuture("Ending conversation");
            //fut.get(2, TimeUnit.SECONDS);
            //session.close(StatusCode.NORMAL, "Finished");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    @OnWebSocketMessage
    public void onMessage(String message) {
        log.info(String.format("Got Message: %s%n", message));
    }

    @OnWebSocketError
    public void onError(Throwable error) {
        log.error(String.format("%s%n error", error));
    }

    public static void testWebSocket(String wssUrl, String token) {
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setTrustAll(Boolean.TRUE);
        WebSocketClient wsClient = new WebSocketClient(sslContextFactory);
        DmsWebSocketClient socket = new DmsWebSocketClient();
        try {
            wsClient.setMaxIdleTimeout(Long.MAX_VALUE);
            wsClient.start();
            URI wsUri = new URI(wssUrl);
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setHeader("Authorization", "Bearer " + token);
            Session session = wsClient.connect(socket, wsUri, request).get();
            log.info("Testing " + session.isOpen());
        }
        catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                wsClient.stop();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
