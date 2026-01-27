package fn10.psptools.psp;

import java.io.InputStream;

public interface PSPFile {

    public byte[] readAll();
    public String readString();
    public InputStream openStream();

    public String getName();
    public String getExtension();

}
