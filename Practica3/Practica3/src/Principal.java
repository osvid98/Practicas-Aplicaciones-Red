
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import javax.swing.SwingUtilities;

class Envia extends Thread {

    MulticastSocket socket;
    BufferedReader br;
    String nombreUsuario;
    Principal contexto;

    public Envia(MulticastSocket m, BufferedReader br, String usuario, Principal contexto) {
        this.socket = m;
        this.br = br;
        this.nombreUsuario = usuario;
        this.contexto = contexto;
        
        try {
            //BufferedReader br2 = new BufferedReader(new InputStreamReader(System.in));
            String dir = "231.1.1.1";
            String dir6 = "ff3e::1234:1";
            int pto = 1234;
            InetAddress gpo = InetAddress.getByName(dir);

            String msgInicio = "<inicio>"+nombreUsuario;
            byte[] b = msgInicio.getBytes();
            DatagramPacket p = new DatagramPacket(b, b.length, gpo, pto);
            socket.send(p);
            
        } catch (Exception e) {
            e.printStackTrace();
        }//catch

    }
    
    public void enviarMsg(String mensaje, String destinatario){
        try{
            String dir = "231.1.1.1";
            String dir6 = "ff3e::1234:1";
            int pto = 1234;
            InetAddress gpo = InetAddress.getByName(dir);
            if(destinatario.equals("gpo")){
                mensaje = "<msj>"+"<"+nombreUsuario+">"+mensaje;
            }else{
                mensaje = "<priv><"+nombreUsuario+"><"+destinatario+">"+mensaje;
            }
            byte[] b = mensaje.getBytes();
            DatagramPacket p = new DatagramPacket(b, b.length, gpo, pto);
            socket.send(p);
        } catch(Exception e){
            e.printStackTrace();
        }
        
    }

    public void run() {
        try {
            //BufferedReader br2 = new BufferedReader(new InputStreamReader(System.in));
            String dir = "231.1.1.1";
            String dir6 = "ff3e::1234:1";
            int pto = 1234;
            InetAddress gpo = InetAddress.getByName(dir);

            for (;;) {
                System.out.println("Selecciona una opcion");
                System.out.println("1. Mensaje grupal \n2. Mensaje privado \n3. Salir");
                char opc = br.readLine().charAt(0);
                while(opc!='1'&&opc!='2'&&opc!='3'){
                    System.out.println("Ingresa una opcion valida: ");
                    opc = br.readLine().charAt(0); 
                }
                String mensaje = "";
                if(opc=='2'){
                    String[] participantes = contexto.getParticipantes().toArray(new String[0]);
                    int n = participantes.length;
                    System.out.println("Participantes: ");
                    for(int i=0; i<n; i++){
                        System.out.println((i+1)+". "+participantes[i]);
                    }
                    System.out.println("Selecciona una opcion: ");
                    int participante = 0;
                    try{
                        participante = Integer.parseInt(br.readLine()) - 1;
                        while(participante < 0 || participante >= n){
                            System.out.println("Ingresa una opcion valida: ");
                            participante = Integer.parseInt(br.readLine()) - 1;
                        }
                    }catch(Exception e){
                        System.out.println(e);
                    }
                    System.out.println("Escribe un mensaje para "+participantes[participante]);
                    mensaje = br.readLine();
                    mensaje = "<priv><"+nombreUsuario+"><"+participantes[participante]+">"+mensaje;
                }else if(opc=='1'){
                    System.out.println("Escribe un mensaje para el chat grupal: ");
                    mensaje = br.readLine();
                    mensaje = "<msj>"+"<"+nombreUsuario+">"+mensaje;
                }else if(opc=='3'){
                    mensaje = "<fin>"+nombreUsuario;
                    byte[] b = mensaje.getBytes();
                    DatagramPacket p = new DatagramPacket(b, b.length, gpo, pto);
                    socket.send(p);
                    System.exit(0);
                }
                byte[] b = mensaje.getBytes();
                DatagramPacket p = new DatagramPacket(b, b.length, gpo, pto);
                socket.send(p);
            }//for
        } catch (Exception e) {
            e.printStackTrace();
        }//catch
    }//run
}//class

class Recibe extends Thread {

    MulticastSocket socket;
    String nombreUsuario;
    Principal contexto;

