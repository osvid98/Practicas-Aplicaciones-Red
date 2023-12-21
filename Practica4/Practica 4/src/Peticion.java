import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;

public class Peticion extends Thread {
    private static final File directory = new File("Server");
    private static final String ServerPath = directory.getAbsolutePath();
    private static final String HomePage = "/prueba.json";
    private static final int BUFFER_SIZE = 1024;
    private byte[] buffer = new byte[BUFFER_SIZE];
    private static final String CRLF = "\r\n";

    private final Socket socket;
    private BufferedOutputStream bos;
    private PrintWriter pw;
    private BufferedReader br;
    private HashMap<String, String> listaMime;
    private String extension;
    private String contentType;

    public Peticion(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            bos = new BufferedOutputStream(socket.getOutputStream());
            pw = new PrintWriter(new OutputStreamWriter(bos));
            Cabecera header = new Cabecera(socket.getInputStream());
            header.parse();
            String file = header.getFile();

            if (file != null) {
                file = file.equals("/") ? HomePage : file;
                extension = file.contains(".") ? file.substring(file.lastIndexOf('.') + 1) : "";

                log("Extension: " + extension);
                log("Petición: " + header.getMethod() + " " + file);

                HashMap<String, String> parametros = header.getParametros();
                if (parametros.keySet().size() > 0) {
                    log("Parámetros:");
                    parametros.forEach((s, value) -> log(String.format("\t%s = %s", s, value)));
                }

                switch (header.getMethod()) {
                    case "GET":
                    case "POST":
                        sendFile(file);
                        break;
                    case "HEAD":
                        sendHeader(file);
                        break;
                    case "DELETE":
                        deleteFile(file);
                        break;
                    case "PUT":
                        sendPutResponse(file);
                        break;
                    default:
                        System.err.println("PETICION NO SOPORTADA");
                        break;
                }

            }

        } catch (IOException ex) {
            ex.printStackTrace(); // o utiliza tu sistema de registro
        } finally {
            try {
                socket.shutdownInput();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace(); // o utiliza tu sistema de registro
            }
        }
    }

    private void log(String msg) {
        System.out.println(msg);
    }

    private void sendHeader(String file) throws IOException {
        String mime = iniciaMiemes(extension);
        File selectedFile = new File(ServerPath + file);
        long length = selectedFile.length();

        String statusLine = selectedFile.exists() ? "HTTP/1.0 200 OK" : "HTTP/1.0 404 Not Found";
        contentType = "Content-Type: " + mime;
        String header = statusLine + CRLF +
                "Server: Node/1.0" + CRLF +
                "Date: " + new Date() + CRLF +
                contentType + CRLF +
                "Content-Length: " + length + CRLF + CRLF;

        bos.write(header.getBytes());
        bos.flush();

        log(contentType);
        log("Código: " + (selectedFile.exists() ? "200 OK" : "404 Not Found"));
        log("Fecha: " + new Date());
        log("Content-Length: " + length);
    }

    private void sendFile(String file) throws IOException {
        sendHeader(file);

        File selectedFile = new File(ServerPath + file);
        try (FileInputStream fr = new FileInputStream(selectedFile)) {
            int bytesRead;
            while ((bytesRead = fr.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            bos.flush();
        }
    }

    private void deleteFile(String file) throws IOException {
        File selectedFile = new File(ServerPath + file);

        if (selectedFile.exists()) {
            if (selectedFile.delete()) {
                sendDeleteResponse(file);
            } else {
                sendErrorResponse("No se pudo eliminar el archivo: " + file);
            }
        } else {
            sendErrorResponse("El archivo no existe: " + file);
        }
    }

    private void sendDeleteResponse(String file) throws IOException {
        String statusLine = "HTTP/1.0 200 OK";
        String response = statusLine + CRLF +
                "Server: Node/1.0" + CRLF +
                "Date: " + new Date() + CRLF +
                "Content-Type: text/plain" + CRLF +
                "Content-Length: " + 0 + CRLF + CRLF;

        bos.write(response.getBytes());
        bos.flush();

        log(response);
        log("Archivo eliminado: " + file);
    }

    private void sendErrorResponse(String message) throws IOException {
        String statusLine = "HTTP/1.0 404 Not Found";
        String response = statusLine + CRLF +
                "Server: Node/1.0" + CRLF +
                "Date: " + new Date() + CRLF +
                "Content-Type: text/plain" + CRLF +
                "Content-Length: " + message.length() + CRLF + CRLF +
                message;

        bos.write(response.getBytes());
        bos.flush();

        log(response);
    }

    private void sendPutResponse(String file) throws IOException {
        File selectedFile = new File(ServerPath + file);

        if (!selectedFile.exists()) {
            selectedFile.createNewFile(); // Crear el archivo si no existe
        }

        String statusLine = "HTTP/1.0 200 OK";
        String response = statusLine + CRLF +
                "Server: Node/1.0" + CRLF +
                "Date: " + new Date() + CRLF +
                "Content-Type: text/plain" + CRLF +
                "Content-Length: " + 0 + CRLF + CRLF;

        bos.write(response.getBytes());
        bos.flush();

        log(response);
        log("Archivo actualizado/creado: " + file);
    }

    private String iniciaMiemes(String file) {
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
        listaMime.put("json", "application/json");
        return listaMime.get(file);
    }
}
