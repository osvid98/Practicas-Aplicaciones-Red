import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Cabecera {

    private final static String CONTENT_LENGTH_FLAG = "th:";
    public final static String MetodoGET = "GET";
    public final static String MetodoPOST = "POST";
    public HashMap<String, String> listaMime;
    BufferedReader br;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public HashMap<String, String> getParametros() {
        return parametros;
    }

    public void setParametros(HashMap<String, String> parametros) {
        this.parametros = parametros;
    }
    private String method;
    private String file;
    private HashMap<String, String> parametros;
    private  String cabecera;
    private     String x;


    public Cabecera(InputStream is) {
        this.br = new BufferedReader(new InputStreamReader(is));
        this.parametros = new HashMap<String, String>();
    }

    void parse() throws IOException {
        String s = br.readLine();
        String[] fields;
        fields = s.split(" ");

        method = fields[0];
        file = fields[1];
        cabecera= fields[2];
        //x= fields[3];
        System.out.println("Cabecera: "+cabecera);
        System.out.println("File:  "+file);
        int qmark = file.indexOf("?");
        String fullParams = null;
        if (qmark >= 0) {
            fullParams = file.substring(qmark + 1);
            file = file.substring(0, qmark);
        }
        if (MetodoGET.equals(method)) {
            // TODO: Algo
        } else if (MetodoPOST.equals(method)) {
            String buff = br.readLine();
            int mxRead = 0;
            while (buff.length() > 0) {
                buff = br.readLine();
                if (buff.indexOf(CONTENT_LENGTH_FLAG) > 0) {
                    mxRead = Integer.parseInt(
                            buff.substring(buff.indexOf(CONTENT_LENGTH_FLAG)
                                    + CONTENT_LENGTH_FLAG.length() + 1));
                }
            }
            char[] data = new char[mxRead];
            br.read(data, 0, mxRead);
            fullParams = new String(data);
        }
        if(fullParams != null) {
            fillParams(fullParams.split("&"));
        }
    }

    private void fillParams(String[] paramString) {
        for (String s : paramString) {
            String[] pp = s.split("=");
            if (pp.length != 2) {
                parametros.put(pp[0], "");
            } else {
                parametros.put(pp[0], pp[1]);
            }
        }
    }

}