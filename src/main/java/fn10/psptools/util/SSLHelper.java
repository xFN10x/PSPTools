package fn10.psptools.util;

// Source - https://stackoverflow.com/a/58325184
// Posted by checklist, modified by community. See post 'Timeline' for change history
// Retrieved 2026-03-18, License - CC BY-SA 4.0

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class SSLHelper {

    static public Connection getConnection(String url){
        return Jsoup.connect(url).sslContext(SSLHelper.socketFactory());
    }

    static private SSLContext socketFactory() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to create a SSL socket factory", e);
        }
    }
}