    public Recibe(MulticastSocket m, String n, Principal contexto) {
        this.socket = m;
        this.nombreUsuario = n;
        this.contexto = contexto;
    }

    public void run() {
        try {

            for (;;) {
                DatagramPacket p = new DatagramPacket(new byte[65535], 65535);
                //System.out.println("Listo para recibir mensajes...");
                socket.receive(p);
                // ejemplo de mensaje: <msg><user>Message
                String msj = new String(p.getData(), 0, p.getLength());
                String[]partes = msj.split("[<>]");
                ArrayList<String> partesSinVacias = new ArrayList<>();
                for (String parte : partes) {
                    if (!parte.isEmpty()) {
                        partesSinVacias.add(parte);
                    }
                }
                // Convertir ArrayList a array
                String[] msgs = partesSinVacias.toArray(new String[0]);
                String msgFinal = "";
                
                if(msgs.length==2){ // INICIO
                    if(msgs[0].equals("inicio")){
                        msgFinal = "(info) "+msgs[1]+" ha entrado al chat";
                        contexto.agregarMsg("admin", msgFinal, "gpo");
                        contexto.addParticipante(msgs[1]);
                    }else if(msgs[0].equals("fin")){
                        msgFinal = "(info) "+msgs[1]+" ha salido del chat";
                        contexto.agregarMsg("admin", msgFinal, "gpo");
                        contexto.rmParticipante(msgs[1]);
                    }  
                }else if(msgs.length==3){ // msg grupal
                    msgFinal = msgs[2];
                    contexto.agregarMsg(msgs[1], msgFinal, "gpo");
                }else if(msgs.length==4){ // msg privado
                    //System.out.println(msgs[1]+" a "+msgs[2]+": "+msgs[3]);
                    //System.out.println(nombreUsuario);
                    
                    if(nombreUsuario.equals(msgs[1])){ // yo lo envie 
                        msgFinal = msgs[3];
                        contexto.agregarMsg(nombreUsuario, msgFinal, msgs[2]);
                    }else if(nombreUsuario.equals(msgs[2])){ // es para mi
                        msgFinal = msgs[3];
                        contexto.agregarMsg(msgs[1], msgFinal, msgs[1]);
                    }
                        
                        
                    
                }else if(msgs.length==5){
                    if(msgs[2].equals(nombreUsuario)){
                        String[] participantes = msgs[4].split(",");
                        for(String participante : participantes){
                            if(participante.equals(nombreUsuario)){
                                continue;
                            }
                            contexto.addParticipante(participante);
                        }
                    }
                }else{
                    System.out.println(msgs.length);
                    for(String s : msgs){
                        System.out.print(s+",");
                    }
                    System.out.println("Mensaje recibido: " + msj);
                }
            } //for
        } catch (Exception e) {
            e.printStackTrace();
        }//catch
    }//run
}//class

public class Principal {
    private static ArrayList<String> participantes;
    private static ChatGUI chatGUI;
    
    // Métodos synchronized para garantizar la consistencia en la manipulación de datos compartidos
    public synchronized ArrayList<String> getParticipantes() {
        return participantes;
    }

    public synchronized void addParticipante(String value) {
        participantes.add(value);
        SwingUtilities.invokeLater(() -> chatGUI.addUser(value));
    }
    
    public synchronized void rmParticipante(String value) {
        participantes.remove(value);
        SwingUtilities.invokeLater(() -> chatGUI.removeUser(value));
    }
    
    public synchronized void agregarMsg(String sender, String message, String recipient){
        SwingUtilities.invokeLater(() -> chatGUI.displayMessage(sender, message, recipient));
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
        Principal contexto = new Principal();
        
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
            
            System.out.println("Proporciona un nombre de usuario: ");
            String nombreUsuario = br.readLine();
            // System.out.println("Tu nombre de usuario es "+nombreUsuario);
            chatGUI = new ChatGUI(nombreUsuario);
            chatGUI.addUser("gpo");

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

            Recibe r = new Recibe(m, nombreUsuario, contexto);
            Envia e = new Envia(m, br, nombreUsuario, contexto);
            e.setPriority(10);
            chatGUI.setEnviar(e);
            r.start();
            e.start();
            r.join();
            e.join();
        } catch (Exception e) {
        }
    }//main  
}
