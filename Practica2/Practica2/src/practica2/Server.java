import java.io.*;
import java.net.*;

public class Server {

    private static final int DATAGRAM_SIZE = 65535;
    private static final int SERVER_PORT = 5555;

    public static void main(String[] args) {
        try {
            DatagramSocket serverSocket = new DatagramSocket(SERVER_PORT);
            System.out.println("Server esperando por paquetes de datagramas...");
            receiveAndSaveFile(serverSocket, ".\\Servidor\\");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para recibir y guardar el archivo enviado por el cliente
    private static void receiveAndSaveFile(DatagramSocket socket, String savePath) throws IOException {
        byte[] totalBytes = null;
        boolean[] partsReceived = null;

        while (true) {
            DatagramPacket packet = new DatagramPacket(new byte[DATAGRAM_SIZE], DATAGRAM_SIZE);
            socket.receive(packet);

            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.getData()));

            int fileNameSize = dis.readInt();
            byte[] fileNameBytes = new byte[fileNameSize];
            dis.read(fileNameBytes);
            String fileName = new String(fileNameBytes);

            int fileSize = dis.readInt();
            int totalParts = dis.readInt();
            int partNumber = dis.readInt();
            int start = dis.readInt();
            int end = dis.readInt();
            int partSize = end - start;

            if (totalBytes == null) {
                totalBytes = new byte[fileSize];
                partsReceived = new boolean[totalParts];
                System.out.println("Byte array created, size: " + fileSize);
            }

            if (partsReceived[partNumber]) {
                dis.close();
                sendConfirmation(socket, packet.getAddress(), packet.getPort(), partNumber); // Envía confirmación si el paquete se recibió previamente
                continue;
            }

            partsReceived[partNumber] = true;

            byte[] data = new byte[partSize];
            dis.read(data);

            System.out.println("Se recibio el paquete de datagrama: #" + (partNumber+1) + " con " + partSize + " bytes");

            for (int i = start; i < end; i++) {
                totalBytes[i] = data[i - start];
            }

            sendConfirmation(socket, packet.getAddress(), packet.getPort(), partNumber); // Envía confirmación del paquete recibido

            boolean complete = true;
            for (boolean received : partsReceived) {
                complete = complete && received;
            }

            if (complete) {
                File file = new File(savePath + fileName);
                try (OutputStream os = new FileOutputStream(file)) {
                    os.write(totalBytes);
                }

                return;
            }
        }
    }

    // Método para enviar una confirmación al cliente
    private static void sendConfirmation(DatagramSocket socket, InetAddress clientAddress, int clientPort, int partNumber)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(partNumber);

        byte[] confirmationData = baos.toByteArray();
        DatagramPacket confirmationPacket = new DatagramPacket(confirmationData, confirmationData.length, clientAddress, clientPort);
        socket.send(confirmationPacket);
    }
}
