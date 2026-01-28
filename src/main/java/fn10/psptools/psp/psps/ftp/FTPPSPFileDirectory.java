package fn10.psptools.psp.psps.ftp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import fn10.psptools.psp.PSPDirectory;
import fn10.psptools.psp.PSPFile;
import fn10.psptools.psp.PSPFileDirectory;
import fn10.psptools.psp.psps.ByteArrayPSPFile;

public class FTPPSPFileDirectory implements PSPFileDirectory {
    private final FTPClient client;
    private final String filePath;

    public FTPPSPFileDirectory(FTPClient client, String file) {
        this.client = client;
        this.filePath = file;
    }

    @Override
    public boolean isDirectory() {
        try {
            client.changeWorkingDirectory("/");
            System.out.println("Checking if directory: " + filePath);
            FTPFile file = client.mlistFile(filePath);
            
            if (file != null) {
                return file.isDirectory();
            } else {
                System.out.println("File is null! Doing crude check");
                return (filePath.contains("."));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public PSPFile getFile() {
        try {
            client.changeWorkingDirectory("/");
            System.out.println("Getting file: " + filePath);
            FTPFile file = client.mlistFile(filePath);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            client.retrieveFile(filePath, byteArrayOutputStream);
            if (file == null)
                return null;
            return new ByteArrayPSPFile(file.getName(), byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public PSPDirectory getDirectory() {
        try {
            client.changeWorkingDirectory("/");
            System.out.println("Getting directory: " + filePath);
            return new FTPPSPDirectory(client, filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean actuallyExists() {
        return true;
    }

}
