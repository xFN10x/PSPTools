/*
    PSPTools - Management Utility for your PSP.
    Copyright (C) 2026 xFN10x (https://github.com/xFN10x)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or any
    later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package fn10.psptools.psp.psps.ftp;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.*;

import fn10.psptools.ui.LoadingScreen;
import fn10.psptools.util.ErrorShower;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPClient;

import com.google.gson.Gson;

import fn10.psptools.psp.PSP;
import fn10.psptools.psp.PSPDirectory;
import fn10.psptools.psp.SelectionMode;

import static fn10.psptools.PSPTools.log;

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

        client.setConnectTimeout(5000);
        client.setDataTimeout(Duration.ofSeconds(10));

        client.setListHiddenFiles(true);
        client.addProtocolCommandListener(new ProtocolCommandListener() {

            @Override
            public void protocolCommandSent(ProtocolCommandEvent event) {
                // System.out.println("FTP: Command sent: " + event.getCommand());
            }

            @Override
            public void protocolReplyReceived(ProtocolCommandEvent event) {
                // System.out.println("FTP: Reply recived: " + event.getMessage());
                if (event.getMessage().startsWith("50")) {
                    JOptionPane.showMessageDialog(null,
                            "FTP Server does not support command. (" + event.getMessage()
                                    + ",) this server may be incompatible with PSPTools.",
                            "FTP Error", JOptionPane.ERROR_MESSAGE);
                }
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
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicBoolean retur = new AtomicBoolean(true);
        new Thread(() -> {
            LoadingScreen loadingScreen = new LoadingScreen(alwaysOnTopFrame);
            loadingScreen.showWhenPossible();
            try {
                String connectingTo = "Connecting to " + host + ":" + port + "...";
                loadingScreen.changeText(connectingTo);
                log.info(connectingTo);
                client.connect(
                        host,
                        port);
                String loggingIn = "Logging in...";
                loadingScreen.changeText(loggingIn);
                log.info(loggingIn);
                client.login(username, pw);
                log.info("Logged in.");
                countDownLatch.countDown();
            } catch (Exception e) {
                log.error("Failed to connect to PSP.", e);
                JOptionPane.showMessageDialog(alwaysOnTopFrame, "Failed to connect to PSP.\n\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                countDownLatch.countDown();
                retur.set(false);
            } finally {
                loadingScreen.hideWhenPossible();
            }
        }).start();

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return retur.get();
    }

    @Override
    public PSPDirectory getFolder(String child, String... others) {
        String loggingFolderPath = child + "/" + String.join("/", others);
        try {
            System.out.println("Getting folder: " + loggingFolderPath);
            return new FTPPSPDirectory(client, "/").resolve(child, others).getDirectory();
        } catch (IOException e) {
            ErrorShower.full(alwaysOnTopFrame, "Failed to get FTP Folder: " + loggingFolderPath, e);
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
