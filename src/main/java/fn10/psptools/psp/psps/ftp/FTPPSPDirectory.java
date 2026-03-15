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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import fn10.psptools.PSPTools;
import fn10.psptools.psp.PSP;
import fn10.psptools.util.ErrorShower;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import fn10.psptools.psp.PSPDirectory;
import fn10.psptools.psp.PSPFile;
import fn10.psptools.psp.PSPFileDirectory;
import fn10.psptools.psp.psps.ByteArrayPSPFile;

public class FTPPSPDirectory implements PSPDirectory {

    private final FTPClient client;
    private final String path;

    public FTPPSPDirectory(FTPClient client, String path) throws IOException {
        this.client = client;
        this.path = fixPath(path);
    }

    @Override
    public PSPFile[] getFiles() {
        try {
            PSPTools.log.info("Retrieving all files from: {}", path);
            ArrayList<PSPFile> building = new ArrayList<>();
            for (FTPFile ftpfile : client.listFiles(path)) {
                String file = path + "/" + ftpfile.getName();
                System.out.println(file);
                if (ftpfile.isDirectory()) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    client.retrieveFile(file, byteArrayOutputStream);
                    building.add(new ByteArrayPSPFile(ftpfile.getName(), byteArrayOutputStream.toByteArray(), () -> {
                        try {
                            client.changeWorkingDirectory("/");
                            client.dele(file);
                        } catch (IOException e) {
                            ErrorShower.full(PSP.alwaysOnTopFrame, e);
                        }
                    }));
                }
            }
            return building.toArray(new PSPFile[0]);
        } catch (Exception e) {
            ErrorShower.full(FTPPSP.alwaysOnTopFrame, "Failed to get all files from:" + path, e);
            return null;
        }
    }

    @Override
    public PSPFileDirectory[] getAll() {
        String Path = "";
        try {
            Path = client.printWorkingDirectory();
            client.changeWorkingDirectory(path);
            PSPTools.log.info("Retrieving all files & directories from: {}", Path);
            ArrayList<PSPFileDirectory> building = new ArrayList<>();
            FTPFile[] files = client.listFiles();
            for (FTPFile file : files) {
                if (file.getName().equalsIgnoreCase(".") || file.getName().equalsIgnoreCase(".."))
                    continue;
                PSPTools.log.info("Got file/dir : {}", file.getName());
                building.add(new FTPPSPFileDirectory(client, Path + "/" + file.getName()));
            }

            return building.toArray(new PSPFileDirectory[0]);
        } catch (Exception e) {
            ErrorShower.full(FTPPSP.alwaysOnTopFrame, "Failed to get all files & directories from: " + Path, e);
            return null;
        }
    }

    @Override
    public PSPFile getFileWithName(String name) {
        String printWorkingDirectory = "";
        try {
            client.changeWorkingDirectory(path);
            printWorkingDirectory = client.printWorkingDirectory();
            PSPTools.log.info("Getting file with name: {}, in path: {}", name, printWorkingDirectory);
            // System.out.println("So, Retriving all files from: " + printWorkingDirectory +
            // " with filter.");
            FTPFile[] listFiles = client.listFiles(null, new FTPFileFilter() {

                @Override
                public boolean accept(FTPFile file) {
                    return !file.isDirectory() && file.getName().equalsIgnoreCase(name);
                }

            });

            if (listFiles.length == 0)
                return null;
            FTPFile ftpfile = listFiles[0];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            client.retrieveFile(ftpfile.getName(), byteArrayOutputStream);
            String path = resolvePath(this.path, name);
            return new ByteArrayPSPFile(ftpfile.getName(), byteArrayOutputStream.toByteArray(), () -> {
                try {
                    client.changeWorkingDirectory("/");
                    client.dele(path);
                } catch (IOException e) {
                    PSPTools.log.error("Failed to delete file: " + path);
                }
            });
        } catch (Exception e) {
            ErrorShower.full(FTPPSP.alwaysOnTopFrame, "Failed to get file: " + printWorkingDirectory + "/" + name, e);
            return null;
        }
    }

    protected ByteArrayPSPFile makePSPFile(FTPFile file) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        client.retrieveFile(file.getName(), byteArrayOutputStream);
        String fullPath = resolvePath(client.printWorkingDirectory(), file.getName());
        return new ByteArrayPSPFile(file.getName(), byteArrayOutputStream.toByteArray(), () -> {

        });
    }

    @Override
    public PSPFile getFileStartingWith(String prefix) {
        try {
            client.changeWorkingDirectory(path);
            for (FTPFile file : client.listFiles()) {
                if (file.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
                    return makePSPFile(file);
                }
            }
            return null;
        } catch (Exception e) {
            ErrorShower.full(FTPPSP.alwaysOnTopFrame, "Couldn't find a file starting with: '" + prefix + "' in the dir: " + path, e);
            return null;
        }
    }

    @Override
    public void delete() {
        try {
            client.changeWorkingDirectory("/");
            client.dele(path);
        } catch (Exception e) {
            ErrorShower.full(FTPPSP.alwaysOnTopFrame, "Failed to delete: " + path, e);
        }
    }

    @Override
    public String getName() {
        try {
            client.changeWorkingDirectory(path);
            return client.printWorkingDirectory().substring(client.printWorkingDirectory().lastIndexOf("/") + 1);

        } catch (Exception e) {
            ErrorShower.full(PSP.alwaysOnTopFrame, "Couldn't get name of: " + path, e);
            return "err";
        }
    }

    public static String fixPath(String path) {
        return path.replaceAll("\\.\\/", "/").replaceAll("//", "/").replace("\\.", "");
    }

    public static String resolvePath(String i, String j, String... k) {
        StringBuilder sb = new StringBuilder(i);
        if (!i.startsWith("./")) {
            if (!i.startsWith("/"))
                sb.insert(0, "./");
            else
                sb.insert(0, ".");
        }

        if (sb.toString().endsWith("/") && !j.startsWith("/"))
            sb.append(j);
        else if (!sb.toString().endsWith("/") && j.startsWith("/"))
            sb.append(j);
        else if (!sb.toString().endsWith("/") && !j.startsWith("/"))
            sb.append("/");
        else
            sb.append(j);

        if (i.endsWith("/") && j.startsWith("/"))
            sb.append(j.substring(1));

        for (String k2 : k) {
            if (sb.toString().endsWith("/") && !k2.startsWith("/"))
                sb.append(k2);
            else if (!sb.toString().endsWith("/") && k2.startsWith("/"))
                sb.append(k2);
            else if (!sb.toString().endsWith("/") && !k2.startsWith("/"))
                sb.append("/");
            sb.append(k2);

            if (sb.toString().endsWith("/") && k2.startsWith("/"))
                sb.append(k2.substring(1));
        }
        return sb.toString().replaceAll("\\./", "/");
    }

    @Override
    public PSPFileDirectory resolve(String first, String... children) {
        System.out.println("Resolving from: " + path + ", to " + first + "/" + String.join("/", children));
        System.out.println("Resolved: " + resolvePath(path, first, children));
        return new FTPPSPFileDirectory(client, resolvePath(path, first, children));
    }

    @Override
    public PSPFileDirectory resolve(String first) {
        return new FTPPSPFileDirectory(client, Path.of(path).resolve(first).toString());
    }

    @Override
    public void addFile(File file) {
        try {
            client.changeWorkingDirectory(path);
            client.storeFile("", Files.newInputStream(file.toPath()));
        } catch (IOException e) {
            ErrorShower.full(PSP.alwaysOnTopFrame, "Couldn't add: " + file.getAbsolutePath() + " to:" + path, e);
        }
    }

}