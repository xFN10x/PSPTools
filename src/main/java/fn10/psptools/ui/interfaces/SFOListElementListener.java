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
