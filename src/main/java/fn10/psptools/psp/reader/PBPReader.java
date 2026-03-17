package fn10.psptools.psp.reader;

import com.google.common.io.LittleEndianDataInputStream;
import fn10.psptools.PSPTools;
import fn10.psptools.psp.PSPFile;
import fn10.psptools.ui.components.ParamSFOListElement;
import fn10.psptools.ui.interfaces.SFOListElementListener;

import javax.imageio.ImageIO;
import javax.naming.NameNotFoundException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PBPReader {

    private final SFOReader param;
    private final byte[] ic0;
    private final byte[] ic1;
    private final byte[] pic0;
    private final byte[] pic1;
    private final byte[] sn0;

    protected PBPReader(
            SFOReader param,
            byte[] ic0,
            byte[] ic1,
            byte[] pic0,
            byte[] pic1,
            byte[] sn0) {
        this.param = param;
        this.ic0 = ic0;
        this.ic1 = ic1;
        this.pic0 = pic0;
        this.pic1 = pic1;
        this.sn0 = sn0;
    }

    public ParamSFOListElement createListElement(SFOListElementListener lis) throws NameNotFoundException, IOException {
        return new ParamSFOListElement(param, null,  ic0, pic1,  ic1, sn0, lis);
    }

    //https://www.psdevwiki.com/ps3/PBP#Content_Information_Files
    public static PBPReader ofPSPFile(PSPFile file) {
        return ofStream(file.openStream());
    }

    public static PBPReader ofStream(InputStream st) {
        try (LittleEndianDataInputStream stream = new LittleEndianDataInputStream(st)) {
            String magic = new String(stream.readNBytes(4), StandardCharsets.UTF_8);
            if (!magic.equals("\0PBP")) throw new ReaderException("Unexpected magic: " + magic);

            int unknownVerNumber = stream.readInt();
            int paramSfoOffset = stream.readInt();
            int icon0Offset = stream.readInt();
            int icon1Offset = stream.readInt();
            int pic0Offset = stream.readInt();
            int pic1Offset = stream.readInt();
            int snd0Offset = stream.readInt();
            int data1Offset = stream.readInt();
            int data2Offset = stream.readInt();

            byte[] paramData = stream.readNBytes(icon0Offset - paramSfoOffset);
            byte[] icon0 = stream.readNBytes(icon1Offset - icon0Offset);
            byte[] icon1 = stream.readNBytes(pic0Offset - icon1Offset);
            byte[] pic0 = stream.readNBytes(pic1Offset - pic0Offset);
            byte[] pic1 = stream.readNBytes(snd0Offset - pic1Offset);
            byte[] snd0 = stream.readNBytes(data1Offset - snd0Offset);

            SFOReader param = SFOReader.ofStream(new ByteArrayInputStream(paramData));
//            BufferedImage icon0Img = ImageIO.read(new ByteArrayInputStream(icon0));
//            BufferedImage icon1Img = ImageIO.read(new ByteArrayInputStream(icon1));
//            BufferedImage pic0Img = ImageIO.read(new ByteArrayInputStream(pic0));
//            BufferedImage pic1Img = ImageIO.read(new ByteArrayInputStream(pic1));

            PSPTools.log.info("Read PBP of title: {}", param.getParam(SFOReader.Params.Title));
            return new PBPReader(param, icon0, icon1, pic0, pic1, snd0);
        } catch (IOException | NameNotFoundException e) {
            throw new ReaderException("Failed reading PBP.", e);
        }
    }
}
