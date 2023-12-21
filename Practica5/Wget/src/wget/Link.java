package wget;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import static wget.Wget.archivosDesc;
import static wget.Wget.poolHilos;
import static wget.Wget.folderDescargas;

public class Link implements Runnable {

    public URL url;
    public boolean esArchivo;
    public int profundidad;
    public int maxProfundidad;
    protected Thread runningThread = null;
    //Constructor

    public Link(String url, int prof, int maxProf) {
        this.profundidad = prof;
        this.maxProfundidad = maxProf;
        try {
            this.url = new URL(url);
            URLConnection con = this.url.openConnection();
            String tipo = con.getContentType();
            esArchivo = (tipo != null) ? !tipo.startsWith("text/html") : true;
        } catch (IOException ex) {
            Logger.getLogger(Link.class.getName()).log(Level.SEVERE, null, ex);
        }

        //System.out.println(esArchivo);
    }

    @Override
    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }
        
        if (this.profundidad > this.maxProfundidad) {
            return;
        }
        
        if (!this.esArchivo && this.url.toString().endsWith("/")) {
            File f = new File(folderDescargas + this.url.getFile());
            f.mkdirs();
            //descargarArchivo(folderDescargas,this.url);
            //System.out.println(this.url);
        } else {
            descargarArchivo(folderDescargas, this.url);
        }
        
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                // System.out.println("No se pudo conectar, error: "+connection.getResponseCode());
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("href") || !line.contains("src")) {
                    continue;
                }

                // href
                while (line.contains("href")) {
                    int startHref = line.indexOf("href=\""),
                            startLink = startHref + 6,
                            endLink = line.indexOf('\"', startLink),
                            endHref = endLink + 1;
                    String link = line.substring(startLink, endLink);
                    if (archivosDesc.contains(link)) {
                        break;
                    }
                    if (!link.startsWith("?")) {
                        if (link.startsWith("/")) {
                            // Root link
                            String path = url.getProtocol() + "://" + url.getHost() + link;
                            if (!url.toString().startsWith(path)) {
                                if (this.profundidad < this.maxProfundidad) {
                                    Runnable test = new Link(path, this.profundidad + 1, this.maxProfundidad);
                                    poolHilos.execute(new Thread(test));
                                }
                            }

                        } else {
                            String path = url.toString() + link;
                            if (!url.toString().startsWith(path)) {
                                if (this.profundidad < this.maxProfundidad) {
                                    Runnable test = new Link(path, this.profundidad + 1, this.maxProfundidad);
                                    poolHilos.execute(new Thread(test));
                                }
                            }
                        }

                        //System.out.println("enlace(2): "+link+" profundidad: "+this.profundidad);
                        archivosDesc.add(link);
                        System.out.println("Descargando: "+link);
                        //System.out.println("enlace(2): "+link+" profundidad: "+this.profundidad);
                    }
                    if (startHref >= 0) {
                        line = line.replace(line.substring(startHref, endHref), "");
                    }
                    //System.out.println(line);
                }

                // src
                while (line.contains("src")) {
                    int startHref = line.indexOf("src=\""),
                            startLink = startHref + 5,
                            endLink = line.indexOf('\"', startLink),
                            endHref = endLink + 1;
                    String link = line.substring(startLink, endLink);
                    if (archivosDesc.contains(link)) {
                        break;
                    }

                    // Discard ?C=N;O=D, but it may be improved
                    if (!link.startsWith("?")) {
                        if (link.startsWith("/")) {
                            String path = url.getProtocol() + "://" + url.getHost() + link;
                            if (!url.toString().startsWith(path)) {
                                if (this.profundidad < this.maxProfundidad) {
                                    Runnable test = new Link(path, this.profundidad + 1, this.maxProfundidad);
                                    poolHilos.execute(new Thread(test));
                                }
                            }

                        } else {
                            String path = url.toString() + link;
                            if (!url.toString().startsWith(path)) {
                                if (this.profundidad < this.maxProfundidad) {
                                    Runnable test = new Link(path, this.profundidad + 1, this.maxProfundidad);
                                    poolHilos.execute(new Thread(test));
                                }
                            }
                        }
                        //System.out.println("enlace(3): "+link+" profundidad: "+this.profundidad);
                        archivosDesc.add(link);
                        System.out.println("Descargando: "+link);
                    }
                    if (startHref >= 0) {
                        line = line.replace(line.substring(startHref, endHref), "");
                    }
                    //System.out.println(line);
                }
            }

            connection.disconnect();
        } catch (IOException e) {
            //System.out.println("Hubo un error: "+ e);
        }

    }

    public void descargarArchivo(String raiz, URL link) {
        String test = link.getFile();
        if (!test.contains(".")) {
            test += ".zip";
        }
        File file = new File(raiz + test);
        boolean flag = file.getParentFile().mkdirs();
        try ( BufferedInputStream in = new BufferedInputStream(link.openStream());  FileOutputStream fileOutputStream = new FileOutputStream(file)) {

            byte dataBuffer[] = new byte[65000];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 65000)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException ex) {
            //System.out.println(ex);
        }
    }
}
