package psptools.ui.interfaces;

import java.util.EventListener;

import psptools.ui.components.ParamSFOListElement;

public interface SFOListElementListiener extends EventListener {

    public void selected(ParamSFOListElement selectedElement);

    public void backup();

    public void restore();

    public void delete(ParamSFOListElement selectedElement);

}
