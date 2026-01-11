package fn10.psptools.ui.components;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JOptionPane;

import com.google.common.io.LittleEndianDataInputStream;

import fn10.psptools.ui.interfaces.VideoPlayingListener;
import fn10.psptools.util.SavedVariables;
import ws.schild.jave.ScreenExtractor;
import ws.schild.jave.process.ffmpeg.FFMPEGProcess;

public class MediaPlayer {

    private boolean playing = true;
    private boolean loading = true;
    private static String defaultFFmpegPath = SavedVariables.DataFolder.resolve("ffplay.exe").toString();
    public static FFMPEGProcess currentFFmpeg = null;

    public static boolean checkFFmpeg() {
        if (!new File(defaultFFmpegPath).exists()) {
            JOptionPane.showConfirmDialog(null,
                    "FFmpeg is not installed. You cannot view the XMB icons, or hear their music. <br/><br/>Do you want to download it?",
                    "FFmpeg not found", JOptionPane.YES_NO_OPTION);
            return false;
        } else
            return true;
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
    public static void playAudio(String file) {
        if (!checkFFmpeg())
            return;

        try {
            // Read loop points in samples
            LittleEndianDataInputStream dis = new LittleEndianDataInputStream(Files.newInputStream(Path.of(file)));
            while (true) {
                byte[] read = new byte[4];

                dis.read(read, 0, 4);
                String magic = new String(read, StandardCharsets.UTF_8);
                // System.out.println(magic);
                if (magic.equals("smpl")) {
                    dis.skip(48);
                    break;
                }
            }
            int loopStart = dis.readInt();
            int loopEnd = dis.readInt() - loopStart;

            FFMPEGProcess ffplay = new FFMPEGProcess(defaultFFmpegPath);

            ffplay.addArgument("\"" + file + "\"");
            ffplay.addArgument("-af");
            ffplay.addArgument("\"aloop=loop=-1:size=" + loopEnd + ":start=" + loopStart + "\"");
            ffplay.addArgument("-nodisp");

            if (currentFFmpeg != null)
                currentFFmpeg.destroy();
            currentFFmpeg = ffplay;
            ffplay.execute();

            System.out.println("ffplay.exe " + file + " " + "-af" + " " + "\"aloop=loop=-1:size=" + loopEnd + ":start="
                    + loopStart + "\"");
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
    public MediaPlayer(String file) { // video
        if (!checkFFmpeg())
            return;
        ScreenExtractor extractor = new ScreenExtractor();
    }

    /**
     * Loads the frames into ram and shows to the VideoPlayingListiener
     * 
     * @return a boolean, false meaning its still loading.
     */
    public boolean start(VideoPlayingListener listener) {
        if (!checkFFmpeg())
            return false;
        if (loading)
            return false;
        return true;
    }

    /**
     * Release all the resources and stop.
     */
    public void stop() {
        if (!checkFFmpeg())
            return;

    }
}