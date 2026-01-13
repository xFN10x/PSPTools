package fn10.psptools.ui.interfaces;

import java.util.EventListener;

import fn10.psptools.ui.components.ParamSFOListElement;

public interface SFOListElementListener extends EventListener {

    public void selected(ParamSFOListElement selectedElement);

    public void backup();

    public void restore();

    public void delete(ParamSFOListElement selectedElement);

}
