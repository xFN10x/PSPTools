package fn10.psptools.ui.components;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import fn10.psptools.ui.interfaces.VideoPlayingListener;
import fn10.psptools.util.SavedVariables;
import ws.schild.jave.ScreenExtractor;
import ws.schild.jave.process.ffmpeg.FFMPEGProcess;

public class MediaPlayer {

    private boolean playing = true;
    private boolean loading = true;
    private static String defaultFFmpegPath = SavedVariables.DataFolder.resolve("ffmpeg.exe").toString();
    public static FFMPEGProcess ffmpeg = new FFMPEGProcess(defaultFFmpegPath);

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
        if (!checkFFmpeg()) return;
        ffmpeg.addArgument(file);
        try {
            ffmpeg.execute(true, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop all playing audio
     */
    public static void stopAllAudio() {
        if (!checkFFmpeg()) return;
        ffmpeg.destroy();
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
        if (!checkFFmpeg()) return;
        ScreenExtractor extractor = new ScreenExtractor();
    }

    /**
     * Loads the frames into ram and shows to the VideoPlayingListiener
     * 
     * @return a boolean, false meaning its still loading.
     */
    public boolean start(VideoPlayingListener listener) {
        if (!checkFFmpeg()) return false;
        if (loading)
            return false;
        return true;
    }

    /**
     * Release all the resources and stop.
     */
    public void stop() {
        if (!checkFFmpeg()) return;

    }
}