package fn10.psptools.util;

import java.awt.Frame;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;

import com.formdev.flatlaf.util.SwingUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fn10.psptools.gson.PathTypeAdapter;
import fn10.psptools.psp.PSP;
import fn10.psptools.ui.LoadingScreen;

public class SavedVariables {

    private transient static final Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(Path.class, new PathTypeAdapter())
            .setPrettyPrinting().create();
    public static transient final Path DataFolder = Path.of(System.getProperty("user.home"), "/PSPTools/");
    static {
        if (!DataFolder.toFile().exists())
            try {
                Files.createDirectory(DataFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
    public transient static final Path saveLocation = Path.of(DataFolder.toString(), "PSPToolsSettings.json");

    public PSP LastSelectedPSP;
    public URL DatabaseUrl;
    public Date SinceLastPatchUpdate;

    public static void installApolloTools(Frame parent) {
        try {
            int option = JOptionPane.showConfirmDialog(parent,
                    "Install Apollo CLI Tools? (Required for Save Patching)",
                    "CLI Tools Downloader", JOptionPane.YES_NO_OPTION);

            if (option != JOptionPane.YES_OPTION)
                return;

            LoadingScreen loading = new LoadingScreen(parent);
            SwingUtilities.invokeLater(() -> {
                loading.setVisible(true);
            });
            new Thread(() -> {
                try {
                    loading.changeText("Opening connection...");

                    URL patchZip;

                    if (SystemUtils.IS_OS_WINDOWS) {
                        if (System.getProperty("sun.arch.data.model") == "64") {
                            patchZip = new URI(
                                    "https://github.com/bucanero/apollo-lib/releases/download/v1.3.0/apollo-cli-43bace95-Win64.zip")
                                    .toURL();
                        } else {
                            patchZip = new URI(
                                    "https://github.com/bucanero/apollo-lib/releases/download/v1.3.0/apollo-cli-43bace95-Win32.zip")
                                    .toURL();
                        }
                    } else if (SystemUtils.IS_OS_LINUX) {
                        patchZip = new URI(
                                "https://github.com/bucanero/apollo-lib/releases/download/v1.3.0/apollo-cli-43bace95-ubuntu.zip")
                                .toURL();
                    } /*
                       * else if (SystemUtils.IS_OS_MAC) {
                       * patchZip = new URI(
                       * "https://github.com/bucanero/apollo-lib/releases/download/v1.3.0/apollo-cli-43bace95-macos.zip")
                       * .toURL();
                       * }
                       */ else {
                        JOptionPane.showMessageDialog(parent,
                                "Sadly, Apollo CLI Tools are not available for this platform.",
                                "What the hell are you on??", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    InputStream stream = patchZip.openStream();
                    File tempFile = File.createTempFile("PSPTOOLS", "TEMPTOOLS.zip");
                    File tempFile2 = Files.createTempDirectory("PSPTOOLS").toFile();

                    FileOutputStream output = new FileOutputStream(tempFile);

                    loading.changeText("Writing to temporary file...");
                    IOUtils.copy(stream, output);

                    loading.changeText("Deleting old install...");
                    FileUtils.deleteDirectory(Path.of(SavedVariables.DataFolder.toString(), "tools").toFile()); // remove
                    // old
                    // patches

                    // tempFile.deleteOnExit();
                    System.out.println(tempFile.getPath());
                    stream.close();

                    loading.changeText("Download tools...");
                    ZipUnArchiver zip = new ZipUnArchiver(tempFile);

                    // extract tar gz
                    zip.setDestDirectory(tempFile2);
                    zip.extract();

                    // extract the new tar gz
                    final TarGZipUnArchiver unarc = new TarGZipUnArchiver(
                            Path.of(tempFile2.getAbsolutePath(), "build.tar.gz").toFile());
                    unarc.setDestDirectory(SavedVariables.DataFolder.toFile());
                    unarc.extract();

                    System.gc();

                    tempFile.delete();

                    loading.setVisible(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                JOptionPane.showMessageDialog(null, "Apollo CLI Tools have been downloaded.");
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean hasApolloToolsInstalled() {
        String folderName = "tools";
        if (SystemUtils.IS_OS_WINDOWS)
            return Path.of(DataFolder.toString(), folderName, "patcher.exe").toFile().exists()
                    && Path.of(DataFolder.toString(), folderName, "parser.exe").toFile().exists()
                    && Path.of(DataFolder.toString(), folderName, "dumper.exe").toFile().exists()
                    && Path.of(DataFolder.toString(), folderName, "patcher-bigendian.exe").toFile().exists();
        else
            return Path.of(DataFolder.toString(), folderName, "patcher").toFile().exists()
                    && Path.of(DataFolder.toString(), folderName, "parser").toFile().exists()
                    && Path.of(DataFolder.toString(), folderName, "dumper").toFile().exists()
                    && Path.of(DataFolder.toString(), folderName, "patcher-bigendian").toFile().exists();
    }

    public void Save() {
        try {

            String json = gson.toJson(this);
            Files.write(saveLocation, json.getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

        } catch (Exception e) {
            ErrorShower.showError(null, "Failed to save settings.", e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public static SavedVariables Load() {
        try {

            if (saveLocation.toFile().exists())
                return gson.fromJson(Files.readString(saveLocation), SavedVariables.class);
            else
                return new SavedVariables();
        } catch (Exception e) {
            ErrorShower.showError(null, "Failed to get settings.", e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }

}
