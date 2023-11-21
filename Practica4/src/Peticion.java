import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;

public class Peticion extends Thread {
    public static File  directory = new File("Server");
    public static final String ServerPath = directory.getAbsolutePath();
    public static final String HomePage = "/prueba.json";
    public static final int BUFFER_SIZE = 1024;
    private byte[] buffer = new byte[BUFFER_SIZE];
    private Socket socket;
    private PrintWriter pw;
    protected BufferedOutputStream bos;
    protected BufferedReader br;
    public HashMap<String, String> listaMime;
    private String  extension;
    public Peticion(Socket socket) {
        this.socket = socket;
    }
    private String contentType;

    @Override
    public void run() {
        try {
            bos = new BufferedOutputStream(socket.getOutputStream());
            pw = new PrintWriter(new OutputStreamWriter(bos));

            Cabecera header = new Cabecera(socket.getInputStream());
            header.parse();
            // Archivo solicitado
            String file = header.getFile();
            file = file.equals("/") ? HomePage : file;
            extension = "";
            int i = file.lastIndexOf('.');
            if (i >= 0) {
                extension = file.substring(i+1);
            }

            System.out.println("Extension: "+extension);

            System.out.println("Petición: " + header.getMethod() + " " + file);
           // System.out.println("Header: "+header.toString());
            HashMap<String, String> parametros = header.getParametros();
            if (parametros.keySet().size() > 0) {
                System.out.println("Parámetros:");
                for (String s : parametros.keySet()) {
                    System.out.println(String.format("\t%s = %s", s, parametros.get(s)));
                }
            }

            if(header.getMethod().equals("GET") || header.getMethod().equals("POST")){
                sendFile(file);
            } else if(header.getMethod().equals("HEAD")){
                sendHeader(file);
            }

            bos.close();
            pw.close();

            socket.shutdownInput();
            socket.close();
        } catch (IOException ex) {
        }
    }

    private void log(String msg) {
        System.out.println(msg);
    }

    private void sendHeader(String file)throws FileNotFoundException, IOException {
        String mime= iniciaMiemes(extension);
        File seleccionado = new File(ServerPath + file);
        long length = seleccionado.length();
        int leidos = 0;
        System.out.println("Archivo Existe: "+seleccionado.exists());
        System.out.println("1) Directory: " + directory);
        System.out.println("2) Directory:" + ServerPath);
        String sb = "";
        if(seleccionado.exists()){
            contentType="Content-Type: "+mime+"\n";
            sb = sb + "HTTP/1.0 200 ok\n";
            sb = sb + "Server: Node/1.0 \n";
            sb = sb + "Date: " + new Date() + " \n";
            sb = sb + contentType;
            sb = sb + "Content-Length: " + length + " \n";
            sb = sb + "\n";
            bos.write(sb.getBytes());
            bos.flush();
        }else {
            contentType="Content-Type: "+mime+"\n";
            sb = sb + "HTTP/1.0 404 Not Found\n";
            sb = sb + "Server: Node/1.0 \n";
            sb = sb + "Date: " + new Date() + " \n";
            sb = sb + contentType;
            sb = sb + "Content-Length: " + length + " \n";
            sb = sb + "\n";
            bos.write(sb.getBytes());
            bos.flush();
        }

        System.out.println(contentType );
        if ((seleccionado.exists())) {
            System.out.println("Codigo:  200 ok");
        } else {
            System.out.println("Codigo:  404 Not Found");
        }
        System.out.println("Date: "+new Date());
        System.out.println("Content-Lenght: "+length);

        bos.flush();
    }

    private void sendFile(String file) throws FileNotFoundException, IOException {

        String mime= iniciaMiemes(extension);
        File seleccionado = new File(ServerPath + file);
        long length = seleccionado.length();
        int leidos = 0;
        System.out.println("Archivo Existe: "+seleccionado.exists());
        System.out.println("1) Directory: " + directory);
        System.out.println("2) Directory:" + ServerPath);
        String sb = "";
       if(seleccionado.exists()){
           contentType="Content-Type: "+mime+"\n";
           sb = sb + "HTTP/1.0 200 ok\n";
           sb = sb + "Server: Node/1.0 \n";
           sb = sb + "Date: " + new Date() + " \n";
           sb = sb + contentType;
           sb = sb + "Content-Length: " + length + " \n";
           sb = sb + "\n";
           bos.write(sb.getBytes());
           bos.flush();
       }else {
           contentType="Content-Type: "+mime+"\n";
           sb = sb + "HTTP/1.0 404 Not Found\n";
           sb = sb + "Server: Node/1.0 \n";
           sb = sb + "Date: " + new Date() + " \n";
           sb = sb + contentType;
           sb = sb + "Content-Length: " + length + " \n";
           sb = sb + "\n";
           bos.write(sb.getBytes());
           bos.flush();
       }

        System.out.println(contentType );
        if ((seleccionado.exists())) {
            System.out.println("Codigo:  200 ok");
        } else {
            System.out.println("Codigo:  404 Not Found");
        }
        System.out.println("Date: "+new Date());
        System.out.println("Content-Lenght: "+length);


        FileInputStream fr = new FileInputStream(seleccionado);
        leidos = fr.read(buffer);
        while (leidos != -1) {
            bos.write(buffer, 0, leidos);
            //pw.flush();
            leidos = fr.read(buffer);
        }
        bos.flush();
        fr.close();
    }

    private String iniciaMiemes(String file){
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
        listaMime.put("json" , "application/json");
    return listaMime.get(file);
    }
}