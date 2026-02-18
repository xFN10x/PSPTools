/*
    PSPTools - Management Utility for your PSP.
    Copyright (C) 2026 xFN10x (https://github.com/xFN10x)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or any
    later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
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
