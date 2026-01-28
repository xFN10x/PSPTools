package fn10.psptools.psp.psps.ftp;

import java.io.IOException;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPClient;

import com.google.gson.Gson;

import fn10.psptools.psp.PSP;
import fn10.psptools.psp.PSPDirectory;
import fn10.psptools.psp.SelectionMode;

public class FTPPSP extends PSP {

    private final String username;
    private final String pw;
    private final String host;
    private final int port;
    private final static Gson gson = new Gson();
    private final FTPClient client;

    public FTPPSP(FTPClient client, String host, int port, String un, String pw) {
        super();
        this.host = host;
        this.port = port;
        this.pw = pw;
        this.username = un;
        this.client = client;

        client.addProtocolCommandListener(new ProtocolCommandListener() {

                @Override
                public void protocolCommandSent(ProtocolCommandEvent event) {
                    System.out.println("FTP: Command sent: " + event.getCommand());
                }

                @Override
                public void protocolReplyReceived(ProtocolCommandEvent event) {
                    System.out.println("FTP: Reply recived: " + event.getMessage());
                }

            });
    }

    public FTPPSP(FTPClient client, String host, int port) {
        this(client, host, port, "PSPTools", "");
    }

    @Override
    public boolean pspActive() {
        if (client.isConnected())
            return true;
        try {
            System.out.println("Connecting to " + host + ":" + port + "...");
            client.connect(
                    host,
                    port);
            System.out.println("Logging in...");
            client.login(username, pw);
            System.out.println("Logged in.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public PSPDirectory getFolder(String child, String... others) {
        try {
            System.out.println("Getting folder: " + child + "/" + String.join("?", others));
            return new FTPPSPDirectory(client, "/").resolve(child, others).getDirectory();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected SelectionMode getSelectionMode() {
        return SelectionMode.FTP;
    }

    @Override
    protected String getSelectionData() {
        return gson.toJson(new FTPSelectionData(host, port, username, pw));
    }

}
