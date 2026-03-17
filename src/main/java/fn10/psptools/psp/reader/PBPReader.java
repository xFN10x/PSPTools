package fn10.psptools.psp.reader;

import fn10.psptools.psp.PSPFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PBPReader {

    public static PBPReader read(PSPFile file) {
        try (InputStream stream = file.openStream();) {
            String magic = new String(stream.readNBytes(4), StandardCharsets.UTF_8);
            if (!magic.equals("\0PBP")) throw new ReaderException("Unexpected magic: " + magic);
        } catch (IOException e) {
            throw new ReaderException("Failed reading PBP.", e);
        }
    }
}
