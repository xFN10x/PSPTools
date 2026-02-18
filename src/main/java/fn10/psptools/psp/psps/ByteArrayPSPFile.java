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
