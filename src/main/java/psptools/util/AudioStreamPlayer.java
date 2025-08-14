package psptools.util;

import static java.lang.System.setIn;

import java.io.InputStream;
import java.util.Map;

import com.goxr3plus.streamplayer.stream.StreamPlayer;
import com.goxr3plus.streamplayer.stream.StreamPlayerEvent;
import com.goxr3plus.streamplayer.stream.StreamPlayerListener;

public class AudioStreamPlayer extends StreamPlayer implements StreamPlayerListener {

    public static void play(InputStream input) {
        play(input);
    }

    @Override
    public void opened(Object dataSource, Map<String, Object> properties) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'opened'");
    }

    @Override
    public void progress(int nEncodedBytes, long microsecondPosition, byte[] pcmData, Map<String, Object> properties) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'progress'");
    }

    @Override
    public void statusUpdated(StreamPlayerEvent event) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'statusUpdated'");
    }

}
