package wget;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Wget {

    public static ExecutorService poolHilos;
    public static HashSet<String> archivosDesc = new HashSet<>();
    public static Runnable command;
    public static String folderDescargas;

    public static void main(String[] args) {
        long tiempoInicio = System.currentTimeMillis();
        if (args.length != 3) {
            System.out.println("Por favor, proporciona los parametros correctos: ");
            System.out.println("url noDeProfundidad noDeHilos");
            return;
        }

        String parentURL = args[0];
        int profMax = Integer.parseInt(args[1]);
        int numHilosPool = Integer.parseInt(args[2]);

        //Variables de apoyo
        Path relativePath = Paths.get("");
        folderDescargas = relativePath.toAbsolutePath().toString() + "/descargado";
        File folder = new File(folderDescargas);
        folder.mkdir();
        System.out.println("Folder de descarga: " + folderDescargas);

        System.out.println("URL padre: " + parentURL);
        System.out.println("Numero de hilos: " + numHilosPool);
        poolHilos = Executors.newFixedThreadPool((numHilosPool > 0) ? numHilosPool : 1);
        Thread x = new Thread(new Link(parentURL, 0, profMax));
        x.start();
        //poolHilos.shutdown();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) poolHilos;
        while (!poolHilos.isTerminated()) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(Wget.class.getName()).log(Level.SEVERE, null, ex);
            }
            int hilosActivos = executor.getActiveCount();
            if (hilosActivos == 0) {
                poolHilos.shutdown();
            }
        }

        long tiempoFin = System.currentTimeMillis();
        long tiempoTranscurrido = tiempoFin - tiempoInicio;
        System.out.println("Tiempo transcurrido: " + tiempoTranscurrido + " milisegundos");
    }
}
