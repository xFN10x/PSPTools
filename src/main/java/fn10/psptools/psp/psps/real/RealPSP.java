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
import java.nio.file.Path;

import fn10.psptools.psp.PSP;
import fn10.psptools.psp.PSPDirectory;
import fn10.psptools.psp.SelectionMode;

public class RealPSP extends PSP {

    private final Path path;

    public RealPSP(Path path) {
        super();
        this.path = path;
    }

    @Override
    public boolean pspActive() {
        try {
            File PSPFolder = new File(Path.of(path.toString(), "PSP").toString());
            File ISOFolder = new File(Path.of(path.toString(), "ISO").toString());
            File PSPGameFolder = new File(Path.of(path.toString(), "PSP", "Game").toString());

            return (PSPFolder.exists() && ISOFolder.exists() && PSPGameFolder.exists());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public PSPDirectory getFolder(String child, String... others) {
        return new RealPSPDirectory(path.resolve(child, others).toFile());
    }

    @Override
    protected SelectionMode getSelectionMode() {
        return SelectionMode.PSP_DIR;
    }

    @Override
    protected String getSelectionData() {
        return path.toString();
    }

}
