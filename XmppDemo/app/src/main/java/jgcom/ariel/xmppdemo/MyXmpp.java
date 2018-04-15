package jgcom.ariel.xmppdemo;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.sasl.SASLMechanism;
import org.jivesoftware.smack.sasl.provided.SASLPlainMechanism;
import org.jivesoftware.smack.sm.StreamManagementException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;

import java.io.IOException;

/**
 * Created by Ariel-17 on 11/3/2017.
 */

public class MyXmpp {

    // flag veriable section;
    boolean isConnecting = false;
    boolean printToast = true;
    boolean isConnected = true;
    boolean isLogin = false;
    boolean isChatCreated = false;

    // Required variables && Constants
    private String TAG = MyXmpp.class.getSimpleName();

    public static String username;
    public static String password;
    private final String hostName = "ec2-52-213-76-100.eu-west-1.compute.amazonaws.com";
    private final String domainName = "ec2-34-250-186-43.eu-west-1.compute.amazonaws.com";
    private final Integer port = 5222;

    // Objects
    public static MyXmpp instance;
    private MyXmppService service;
    public XMPPTCPConnection connection;
    private ChatManagerListenerInterface chatManagerListenerInterface;
    private MessageListenerInterface messageListenerInterface;
    private ReceiptReceivedListener receiptReceivedListener;
    private Chat mChat;

    static {
        try {
            Class.forName("org.jivesoftware.smack.ReconnectionManager");
        } catch (ClassNotFoundException ex) {
            // problem loading reconnection manager
        }
    }

    MyXmpp(MyXmppService service, String username, String password) {
        this.service = service;
        this.username = username;
        this.password = password;
        inilizaConnection();
    }

    public static MyXmpp getInstance(MyXmppService service, String username, String password) {
        if (instance == null) {
            instance = new MyXmpp(service, username, password);
        }
        return instance;
    }

    public void initListeners() {
        receiptReceivedListener = receiptReceivedListener();
        messageListenerInterface = new MessageListenerInterface();
        chatManagerListenerInterface = new ChatManagerListenerInterface();
    }

    private void inilizaConnection() {
        initListeners();
        // connection configuration
        XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder();

        builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        builder.setDebuggerEnabled(true);
        builder.setServiceName(domainName);
        builder.setHost(hostName);
        builder.setPort(port);

        SASLMechanism mechanism = new SASLPlainMechanism();
        SASLAuthentication.registerSASLMechanism(mechanism);
        SASLAuthentication.blacklistSASLMechanism("SCRAM-SHA-1");
        SASLAuthentication.unBlacklistSASLMechanism("DIGEST-MD5");


        ProviderManager.addExtensionProvider(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceipt.Provider());
        ProviderManager.addExtensionProvider(DeliveryReceiptRequest.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceiptRequest.Provider());

        XMPPTCPConnection.setUseStreamManagementResumptiodDefault(true);
        XMPPTCPConnection.setUseStreamManagementDefault(true);
        // XMPP connecntion creation
        connection = new XMPPTCPConnection(builder.build());
        XMPPConnectionListener xmppConnectionListener = new XMPPConnectionListener();
        connection.addConnectionListener(xmppConnectionListener);
    }


    class XMPPConnectionListener implements ConnectionListener {

        @Override
        public void connected(XMPPConnection connection) {
            Log.e(TAG + ">>>", ">>> Connected");
            isConnected = true;


            DeliveryReceiptManager deliveryReceiptManager = DeliveryReceiptManager.getInstanceFor(connection);
            deliveryReceiptManager.setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.always);
            deliveryReceiptManager.autoAddDeliveryReceiptRequests();
            deliveryReceiptManager.addReceiptReceivedListener(receiptReceivedListener);

            // if connection is authenticated then login
            if (!connection.isAuthenticated()) {
                logIn();
            } else {
                Log.e(TAG + ">>>", "authentication failed");
            }
        }

        @Override
        public void authenticated(XMPPConnection connection, boolean resumed) {
            isLogin = true;

            Log.e(TAG + ">>>", " athenticated");
            ChatManager.getInstanceFor(connection).addChatListener(chatManagerListenerInterface);
            Log.e(TAG + ">>>", " connected ");
            isChatCreated = false;
        }

        @Override
        public void connectionClosed() {
            if (printToast) {
                MyToast.threadToast(service, "Connection closed");
            }
            Log.e(TAG + ">>>", "connection closed");
            isConnected = false;
            isLogin = false;
            isChatCreated = false;
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            if (printToast) {
                MyToast.threadToast(service, "Connection closed on error " + e);
            }
            Log.e(TAG + ">>>", "connection closed on error " + e);
            isConnected = false;
            isLogin = false;
            isChatCreated = false;
        }

