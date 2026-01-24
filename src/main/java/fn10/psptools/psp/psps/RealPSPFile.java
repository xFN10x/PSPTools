package fn10.psptools.psp.psps;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import fn10.psptools.psp.PSPFile;

public class RealPSPFile implements PSPFile {
    private File file;

    public static RealPSPFile of(File file) {
        RealPSPFile pspfile = new RealPSPFile();
        pspfile.file = file;
        return pspfile;
    }

    @Override
    public byte[] readAll() {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String readString() {
        return new String(readAll());
    }

    @Override
    public OutputStream openStream() {
        try {
            return Files.newOutputStream(file.toPath(), StandardOpenOption.READ);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
