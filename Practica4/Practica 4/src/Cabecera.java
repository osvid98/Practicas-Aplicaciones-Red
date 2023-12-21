import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Cabecera {

    private final static String CONTENT_LENGTH_FLAG = "th:";
    public final static String MetodoGET = "GET";
    public final static String MetodoPOST = "POST";

    private BufferedReader br;
    private String method;
    private String file;
    private HashMap<String, String> parametros;

    public Cabecera(InputStream is) {
        this.br = new BufferedReader(new InputStreamReader(is));
        this.parametros = new HashMap<>();
    }

    public String getMethod() {
        return method;
    }

    public String getFile() {
        return file;
    }

    public HashMap<String, String> getParametros() {
        return parametros;
    }

    public void parse() throws IOException {
        String line = br.readLine();
        if (line != null) {
            String[] fields = line.split(" ");
            method = fields[0];
            file = fields[1];

            System.out.println("\n----------\n");
    
            System.out.println("Cabecera: " + fields[2]);
            System.out.println("File: " + file);
    
            int qmark = file.indexOf("?");
            String fullParams = (qmark >= 0) ? file.substring(qmark + 1) : null;
    
            if (MetodoPOST.equals(method)) {
                readPostParams();
            }
    
            if (fullParams != null) {
                fillParams(fullParams.split("&"));
            }
        }
    }

    private void readPostParams() throws IOException {
        String buff;
        int mxRead = 0;

        while ((buff = br.readLine()).length() > 0) {
            if (buff.contains(CONTENT_LENGTH_FLAG)) {
                mxRead = Integer.parseInt(buff.substring(buff.indexOf(CONTENT_LENGTH_FLAG)
                        + CONTENT_LENGTH_FLAG.length() + 1));
            }
        }

        char[] data = new char[mxRead];
        br.read(data, 0, mxRead);
        fillParams(new String(data).split("&"));
    }

    private void fillParams(String[] paramString) {
        for (String s : paramString) {
            String[] pp = s.split("=");
            parametros.put(pp[0], (pp.length == 2) ? pp[1] : "");
        }
    }
}