        @Override
        public void reconnectionSuccessful() {
            if (printToast) {
                MyToast.threadToast(service, "Reconnectied");
            }
            Log.e(TAG + ">>>", "Reconnected");
            isConnected = true;
            isLogin = false;
        }

        @Override
        public void reconnectingIn(int seconds) {
            if (printToast) {
                MyToast.threadToast(service, "Reconnecting");
            }
            Log.e(TAG + ">>>", "Reconnecting");
            isLogin = false;
            isChatCreated = false;
        }

        @Override
        public void reconnectionFailed(Exception e) {
            if (printToast) {
                MyToast.threadToast(service, "Reconnecting error " + e);
            }
            Log.e(TAG + ">>>", "Reconnecting error " + e);
            isLogin = false;
            isConnected = false;
            isChatCreated = false;
        }
    }

    public void connect() {
        final AsyncTask<Void, Void, Boolean> connectionProcess = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                // if connection is already connected
                if (connection.isConnected()) {
                    return false;
                }
                // if connection not connected;
                isConnecting = true;
                if (printToast) {
                    MyToast.threadToast(service, "Connecting");
                }
                Log.e(TAG + ">>>", " connecting ....");

                try {
                    // connecting
                    connection.connect();
                    // this code is to get deleviery recipet:

                    isConnected = true;
                } catch (IOException e) {
                    Log.e(TAG + ">>>", "Error IO: " + e);
                    if (printToast) {
                        MyToast.threadToast(service, "Error IO: " + e);
                    }
                } catch (XMPPException e) {
                    Log.e(TAG + ">>>", "Error XMPP: " + e);
                    if (printToast) {
                        MyToast.threadToast(service, "Error XMPP : " + e);
                    }
                } catch (SmackException e) {
                    Log.e(TAG + ">>>", "Error SMACK: " + e);
                    if (printToast) {
                        MyToast.threadToast(service, "Error SMACK: " + e);
                    }
                } catch (Exception e) {
                    Log.e(TAG + ">>>", "Other Exception: " + e);
                    if (printToast) {
                        MyToast.threadToast(service, "Error SMACK: " + e);
                    }
                }
                return isConnecting = false;
            }
        };
        connectionProcess.execute();
    }

    public ReceiptReceivedListener receiptReceivedListener() {
        return new ReceiptReceivedListener() {
            @Override
            public void onReceiptReceived(String fromJid, String toJid, String receiptId, Stanza receipt) {
                // testing ....
                Log.e(">>>", "from jid " + fromJid);
                Log.e(">>>", "to jid " + toJid);
                Log.e(">>>", "reciept id  " + receiptId);
                Log.e(">>>", "Stanzaaaa  " + receipt);

            }
        };
    }

    public void logIn() {
        try {
            connection.login(username, password);
        } catch (IOException | SmackException | XMPPException e) {
            if (printToast) {
                MyToast.threadToast(service, "Error : " + e);
            }
            Log.e(TAG + ">>>", "Error : " + e);
        }
    }


    // this used to get messages from server side
    // this is attached while user authenticated by login
    class ChatManagerListenerInterface implements ChatManagerListener {
        @Override
        public void chatCreated(Chat chat, boolean createdLocally) {

            if (!createdLocally) {
                chat.addMessageListener(messageListenerInterface);
            }
        }
    }


    // getting messges from server
    // getting in the json format
    class MessageListenerInterface implements ChatMessageListener {
        @Override
        public void processMessage(Chat chat, Message message) {

            if (message.getBody() != null) {
                if (message.getType().equals(Message.Type.chat)) {
                    // log testing recieving message
                    Log.e(TAG + ">>>", ">>> message recived " + message);
                    sendMsgBroadCast(message.getBody());
                }
            }
        }
    }


    public void sendMessge(ChatModel chatModel) {
        Gson gson = new Gson();
        String messageJson = gson.toJson(chatModel);

        // Chat is inbuild class used to create chat at server
        // it will create chat accoring to particullar user.
        if (!isChatCreated) {
            mChat = ChatManager.getInstanceFor(connection).createChat("g2.gmail.com@ec2-34-250-186-43.eu-west-1.compute.amazonaws.com", messageListenerInterface);
            isChatCreated = true;
        }

        // its inbuild class used to send message,
        // its sending message in json format
        Message message = new Message();
        message.setBody(messageJson);
        message.setStanzaId(chatModel.getMsgId());
        message.setType(Message.Type.chat);

        if (connection.isAuthenticated()) {
            try {
                mChat.sendMessage(message);
                connection.sendPacket(message);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
                Log.e(TAG + ">>>", "Exception at message sending " + e);
            }
        } else {
            logIn();
        }
    }


    public void sendMsgBroadCast(String body) {
        Intent intent = new Intent("msg");
        intent.putExtra("msg", body);
        LocalBroadcastManager.getInstance(service).sendBroadcast(intent);
    }


}
