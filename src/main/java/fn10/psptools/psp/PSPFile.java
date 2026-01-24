package fn10.psptools.psp;

import java.io.OutputStream;

public interface PSPFile {

    public byte[] readAll();
    public String readString();
    public OutputStream openStream();

}
