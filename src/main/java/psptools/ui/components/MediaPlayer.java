package psptools.ui.components;

import java.nio.ShortBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import com.google.common.io.LittleEndianDataInputStream;

import psptools.ui.interfaces.VideoPlayingListiener;

public class MediaPlayer {

    private volatile Thread playThread;
    private boolean playing = true;

    public MediaPlayer(String file) {
        if (file.isEmpty())
            return;

        playThread = new Thread(() -> {
            try {
                // Read loop points in samples
                LittleEndianDataInputStream dis = new LittleEndianDataInputStream(Files.newInputStream(Path.of(file)));
                while (true) {
                    byte[] read = new byte[4];

                    dis.read(read, 0, 4);
                    String magic = new String(read, StandardCharsets.UTF_8);
                    //System.out.println(magic);
                    if (magic.equals("smpl")) {
                        dis.skip(48);
                        break;
                    }
                }
                int loopStart = dis.readInt();
                //System.out.println(loopStart);
                int loopEnd = dis.readInt() * 2;
                //System.out.println(loopEnd);

                // Grab audio with forced stereo output
                FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file);
                grabber.setAudioChannels(2); // force stereo
                grabber.start();

                int sampleRate = grabber.getSampleRate();
                int channels = 2; // fixed stereo
                int frameSize = 2 * channels;

                int correctRate = sampleRate * 2; // correct ATRAC3 rate

                grabber.setAudioBitrate(correctRate);
                AudioFormat format = new AudioFormat(correctRate, 16, 2, true, false);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                // Then write PCM samples to line.

                line.start();

                int currentSample = 0;

                while (playing) {
                    Frame frame = grabber.grabSamples();

                    if (frame == null) {

                        System.out.println(loopEnd + "end " + (long) ((loopStart / (double) sampleRate) * 1_000_000));
                        grabber.setTimestamp((long) ((loopStart / (double) sampleRate) * 1_000_000), true);
                        currentSample = loopStart;
                        continue;
                    }

                    Object[] samples = frame.samples;
                    if (samples == null || samples.length == 0)
                        continue;

                    ShortBuffer left = (ShortBuffer) samples[0];
                    ShortBuffer right = channels == 2 && samples.length > 1 ? (ShortBuffer) samples[1] : null;

                    int availableSamples = left.remaining();
                    if (loopEnd != 0 && currentSample + availableSamples > loopEnd) {
                        availableSamples = loopEnd - currentSample;
                    }

                    // ensure full frames only
                    availableSamples -= availableSamples % channels;
                    if (availableSamples <= 0) {
                        grabber.setTimestamp((long) ((loopStart / (double) sampleRate) * 1_000_000));
                        currentSample = loopStart;
                        continue;
                    }

                    byte[] bytes = new byte[availableSamples * frameSize];
                    for (int i = 0; i < availableSamples; i++) {
                        if (!playing) {
                            line.stop();
                            break;
                        }
                        short l = left.get();
                        short r = right != null ? right.get() : l; // duplicate mono to stereo

                        int idx = i * 4;
                        bytes[idx] = (byte) (l & 0xFF);
                        bytes[idx + 1] = (byte) ((l >> 8) & 0xFF);
                        bytes[idx + 2] = (byte) (r & 0xFF);
                        bytes[idx + 3] = (byte) ((r >> 8) & 0xFF);
                    }
                    // System.out.println(currentSample);
                    if (playing)
                        line.write(bytes, 0, bytes.length);
                    else {
                        line.stop();
                        break;
                    }
                    currentSample += availableSamples;

                    if (loopEnd != 0 && currentSample >= loopEnd) {
                        grabber.setTimestamp((long) ((loopStart / (double) sampleRate) * 1_000_000));
                        currentSample = loopStart;
                    }
                }

                grabber.stop();
                grabber.release();
                grabber.close();
                line.drain();
                line.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        playThread.start();
    }

    public MediaPlayer(String file, VideoPlayingListiener listiener) { // video
        if (file.isEmpty())
            return;

        playThread = new Thread(() -> {
            try {
                FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file);

                // grabber.setVideoCodecName("yuvj420p");
                grabber.setPixelFormat(avutil.AV_PIX_FMT_0BGR);

                grabber.start();

                Java2DFrameConverter converter = new Java2DFrameConverter();

                long lastTimeStamp = System.currentTimeMillis();

                while (!Thread.interrupted()) {
                    // System.out.println(grabber.getVideoFrameRate());
                    if ((System.currentTimeMillis() - lastTimeStamp) >= (grabber.getVideoFrameRate())) {
                        Frame frame = grabber.grab();
                        if (frame == null) {
                            grabber.restart();
                            frame = grabber.grab();
                        }
                        listiener.frameStepped(converter.convert(frame));
                        // System.out.println((System.currentTimeMillis() - lastTimeStamp));
                        // System.out.println((grabber.getVideoFrameRate()));
                        lastTimeStamp = System.currentTimeMillis();

                    }
                }

                grabber.stop();
                grabber.release();
                grabber.close();
                converter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        playThread.start();
    }

    public void stop() {
        playing = false;
        playThread.interrupt();
    }
}