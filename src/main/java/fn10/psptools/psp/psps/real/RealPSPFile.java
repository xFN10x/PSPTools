package fn10.psptools.psp.psps.real;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import fn10.psptools.psp.PSPFile;

public class RealPSPFile implements PSPFile {
    private final File file;

    public RealPSPFile(File file) {
        this.file = file;
    }

    public static RealPSPFile of(File file) {
        RealPSPFile pspfile = new RealPSPFile(file);
        return pspfile;
    }

    public File getFileOnDisk() {
        return file;
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
    public InputStream openStream() {
        try {
            return Files.newInputStream(file.toPath(), StandardOpenOption.READ);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String getExtension() {
        return getName().substring(getName().lastIndexOf(".")+1);
    }

    @Override
    public boolean actuallyExists() {
        return file.exists();
    }

}
