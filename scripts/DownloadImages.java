import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Downloads a real product photo for every product from the free Pexels API
 * and saves it as src/main/resources/static/images/<name>.jpg
 *
 * Usage (from the project root):
 *   1. Get a free API key at https://www.pexels.com/api/
 *   2. Mac/Linux:  export PEXELS_API_KEY=your_key      Windows (cmd):  set PEXELS_API_KEY=your_key
 *   3. java scripts/DownloadImages.java
 *
 * Optional: PICK=1 java scripts/DownloadImages.java   -> uses the 2nd search result instead of the 1st
 * (try PICK=2, 3, 4 if a photo is not what you wanted). Edit the QUERIES below to change what is searched.
 */
public class DownloadImages {

    // file name (without extension)  ->  search phrase
    static final Map<String, String> QUERIES = new LinkedHashMap<>();
    static {
        QUERIES.put("running-shoes", "red running shoes");
        QUERIES.put("sneakers", "white high top sneakers");
        QUERIES.put("tshirt", "teal t-shirt clothing");
        QUERIES.put("jeans", "blue denim jeans");
        QUERIES.put("hoodie", "purple hoodie");
        QUERIES.put("watch", "gold wrist watch");
        QUERIES.put("backpack", "orange backpack");
        QUERIES.put("sunglasses", "aviator sunglasses");
        QUERIES.put("cap", "navy baseball cap");
        QUERIES.put("headphones", "black wireless headphones");
        QUERIES.put("smartphone", "smartphone mobile phone");
        QUERIES.put("laptop", "silver laptop");
    }

    public static void main(String[] args) throws Exception {
        String key = System.getenv("PEXELS_API_KEY");
        if (key == null || key.isBlank()) {
            System.err.println("Set PEXELS_API_KEY first (free key: https://www.pexels.com/api/)");
            System.exit(1);
        }
        int pick = Integer.parseInt(System.getenv().getOrDefault("PICK", "0"));
        Path dir = Path.of("src/main/resources/static/images");
        Files.createDirectories(dir);

        HttpClient http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        Pattern large = Pattern.compile("\"large\"\\s*:\\s*\"(https:[^\"]+)\"");
        int ok = 0;

        for (Map.Entry<String, String> e : QUERIES.entrySet()) {
            try {
                String q = URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8);
                HttpRequest search = HttpRequest.newBuilder(URI.create(
                        "https://api.pexels.com/v1/search?query=" + q + "&per_page=10&orientation=square"))
                        .header("Authorization", key).build();
                HttpResponse<String> res = http.send(search, HttpResponse.BodyHandlers.ofString());
                if (res.statusCode() != 200) {
                    System.out.println("FAIL " + e.getKey() + " (HTTP " + res.statusCode() + ") - check your API key");
                    continue;
                }
                List<String> urls = new ArrayList<>();
                Matcher m = large.matcher(res.body());
                while (m.find()) urls.add(m.group(1).replace("\\/", "/").replace("\\u0026", "&"));
                if (urls.isEmpty()) {
                    System.out.println("FAIL " + e.getKey() + " - no results for '" + e.getValue() + "'");
                    continue;
                }
                String url = urls.get(Math.min(pick, urls.size() - 1));
                HttpResponse<byte[]> img = http.send(HttpRequest.newBuilder(URI.create(url)).build(),
                        HttpResponse.BodyHandlers.ofByteArray());
                Files.write(dir.resolve(e.getKey() + ".jpg"), img.body());
                System.out.println("OK   " + e.getKey() + ".jpg  <-  '" + e.getValue() + "'");
                ok++;
                Thread.sleep(300);
            } catch (Exception ex) {
                System.out.println("FAIL " + e.getKey() + " - " + ex.getMessage());
            }
        }
        System.out.println("\nDone: " + ok + "/" + QUERIES.size() + " photos saved to " + dir
                + "\nRestart the app and hard-refresh the browser (Ctrl+F5).");
    }
}
