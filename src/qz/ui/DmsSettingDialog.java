package qz.ui;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.internal.RealApolloSubscriptionCall;
import com.apollographql.apollo.internal.subscription.SubscriptionManager;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qz.common.Constants;
import qz.installer.certificate.CertificateManager;
import qz.ui.BasicDialog;
import qz.ui.Themeable;
import qz.ui.component.EmLabel;
import qz.ui.component.IconCache;
import qz.utils.DmsWebSocketClient;
import qz.utils.GraphQLUtilities;
import qz.utils.MiscUtilities;
import redis.clients.jedis.Jedis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import static qz.common.DmsContants.*;


public class DmsSettingDialog extends JDialog {

    private static final Logger log = LoggerFactory.getLogger(DmsSettingDialog.class);
    private final static String TOKEN_ACCESS_ID = "D3VT0K3N2YhWbUZ1WlhOellWOU53Nmx1WkdWNjM5NTg3";
    private final static String GRAPHQL_URL = "https://dms-graphql-task-35571-windowstickers.envoyproxy.automatrix.com/graphql";
    private final static String WSS_URL = "wss://dms-graphql-task-35571-windowstickers.envoyproxy.automatrix.com/graphql";
    private JTextField txtTokenIdentifier;
    private final IconCache iconCache;
    JLabel errorTxt = new EmLabel("", 1);

    public DmsSettingDialog(Frame owner, String title, IconCache iconCache) {
        super(owner, title, true);

        this.iconCache = iconCache;
        initComponents();
    }

    public void initComponents() {
        // Logic for create property and reload it
        MiscUtilities.writeDmsProperty(TOKEN_ID_KEY, TOKEN_ACCESS_ID, false);
        MiscUtilities.writeDmsProperty(DMS_GRAPHQL_URL, GRAPHQL_URL, false);
        MiscUtilities.writeDmsProperty(DMS_WSS_URL, WSS_URL, false);
        MiscUtilities.forceLoadProps();

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        errorTxt.setForeground(Color.RED);
        errorTxt.setVisible(false);
        JLabel lblTokenIdentifier = new EmLabel("Please insert the PIN Code:", 1);
        txtTokenIdentifier = new JTextField();
        txtTokenIdentifier.setColumns(12);
        Properties props = MiscUtilities.getProps();
        log.info("Props " + props.size());
        txtTokenIdentifier.setText(props.getProperty(TOKEN_ID_KEY));
        infoPanel.add(lblTokenIdentifier);
        infoPanel.add(errorTxt);
        infoPanel.add(txtTokenIdentifier);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(infoPanel);
        panel.add(new JSeparator());
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout());
        JButton btnSave = new JButton();
        btnSave.setText("Save");
        btnSave.setIcon(this.iconCache.getIcon(IconCache.Icon.SAVED_ICON));
        btnSave.addActionListener(saveListener());
        JButton btnCancel = new JButton();
        btnCancel.setText("Cancel");
        btnCancel.setIcon(this.iconCache.getIcon(IconCache.Icon.CANCEL_ICON));
        btnCancel.addActionListener(cancelListener());
        buttonsPanel.add(btnSave);
        buttonsPanel.add(btnCancel);
        panel.add(buttonsPanel);
        getContentPane().add(panel);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        pack();
        setResizable(false);
        setAlwaysOnTop(true);
        setLocationRelativeTo(null);    // center on main display
    }

    private void showErrorMessage(String message) {
        errorTxt.setText(message);
        errorTxt.setVisible(true);
    }

    private final ActionListener cancelListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        };
    }

    private final ActionListener saveListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String pinValue = txtTokenIdentifier.getText().trim();
                //if (pinValue.equals(null) || "".equals(pinValue))
                //    showErrorMessage("This field can't be empty");
                //if (pinValue.length() != 6)
                //    showErrorMessage("Please insert the 6 digits");
                String bearerToken = "b/B/VbxE2og2ZTg1YzZhZS1mOTUwLTRhMzMtYTFhOC02NTY1ZjRkZjVmOWUzOTU4Nw==";
                //String query = "{\n" +
                //        "  getPrinters (lotId: 3) {\n" +
                //        "    name id \n" +
                //        "  }\n" +
                //        "}";
                //JSONObject responseJson = GraphQLUtilities.requestGraphQL(query, bearerToken);
                //ApolloClient apolloClient = ApolloClient.builder().serverUrl("http://localhost:4000/graphql").build();
                //apolloClient.subscribe(new Su)
                log.info("Lets try this");
                DmsWebSocketClient.testWebSocket("ws://localhost:4000/graphql", bearerToken);
                //requestForNewAuthToken(txtTokenIdentifier.getText());
                //log.info("response JSON " + responseJson);
                //setVisible(false);
            }
        };
    }

    public void requestForNewAuthToken(String pin) {
        String query = String.format("mutation {\n" +
                "  getTokenByPIN(pin: %d) {\n" +
                "    token\n" +
                "    lotId\n" +
                "  }\n" +
                "}", Integer.valueOf(pin));
        JSONObject responseJson = GraphQLUtilities.requestGraphQL(query, TOKEN_ACCESS_ID);
        log.info("response JSON " + responseJson);
    }
}