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
import java.nio.file.Files;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import fn10.psptools.psp.PSPDirectory;
import fn10.psptools.psp.PSPFile;
import fn10.psptools.psp.PSPFileDirectory;

public class RealPSPDirectory implements PSPDirectory {

    private final File dir;

    public RealPSPDirectory(File dir) {
        this.dir = dir;
    }

    public File getDirOnDisc() {
        return dir;
    }

    @Override
    public PSPFile[] getFiles() {
        ArrayList<PSPFile> building = new ArrayList<>();
        for (File realFile : dir.listFiles()) {
            if (!realFile.isDirectory())
                building.add(new RealPSPFile(realFile));
        }
        return building.toArray(new RealPSPFile[0]);
    }

    @Override
    public PSPFileDirectory[] getAll() {
        ArrayList<RealPSPFileDirectory> building = new ArrayList<>();
        if (!dir.exists()) return new RealPSPFileDirectory[0];
        for (File realFile : dir.listFiles()) {
            building.add(new RealPSPFileDirectory(realFile));
        }
        return building.toArray(new RealPSPFileDirectory[0]);
    }

    @Override
    public PSPFile getFileWithName(String name) {
        for (PSPFile file : getFiles()) {
            if (file.getName().equalsIgnoreCase(name))
                return file;
        }
        return null;
    }

    @Override
    public PSPFile getFileStartingWith(String prefix) {
        for (PSPFile file : getFiles()) {
            if (file.getName().toLowerCase().startsWith(prefix.toLowerCase()))
                return file;
        }
        return null;
    }

    @Override
    public void delete() {
        try {
            Files.deleteIfExists(dir.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return dir.getName();
    }

    @Override
    public PSPFileDirectory resolve(String first, String... children) {
        return new RealPSPFileDirectory(dir.toPath().resolve(first, children));
    }

    @Override
    public PSPFileDirectory resolve(String first) {
        return new RealPSPFileDirectory(dir.toPath().resolve(first));
    }

    @Override
    public void addFile(File file) {
        try {
            FileUtils.copyFileToDirectory(file, dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
