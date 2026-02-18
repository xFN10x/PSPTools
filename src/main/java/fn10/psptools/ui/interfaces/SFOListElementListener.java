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
package fn10.psptools.ui.interfaces;

import java.util.EventListener;

import fn10.psptools.ui.components.ParamSFOListElement;

public interface SFOListElementListener extends EventListener {

    void selected(ParamSFOListElement selectedElement);

    void backup();

    void restore();

    void delete(ParamSFOListElement selectedElement);

    void onThreadCreate(Thread thread);

}
