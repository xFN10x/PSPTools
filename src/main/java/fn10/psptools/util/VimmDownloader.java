package fn10.psptools.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;

/**
 * A class to download to download things off <a href="https://vimm.net/">https://vimm.net/</a>
 * <br>
 * <br>
 * Based off of <a href="https://github.com/TrendingTechnology/VimmsDownloader/blob/master/script.py">https://github.com/TrendingTechnology/VimmsDownloader/blob/master/script.py</a>
 */
public class VimmDownloader {

    private final String base = "https://vimm.net/vault/";

    private VimmDownloader() {

    }

    static void main() throws IOException {
        System.out.println(of().getDownloadURLFromRomID("23991").toString());
    }

    public URI getDownloadURLFromRomID(String ID) throws IOException {
        Document doc = SSLHelper.getConnection(base + ID).ignoreHttpErrors(true).get();
        FormElement dlForm = (FormElement) doc.getElementById("dl_form");
        Elements idInput = dlForm.getElementsByAttributeValue("name", "mediaId");
        String id = idInput.attr("value");

        return URI.create("https://dl3.vimm.net/").resolve(id);
    }

    public static VimmDownloader of() {
        return new VimmDownloader();
    }
}
