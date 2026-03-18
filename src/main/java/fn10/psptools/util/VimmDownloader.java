package fn10.psptools.util;

import fn10.psptools.PSPTools;
import fn10.psptools.psp.PSP;
import fn10.psptools.ui.LoadingScreen;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CountDownLatch;

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
//    static void main() throws IOException, InterruptedException {
//        VimmGame[] as = of().getAllGames().toArray(new VimmGame[0]);
//        for (VimmGame a : as) {
//            PSPTools.log.info(a.toString());
//        }
//    }
//

    public Map<Character, List<VimmGame>> getAllGames() throws IOException, InterruptedException {
        return getAllGames(null);
    }

    public Map<Character, List<VimmGame>> getAllGames(@Nullable LoadingScreen ls) throws IOException, InterruptedException {
        char[] letters = "#ABCDEFGHIJKLMNOPQRSTUVWXZY".toCharArray();
        if (ls != null)
            ls.Steps = letters.length;
        HashMap<Character, List<VimmGame>> allGames = new HashMap<>();
        CountDownLatch countDownLatch = new CountDownLatch(letters.length);
        for (char letter : letters) {
            Thread thread = new Thread(() -> {
                try {
                    if (ls != null)
                        ls.increaseProgressBySteps("Getting games from letter: '" + letter + "'...");
                    PSPTools.log.info("Getting games from letter: '{}'...", letter);
                    allGames.put(letter, getGamesFromCategory(letter));
                } catch (Exception e) {
                    ErrorShower.full(PSP.alwaysOnTopFrame, e);
                }
                countDownLatch.countDown();
            });
            thread.setUncaughtExceptionHandler((t, e) -> {
                PSPTools.log.error("Failed to get games with letters: " + letter, e);
            });
            thread.start();
        }
        countDownLatch.await();
        return allGames;
    }

    public List<VimmGame> getGamesFromCategory(char letter) throws IOException {
        ArrayList<VimmGame> games = new ArrayList<>();
        String url;
        if (letter == '#') {
            url = base + "?p=list&system=PSP&section=number";
        } else {
            url = base + "PSP/" + letter;
        }

        Document doc = SSLHelper.getConnection(url).ignoreHttpErrors(true).get();
        if (doc.connection().response().statusCode() == 404) {
            PSPTools.log.info("Nothing found for {}.", letter);
            return Collections.emptyList();
        }
        Element table = doc.getElementsByClass("rounded centered cellpadding1 hovertable striped").getFirst();
        //System.out.println(table.getElementsByTag("tbody").get(1));
        Elements tableEntrys = table.getElementsByTag("tbody").get(1).getElementsByTag("tr");

        for (Element tableEntry : tableEntrys) {
            //looking at the website, the order is: title, region, version
            Elements elements = tableEntry.getElementsByTag("td");
            Element titleElement = elements.get(0).getElementsByTag("a").getFirst();

            String ver = elements.get(2).text();
            String link = titleElement.attr("href");
            String title = titleElement.text();
            String region = elements.get(1).getAllElements().get(2).attr("title");

            VimmGame ga = new VimmGame(title, region, ver, link.substring(link.lastIndexOf('/') + 1));
            //System.out.println(ga.toString());
            games.add(ga);
        }
        return Collections.unmodifiableList(games);
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

    public record VimmGame(String name, String region, String version, String gameID) {
        @NonNull
        public String toString() {
            return name + " (" + region + ") " + version + " [" + gameID + "]";
        }
    }
}
