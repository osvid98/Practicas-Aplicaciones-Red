import java.util.HashMap;

public class ListMimes {

     public HashMap<String, String> listaMime;

     public  void ListMimes () {
        listaMime = new HashMap<>();
        listaMime.put("doc", "application/msword");
        listaMime.put("pdf", "application/pdf");
        listaMime.put("rar", "application/x-rar-compressed");
        listaMime.put("mp3", "audio/mpeg");
        listaMime.put("jpg", "image/jpeg");
        listaMime.put("jpeg", "image/jpeg");
        listaMime.put("png", "image/png");
        listaMime.put("html", "text/html");
        listaMime.put("htm", "text/html");
        listaMime.put("mp4", "video/mp4");
        listaMime.put("java", "text/plain");
        listaMime.put("c", "text/plain");
        listaMime.put("txt", "text/plain");
    }

}