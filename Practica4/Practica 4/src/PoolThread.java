import java.net.Socket;

public class PoolThread implements Runnable {
  Socket s;

  public PoolThread(Socket s){
    this.s = s;
  }

  public void run() {
    new Peticion(s).start();
  }
}
