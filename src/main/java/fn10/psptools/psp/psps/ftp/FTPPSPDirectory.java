package fn10.psptools.psp.psps.ftp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPFileFilters;
import org.apache.commons.net.ftp.parser.MLSxEntryParser;

import fn10.psptools.psp.PSPDirectory;
import fn10.psptools.psp.PSPFile;
import fn10.psptools.psp.PSPFileDirectory;
import fn10.psptools.psp.psps.ByteArrayPSPFile;

public class FTPPSPDirectory implements PSPDirectory {

    private final FTPClient client;
    private final String path;

    public FTPPSPDirectory(FTPClient client, String path) throws IOException {
        this.client = client;
        this.path = path;
    }

    @Override
    public PSPFile[] getFiles() {
        try {
            client.changeWorkingDirectory("/");
            System.out.println("Retriving all files from: " + client.printWorkingDirectory());
            ArrayList<PSPFile> building = new ArrayList<>();
            for (String file : client.listNames(path)) {
                System.out.println(file);
                FTPFile ftpfile = client.mlistFile(file);
                if (ftpfile.isDirectory()) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    client.retrieveFile(file, byteArrayOutputStream);
                    building.add(new ByteArrayPSPFile(ftpfile.getName(), byteArrayOutputStream.toByteArray()));
                }
            }
            return building.toArray(new ByteArrayPSPFile[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public PSPFileDirectory[] getAll() {
        try {
            client.changeWorkingDirectory("/");
            System.out
                    .println("Retriving all files & directories from: " + (path.startsWith("./") ? path : "./" + path));
            ArrayList<PSPFileDirectory> building = new ArrayList<>();
            String[] listNames = client.listNames(path.startsWith("./") ? path : "./" + path);
            if (listNames == null)
                return new FTPPSPFileDirectory[0];
            for (String file : listNames) {
                System.out.println(file);
                building.add(new FTPPSPFileDirectory(client, file));
            }
            return building.toArray(building.toArray(new FTPPSPFileDirectory[0]));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public PSPFile getFileWithName(String name) {
        try {
            client.changeWorkingDirectory("./");
            System.out.println("Getting file with name: " + name + ", in path: " + client.printWorkingDirectory());
            System.out.println("So, Retriving all files from: " + client.printWorkingDirectory() + " with filter.");
            FTPFile[] listFiles = client.listFiles(path, new FTPFileFilter() {

                @Override
                public boolean accept(FTPFile file) {
                    return !file.isDirectory() && file.getName().equalsIgnoreCase(name);
                }

            });
            /*
             * for (FTPFile ftpfile : ) {
             * if (ftpfile == null)
             * continue;
             * System.out.println("Path? " + path + "/" + ftpfile.getName());
             * if (!ftpfile.isDirectory() && ftpfile.getName().equalsIgnoreCase(name)) {
             * ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             * client.retrieveFile(path + "/" + ftpfile.getName(), byteArrayOutputStream);
             * return new ByteArrayPSPFile(ftpfile.getName(),
             * byteArrayOutputStream.toByteArray());
             * }
             * }
             */
            FTPFile ftpfile = listFiles[0];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            client.retrieveFile(path + "/" + ftpfile.getName(), byteArrayOutputStream);
            return new ByteArrayPSPFile(ftpfile.getName(), byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected ByteArrayPSPFile makePSPFile(FTPFile file) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        client.retrieveFile(file.getName(), byteArrayOutputStream);
        return new ByteArrayPSPFile(file.getName(), byteArrayOutputStream.toByteArray());
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
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void delete() {
        try {
            client.changeWorkingDirectory("/");
            client.dele(path);
            client.changeWorkingDirectory(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        try {
            client.changeWorkingDirectory(path);
            return client.printWorkingDirectory().substring(client.printWorkingDirectory().lastIndexOf("/") + 1);

        } catch (Exception e) {
            e.printStackTrace();
            return "err";
        }
    }

    public String resolvePath(String i, String j, String... k) {
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
        return sb.toString();
    }

    @Override
    public PSPFileDirectory resolve(String first, String... children) {
        System.out.println("Resolving from: " + path + ", to " + first + "/" + String.join("/", children));
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
            e.printStackTrace();
        }
    }

}
