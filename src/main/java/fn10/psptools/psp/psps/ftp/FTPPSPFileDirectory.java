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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
        this.filePath = FTPPSPDirectory.fixPath(file);
    }

    @Override
    public boolean isDirectory() {
        try {
            client.changeWorkingDirectory(filePath);
            String printWorkingDirectory = client.printWorkingDirectory();
            System.out.println("Checking if directory: " + printWorkingDirectory);
            FTPFile[] listFiles = client.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (FTPFile ftpFile : listFiles) {
                    if (ftpFile.getName().equalsIgnoreCase(filePath.substring(filePath.lastIndexOf("/") + 1)))
                        return false;
                }
                return true;
            } else
                return false;
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
            FTPFile file = client.listFiles(filePath)[0];
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
