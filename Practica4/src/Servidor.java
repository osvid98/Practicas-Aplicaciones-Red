import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Servidor {

    static final int MAX_T = 3;
    public static final int PUERTO = 7777;
    private static ServerSocket ss;

    public static void main(String [] args) {
        try {
            ss = new ServerSocket(PUERTO);
            ExecutorService pool = Executors.newFixedThreadPool(MAX_T);
            System.out.println("Iniciando Servidor En El Puerto " + PUERTO);
            for (;;) {
                Socket s = ss.accept();
                Runnable r1 = new PoolThread(s);
                pool.execute(r1);
            }
        } catch (IOException ex) {
        }
    }
}