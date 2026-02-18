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
package fn10.psptools.ui.components;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;

import com.google.common.io.LittleEndianDataInputStream;

import fn10.psptools.ui.LoadingScreen;
import fn10.psptools.ui.interfaces.VideoPlayingListener;
import fn10.psptools.util.SavedVariables;
import ws.schild.jave.process.ffmpeg.FFMPEGProcess;

import java.awt.image.BufferedImage;

public class MediaPlayer {

    private boolean playing = false;
    private boolean aborted = false;
    private volatile boolean loading = true;
    private Thread videoThread;
    private final String id;
    private static final String defaultFFmpegPath = SavedVariables.DataFolder.resolve("tools", "ffmpeg.exe").toString();
    private static final String defaultFFplayPath = SavedVariables.DataFolder.resolve("tools", "ffplay.exe").toString();
    private static final String defaultFFmpegPathLinux = SavedVariables.DataFolder.resolve("tools", "ffmpeg").toString();
    private static final String defaultFFplayPathLinux = SavedVariables.DataFolder.resolve("tools", "ffplay").toString();
    public static FFMPEGProcess currentFFmpeg = null;

    public static String getDefaultFFmpegPath() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return defaultFFmpegPath;
        } else {
            return defaultFFmpegPathLinux;
        }
    }

    public static String getDefaultFFplayPath() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return defaultFFplayPath;
        } else {
            return defaultFFplayPathLinux;
        }
    }

    public static void downloadFFmpeg() {
        if (checkFFmpeg())
            return;
        int option = JOptionPane.showConfirmDialog(null,
                "<html>FFmpeg is not installed. You cannot view the XMB icons, or hear their music. <br/><br/>Do you want to download it?</html>",
                "FFmpeg not found", JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION)
            return;
        try {
            LoadingScreen loading = new LoadingScreen(null);
            SwingUtilities.invokeLater(() -> {
                loading.setVisible(true);
            });
            new Thread(() -> {
                loading.changeText("Opening connection...");
                try {
                    URL patchZip;

                    File tempFile = File.createTempFile("PSPTOOLS", "TEMPFFMPEG");
                    FileOutputStream output = new FileOutputStream(tempFile);
                    if (SystemUtils.IS_OS_WINDOWS) {
                        patchZip = new URI(
                                "https://github.com/GyanD/codexffmpeg/releases/download/8.0.1/ffmpeg-8.0.1-essentials_build.zip")
                                .toURL();
                        InputStream stream = patchZip.openStream();
                        int totalBytes = 108346640;// stream.available();
                        loading.changeText("Downloading...");
                        long count = 0;
                        byte[] buffer = new byte[2000000];
                        int n;
                        while (-1 != (n = stream.read(buffer))) {
                            output.write(buffer, 0, n);
                            count += n;
                            loading.setProgress((int) (((float) count / (float) totalBytes) * 100));
                        }

                        stream.close();

                        ZipUnArchiver zip = new ZipUnArchiver(tempFile);
                        zip.extract("ffmpeg-8.0.1-essentials_build/bin/ffmpeg.exe",
                                new File(getDefaultFFmpegPath()).getParentFile());
                        zip.extract("ffmpeg-8.0.1-essentials_build/bin/ffplay.exe",
                                new File(getDefaultFFmpegPath()).getParentFile());
                        try {
                            FileUtils.moveFile(Path.of(new File(getDefaultFFmpegPath()).getParentFile().getPath(),
                                    "ffmpeg-8.0.1-essentials_build", "bin",
                                    "ffmpeg.exe").toFile(), new File(getDefaultFFmpegPath()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            FileUtils.moveFile(Path.of(new File(getDefaultFFplayPath()).getParentFile().getPath(),
                                    "ffmpeg-8.0.1-essentials_build", "bin",
                                    "ffplay.exe").toFile(), new File(getDefaultFFplayPath()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        FileUtils.deleteDirectory(
                                Path.of(new File(getDefaultFFmpegPath()).getParentFile().getPath(),
                                        "ffmpeg-8.0.1-essentials_build").toFile());
                    } else if (SystemUtils.IS_OS_LINUX) {
                        if (SystemUtils.OS_ARCH.equals("arm64")) {
                            patchZip = new URI(
                                    "https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-n8.0-latest-linuxarm64-gpl-8.0.tar.xz")
                                    .toURL();
                        } else {
                            patchZip = new URI(
                                    "https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-n8.0-latest-linux64-gpl-8.0.tar.xz")
                                    .toURL();
                        }
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "This platform is unsupported.",
                                "What the hell are you on??", JOptionPane.ERROR_MESSAGE);
                        output.close();
                        return;
                    }

                    output.close();

                    System.gc();

                    tempFile.delete();

                    loading.setVisible(false);

                    JOptionPane.showMessageDialog(null, "FFmpeg has been downloaded.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static boolean checkFFmpeg() {
        return new File(getDefaultFFmpegPath()).exists() && new File(getDefaultFFplayPath()).exists();
    }

    /**
     * Play an audio file
     * 
     * <p>
     * <p>
     * This function uses FFmpeg to start playing an audio file.
     * 
     * @param file the file path.
     */
    @SuppressWarnings("null")
    public static void playAudio(String file) {
        if (!checkFFmpeg())
            return;

        try {
            boolean hasLoopPoints = false;
            LittleEndianDataInputStream dis = new LittleEndianDataInputStream(Files.newInputStream(Path.of(file)));
            byte[] read = new byte[4];
            while (dis.read(read, 0, 4) != -1) {
                String magic = new String(read, StandardCharsets.UTF_8);
                // System.out.println(magic);
                if (magic.equals("smpl")) {
                    dis.skip(48);
                    hasLoopPoints = true;
                    break;
                }
            }
            FFMPEGProcess ffplay = new FFMPEGProcess(getDefaultFFplayPath());
            if (hasLoopPoints) {
                int loopStart = dis.readInt();
                int loopEnd = dis.readInt() - loopStart;

                ffplay.addArgument("\"" + file + "\"");
                ffplay.addArgument("-af");
                ffplay.addArgument("\"aloop=loop=-1:size=" + loopEnd + ":start=" + loopStart + "\"");
                ffplay.addArgument("-nodisp");

                if (currentFFmpeg != null)
                    currentFFmpeg.destroy();
                currentFFmpeg = ffplay;
                ffplay.execute();

                System.out.println(
                        "ffplay.exe " + file + " " + "-af" + " " + "\"aloop=loop=-1:size=" + loopEnd + ":start="
                                + loopStart + "\"");
            } else {

                ffplay.addArgument("\"" + file + "\"");
                ffplay.addArgument("-nodisp");

                if (currentFFmpeg != null)
                    currentFFmpeg.destroy();
                currentFFmpeg = ffplay;
                ffplay.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop all playing audio
     */
    public static void stopAllAudio() {
        if (!checkFFmpeg())
            return;
        if (currentFFmpeg != null)
            currentFFmpeg.destroy();
    }

    /**
     * Setup a MediaPlayer with a file for the video.
     * 
     * <p>
     * <p>
     * The media player will save all frames of the video to files, then when it
     * plays, it loads all the frames into ram and plays them.
     * 
     * @param file The file path of the video. Any FFmpeg supported formats, so PMF
     *             should work fine.
     */
    public MediaPlayer(File file, String gameID) { // video
        this.id = gameID;
        if (!checkFFmpeg())
            return;
        FFMPEGProcess ffmpeg = new FFMPEGProcess(getDefaultFFmpegPath());
        Path frameFolderPath = SavedVariables.DataFolder.resolve("video", gameID);
        System.out.println(file.getAbsolutePath());
        if (!frameFolderPath.toFile().exists()) {
            new Thread(() -> {
                try {
                    FileUtils.createParentDirectories(frameFolderPath.toFile());
                    Files.createDirectory(frameFolderPath);
                    ffmpeg.addArgument("-i");
                    ffmpeg.addArgument(file.getAbsolutePath());
                    ffmpeg.addArgument("\"" + frameFolderPath + File.separator + "%03d.jpg\"");
                    ffmpeg.execute();
                    ffmpeg.getProcessExitCode();
                    loading = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /**
     * Loads the frames into ram and shows to the VideoPlayingListener
     * 
     * @return a boolean, false meaning its still loading.
     */
    public boolean start(VideoPlayingListener listener) {
        if (!checkFFmpeg())
            return false;
        if (loading) {
            new Thread(() -> {
                new Timer("Loading-Timeout").schedule(new TimerTask() {

                    @Override
                    public void run() {
                        if (loading) {
                            aborted = true;
                            loading = false;
                            System.out.println("loading aborted");
                        }
                    }

                }, 4000);
                while (loading) {
                    System.out.println("loading...");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("done");
                if (!aborted)
                    start(listener);
            }).start();
            return false;
        }
        playing = true;
        videoThread = new Thread(() -> {
            try {
                List<BufferedImage> frames = new ArrayList<>();

                File framesPath = SavedVariables.DataFolder.resolve("video", id).toFile();
                for (File file : framesPath.listFiles()) {
                    try {
                        frames.add(ImageIO.read(file));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                while (playing) {
                    for (BufferedImage img : frames) {
                        try {
                            Thread.sleep(1000 / 30);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (!playing)
                            break;
                        listener.frameStepped(img);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        videoThread.start();

        return true;
    }

    /**
     * Release all the resources and stop.
     */
    public void stop() {
        if (!checkFFmpeg())
            return;
        if (videoThread != null)
            videoThread.interrupt();
        playing = false;
    }
}