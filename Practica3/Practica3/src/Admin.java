
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

class RecibeAdmin extends Thread {

    MulticastSocket socket;
    Admin contexto;

    public RecibeAdmin(MulticastSocket m, Admin contexto) {
        this.socket = m;
        this.contexto = contexto;
    }

    public void run() {
        try {
            String dir = "231.1.1.1";
            String dir6 = "ff3e::1234:1";
            int pto = 1234;
            InetAddress gpo = InetAddress.getByName(dir);

            for (;;) {
                DatagramPacket p = new DatagramPacket(new byte[65535], 65535);
                //System.out.println("Listo para recibir mensajes...");
                socket.receive(p);
                // ejemplo de mensaje: <msg><user>Message
                String msj = new String(p.getData(), 0, p.getLength());
                System.out.println(msj);
                String[]partes = msj.split("[<>]");
                ArrayList<String> partesSinVacias = new ArrayList<>();
                for (String parte : partes) {
                    if (!parte.isEmpty()) {
                        partesSinVacias.add(parte);
                    }
                }
                // Convertir ArrayList a array
                String[] msgs = partesSinVacias.toArray(new String[0]);
                
                if(msgs.length==2){ // INICIO
                    if(msgs[0].equals("inicio")){
                        //System.out.println("(info) "+msgs[1]+" ha entrado al chat");
                        contexto.addParticipante(msgs[1]);
                        ArrayList<String> participantes = contexto.getParticipantes();
                        String msg = "<priv><admin><"+msgs[1]+"><participantes>";

                        for(String participante : participantes){
                            msg = msg + participante + ",";
                        }
                        // System.out.println("msg: "+msg);
                        byte[] b = msg.getBytes();
                        p = new DatagramPacket(b, b.length, gpo, pto);
                        socket.send(p);
                    }else if(msgs[0].equals("fin")){
                        contexto.rmParticipante(msgs[1]);
                    }
                }
                
            } //for
        } catch (Exception e) {
            e.printStackTrace();
        }//catch
    }//run
}//class

public class Admin {
    private static ArrayList<String> participantes;
    
    // Métodos synchronized para garantizar la consistencia en la manipulación de datos compartidos
    public synchronized ArrayList<String> getParticipantes() {
        return participantes;
    }

    public synchronized void addParticipante(String value) {
        participantes.add(value);
    }
    
    public synchronized void rmParticipante(String value) {
        participantes.remove(value);
    }

    static void despliegaInfoNIC(NetworkInterface netint) throws SocketException {
        System.out.printf("Nombre de despliegue: %s\n", netint.getDisplayName());
        System.out.printf("Nombre: %s\n", netint.getName());
        String multicast = (netint.supportsMulticast()) ? "Soporta multicast" : "No soporta multicast";
        System.out.printf("Multicast: %s\n", multicast);
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            System.out.printf("Direccion: %s\n", inetAddress);
        }
        System.out.printf("\n");
    }

    public static void main(String[] args) {
        participantes = new ArrayList<>();
        Admin contexto = new Admin();
        
        try {
            int pto = 1234, z = 0;
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "ISO-8859-1"));
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets)) {
                System.out.print("[Interfaz " + ++z + "]:");
                despliegaInfoNIC(netint);
            }//for
            System.out.print("\nElige la interfaz multicast:");
            int interfaz = Integer.parseInt(br.readLine());
            //NetworkInterface ni = NetworkInterface.getByName("eth2");
            NetworkInterface ni = NetworkInterface.getByIndex(interfaz);
            //br.close();
            System.out.println("\nElegiste " + ni.getDisplayName());
            
            //System.out.println("Proporciona un nombre de usuario: ");
            //String nombreUsuario = br.readLine();
            // System.out.println("Tu nombre de usuario es "+nombreUsuario);

            MulticastSocket m = new MulticastSocket(pto);
            m.setReuseAddress(true);
            m.setTimeToLive(255);
            String dir = "231.1.1.1";
            String dir6 = "ff3e::1234:1";
            InetAddress gpo = InetAddress.getByName(dir);
            //InetAddress gpo = InetAddress.getByName("ff3e:40:2001::1");
            SocketAddress dirm;
            try {
                dirm = new InetSocketAddress(gpo, pto);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }//catch
            m.joinGroup(dirm, ni);
            System.out.println("Socket unido al grupo " + gpo);

            RecibeAdmin r = new RecibeAdmin(m, contexto);
            //Envia e = new Envia(m, br, nombreUsuario, contexto);
            //e.setPriority(10);
            r.start();
            //e.start();
            //r.join();
            //e.join();
        } catch (Exception e) {
        }
    }//main  
}
