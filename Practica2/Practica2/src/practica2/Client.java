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
                
                System.out.println("Enviado paquete datagrama #" + i + " - Size: " + partData.length + " bytes");
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

        System.out.println("Sent packet " + partNumber);
    }
}
