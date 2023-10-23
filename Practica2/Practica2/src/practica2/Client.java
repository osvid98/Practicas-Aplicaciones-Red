import java.io.*;
import java.net.*;
import javax.swing.JFileChooser;

public class Client {

    private static final int DATAGRAM_SIZE = 65248;
    private static final int SERVER_PORT = 5555;

    public static void main(String[] args) {
        try {
            // Seleccionar un archivo para enviar
            File selectedFile = chooseFile();
            if (selectedFile == null) {
                System.out.println("No se selecciono un archivo");
                return;
            }
            
            System.out.println("Archivo Seleccionado: " + selectedFile.getAbsolutePath());

            // Enviar el archivo al servidor
            sendFileToServer(selectedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para elegir un archivo utilizando JFileChooser
    private static File chooseFile() {
        JFileChooser fileChooser = new JFileChooser(".");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }

        return null;
    }

    // Método para enviar un archivo al servidor
    private static void sendFileToServer(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             DatagramSocket clientSocket = new DatagramSocket()) {

            String fileName = file.getName();
            int fileSize = (int) file.length();
            int sizeOfEachPart = DATAGRAM_SIZE;
            int totalParts = (fileSize / sizeOfEachPart) + 1;

            for (int i = 0; i < totalParts; i++) {
                int start = i * sizeOfEachPart;
                int end = Math.min(start + sizeOfEachPart, fileSize);

                byte[] partData = new byte[end - start];
                fis.read(partData);

                // Enviar cada parte al servidor
                sendPartToServer(clientSocket, fileName, fileSize, totalParts, i, start, end, partData);
                
                System.out.println("Enviado paquete datagrama #" + (i+1) + " - Size: " + partData.length + " bytes");
            }
        }
    }

    // Método para enviar una parte del archivo al servidor
    private static void sendPartToServer(DatagramSocket socket, String fileName, int fileSize, int totalParts,
                                         int partNumber, int start, int end, byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(fileName.length());
        dos.write(fileName.getBytes());
        dos.writeInt(fileSize);
        dos.writeInt(totalParts);
        dos.writeInt(partNumber);
        dos.writeInt(start);
        dos.writeInt(end);
        dos.write(data);

        byte[] datagramData = baos.toByteArray();
        InetAddress serverAddress = InetAddress.getByName("127.0.0.1");
        DatagramPacket packet = new DatagramPacket(datagramData, datagramData.length, serverAddress, SERVER_PORT);
        socket.send(packet);

        // Envío del paquete con confirmación
        boolean packetReceived = false;
        int maxRetries = 3;
        int retries = 0;

        while (!packetReceived && retries < maxRetries) {
            try {
                socket.send(packet);
                System.out.println("Tratando de enviar paquete " + (partNumber+1));

                // Esperar la confirmación del servidor
                socket.setSoTimeout(5000);  // Esperar 5 segundos para la confirmación
                byte[] confirmationData = new byte[DATAGRAM_SIZE];
                DatagramPacket confirmationPacket = new DatagramPacket(confirmationData, DATAGRAM_SIZE);
                socket.receive(confirmationPacket);

                // Comprobar si la confirmación es para el paquete actual
                DataInputStream confirmationStream = new DataInputStream(new ByteArrayInputStream(confirmationData));
                int confirmedPartNumber = confirmationStream.readInt();
                if (confirmedPartNumber == partNumber) {
                    packetReceived = true;
                }
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout waiting for confirmation, retrying...");
                retries++;
            }
        }

        if (!packetReceived) {
            System.err.println("Fallo la confirmacion de paquete " + partNumber);
            // Aquí puedes decidir cómo manejar el paquete no confirmado, como reenviarlo nuevamente.
        }
    }
}
