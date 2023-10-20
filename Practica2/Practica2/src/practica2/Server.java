/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package practica2;

import java.io.*;
import java.net.*;
import java.util.*;

class Server {

    public static void main(String[] args) {
        try {
            DatagramSocket s = new DatagramSocket(5555);
            System.out.println("Servidor esperando datagrama..");
            String path = ".\\Servidor\\";

            byte[] totalBytes = null;
            boolean notInit = true;
            boolean[] partsRecived = null;
            for (;;) {
                DatagramPacket p = new DatagramPacket(new byte[65535], 65535);
                s.receive(p);
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(p.getData()));
                int fileNameSize = dis.readInt();
                byte[] fileNameBytes = new byte[fileNameSize];
                dis.read(fileNameBytes);
                String fileName = new String(fileNameBytes);
                int size = dis.readInt();
                int totalParts = dis.readInt();
                int n = dis.readInt();
                int l = dis.readInt();
                int r = dis.readInt();
                int tam = r-l;
//                System.out.println("Total parts: " + totalParts);
//                System.out.println("Current part: " + n);
//                System.out.println("Size of the part: " + tam);
                if (notInit) {
                    totalBytes = new byte[size];
                    System.out.println("byte array created, size: " + size);
                    partsRecived = new boolean[totalParts];
                    notInit = false;
                }
                byte[] b = new byte[tam];
                dis.read(b);
                if (partsRecived[n]) {
                    dis.close();
                    continue;
                }
                partsRecived[n] = true;
                System.out.println("Paquete recibido con los datos: #paquete->" + n + " con " + (r-l) + " bytes");
                if (n + 1 == totalParts) {
                    r = size;
                } else {
                    r = (int) ((n * tam) + (tam));
                }
                System.out.println(l);
                System.out.println(r);
                System.out.println(b.length);
                for (int i = l; i < r; i++) {
                    totalBytes[i] = b[i - l];
                }
                boolean complete = true;
                for (int i = 0; i < totalParts; i++) {
                    complete = complete && partsRecived[i];
                }
                if (complete) {
                    File f1 = new File(path + fileName);
                    OutputStream os = new FileOutputStream(f1);
                    os.write(totalBytes);
                    os.close();
                    return;
                }
                dis.close();
            }//for
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}