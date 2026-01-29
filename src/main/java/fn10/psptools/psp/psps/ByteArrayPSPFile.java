package fn10.psptools.psp.psps;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import fn10.psptools.psp.PSPFile;

public class ByteArrayPSPFile implements PSPFile {

    private final String name;
    private final byte[] data;

    public ByteArrayPSPFile(String name, byte[] data) {
        this.name = name;
        this.data = data;
    }

    @Override
    public byte[] readAll() {
        return data;
    }

    @Override
    public String readString() {
        return new String(data);
    }

    @Override
    public InputStream openStream() {
        System.out.println("OPENING STREAM: " + data);
        return new ByteArrayInputStream(data);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getExtension() {
        return getName().substring(getName().lastIndexOf(".") + 1);
    }

    @Override
    public boolean actuallyExists() {
        return true;
    }

}
