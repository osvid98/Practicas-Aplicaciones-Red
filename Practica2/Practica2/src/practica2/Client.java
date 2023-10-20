/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package practica2;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.JFileChooser;

public class Client {

    public static void main(String[] args) {
        try {

            // opening the file: 
//            String path = "C:\\Users\\sergi\\Desktop\\escom\\septimo\\redes\\DatagramFiles\\src\\datagramfiles\\INE_Sergio_Demian_Acuayte.pdf";
//            File initialFile = new File(path);
            JFileChooser jf = new JFileChooser(".");
            jf.setMultiSelectionEnabled(false);
            jf.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int res = jf.showOpenDialog(null);
            File initialFile = null;

            if (res == JFileChooser.APPROVE_OPTION) {
                initialFile = jf.getSelectedFile();
            }
            String fileName = initialFile.getName();
            int size = (int) initialFile.length();
            FileInputStream fis = new FileInputStream(initialFile);
            byte[] fBytes = new byte[(int) initialFile.length()];
            fis.read(fBytes);

            // geting the size of each part:
            /*
            DATAGRAM STRUCTUTRE:
            name        260 max 
            size        8
            totalParts  8
            currPart    8
            data        rest -> 65251 sizeOfEachPart
             */
            int sizeOfEachPart = 65248;
            int totalParts = size / sizeOfEachPart + 1;

            // Streams for byte and data
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            // Socket:
            DatagramSocket cl = new DatagramSocket(8888);
            System.out.println("Size : " + size);
            System.out.println("Total parts : " + totalParts);

            for (;;) {
                for (int i = 0; i < totalParts; i++) {
                    // GETTING A COPY OF A PART OF THE BYTE ARRAY:
                    int l = (int) (i * sizeOfEachPart);
                    int r;
                    if (i + 1 == totalParts) {
                        r = size;
                    } else {
                        r = (int) ((i * sizeOfEachPart) + (sizeOfEachPart));
                    }
                    System.out.println("l: " + l);
                    System.out.println("r: " + r);
                    System.out.println(fileName);
                    byte[] btmp;
                    if (i + 1 == totalParts) {
                        System.out.println("r: " + r);
                        btmp = Arrays.copyOfRange(fBytes, (int) (i * sizeOfEachPart), (int) size);
                    } else {
                        btmp = Arrays.copyOfRange(fBytes, (int) (i * sizeOfEachPart), (int) ((i * sizeOfEachPart) + (sizeOfEachPart)));
                    }
                    byte[] fileNameBytes = fileName.getBytes();
                    dos.writeInt(fileNameBytes.length);
                    dos.write(fileNameBytes);
                    dos.writeInt(size);
                    dos.writeInt(totalParts);
                    dos.writeInt(i); //SENDIG i
                    dos.writeInt(l);
                    dos.writeInt(r);
                    System.out.println("Enviando el paquete " + i);
                    dos.write(btmp); //SENDING THE PART 
                    dos.flush(); // SENDING TO THE STREAM dos -> baos

                    byte[] b = baos.toByteArray(); // GETTING THE BYTE OF THE DATAGRAM
                    //System.out.println("Tam del dato: "+b.length); 2 int + 22 -> int = 4 bytes
                    //cl.setBroadcast(true);
                    InetAddress dir = InetAddress.getByName("127.0.0.1");
                    //InetAddress dir = InetAddress.getByName("192.168.10.199");
                    System.out.println("leng of the byte in datagram : " + b.length);
                    DatagramPacket p = new DatagramPacket(b, b.length, dir, 5555); // CREATING THE DATAGRAM
                    cl.send(p); // SENDING THE DATAGRAM THROUGH THE SOCKET
                    System.out.println("mensaje enviado..");
                    baos.reset();
                }
            }//for
//            dos.close();
//            cl.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    
    }
}
