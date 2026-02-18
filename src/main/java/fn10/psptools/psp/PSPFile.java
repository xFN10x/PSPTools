package fn10.psptools.psp;

import java.io.InputStream;

public interface PSPFile {

    byte[] readAll();
    String readString();
    InputStream openStream();

    String getName();
    String getExtension();
    boolean actuallyExists();

}
