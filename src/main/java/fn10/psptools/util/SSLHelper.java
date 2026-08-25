package fn10.psptools.util;

// Source - https://stackoverflow.com/a/58325184
// Posted by checklist, modified by community. See post 'Timeline' for change history
// Retrieved 2026-03-18, License - CC BY-SA 4.0

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class SSLHelper {

    static public Connection getConnection(String url){
        return Jsoup.connect(url).sslContext(SSLHelper.socketFactory());
    }

    static private SSLContext socketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, new java.security.SecureRandom());

            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to create a SSL socket factory", e);
        }
    }
}
