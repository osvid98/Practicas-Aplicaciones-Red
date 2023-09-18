package com.mycompany.practica1;

import static com.mycompany.practica1.Practica1.modelo;
import javax.swing.JFileChooser;
import java.io.*;
import java.net.*;
import javax.swing.JOptionPane;


/* Funciones del cliente que haran las peticiones que se requieran al servidor | n) Son las banders*/
public class Cliente {

    private static int pto = 4444;
    private static String host = "127.0.0.1";
    private static String rutaDirectorios = "";
    public static String sep = System.getProperty("file.separator");
    public static int[] tipoFile;

    // -> SELECCIONAR ARCHIVOS (Para Enviar muchos archivos al servidor)
    public static void SeleccionarArchivos() {
        try {
            JFileChooser jf = new JFileChooser();
            jf.setMultiSelectionEnabled(true);
            jf.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int r = jf.showOpenDialog(null);

            if (r == JFileChooser.APPROVE_OPTION) {
                rutaDirectorios = "";
                File[] files = jf.getSelectedFiles();
                for (File file : files) {
                    String rutaOrigen = file.getAbsolutePath();
                    EnviarArchivo(file, rutaOrigen, file.getName());
                }//for
                Practica1.modelo.clear();
                Actualizar();
            }//if   
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 0) SUBIR ARCHIVO/S | 1) SUBIR DiRECTORIO/S
    public static void EnviarArchivo(File f, String pathOrigen, String pathDestino) {
        try {
            if (f.isFile()) {
                Socket cl = new Socket(host, pto);
                DataOutputStream dos = new DataOutputStream(cl.getOutputStream()); //OutputStream

                String nombre = f.getName();
                long tam = f.length();

                System.out.println("\nSe envia el archivo " + pathOrigen + " con " + tam + " bytes");
                DataInputStream dis = new DataInputStream(new FileInputStream(pathOrigen)); // InputStream

                //La bandera tiene el valor de 0 = Subir archivo
                dos.writeInt(0);
                dos.flush();

                //Se envia info de los archivos
                dos.writeUTF(nombre);
                dos.flush();
                dos.writeLong(tam);
                dos.flush();
                dos.writeUTF(pathDestino);
                dos.flush();

                long enviados = 0;
                int pb = 0;
                int n = 0, porciento = 0;
                byte[] b = new byte[2000];

                while (enviados < tam) {
                    n = dis.read(b);
                    dos.write(b, 0, n);
                    dos.flush();
                    enviados += n;
                    porciento = (int) ((enviados * 100) / tam);
                    System.out.println("\r Enviando el " + porciento + "% --- " + enviados + "/" + tam + " bytes");
                } //while

                JOptionPane.showMessageDialog(null, "Se ha subido el archivo " + nombre + " con tamanio: " + tam);
                dis.close();
                dos.close();
                cl.close();
            } // If
            else {
                Socket cl = new Socket(host, pto);
                DataOutputStream dos = new DataOutputStream(cl.getOutputStream());

                String nombre = f.getName();
                String ruta = f.getAbsolutePath();
                System.out.println("Nombre: " + nombre + " Ruta: " + ruta);

                String aux = rutaDirectorios;
                rutaDirectorios = rutaDirectorios + sep + nombre;

                //La bandera tiene el valor de 1 = Subir Carpeta
                dos.writeInt(1);
                dos.flush();

                //Se envia info de los archivos
                dos.writeUTF(rutaDirectorios);
                dos.flush();

                // Envio los archivos que pertenecen al directorio creado
                File folder = new File(ruta);
                File[] files = folder.listFiles();

                for (File file : files) {
                    String path = rutaDirectorios + sep + file.getName();
                    System.out.println("Ruta destino en el servidor:" + path);
                    EnviarArchivo(file, file.getAbsolutePath(), path);
                }// for

                rutaDirectorios = aux;
                dos.close();
                cl.close();
            } // Else		
        } // try
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 2) DESCARGAR ARCHIVOS 
    public static void RecibirArchivos(String[] nombresArchivos, int tama) {
        try {
            Socket cl = new Socket(host, pto);
            DataOutputStream dos = new DataOutputStream(cl.getOutputStream()); //OutputStream
            DataInputStream dis = new DataInputStream(cl.getInputStream()); // InputStream

            //La bandera tiene el valor de 2 = Descargar seleccion
            dos.writeInt(2);
            dos.flush();

            dos.writeInt(tama);
            dos.flush();

            //Enviamos los indices de los archivos seleccionados
            String aux = "";

            for (int i = 0; i < tama; i++) {
                aux = nombresArchivos[i];
                dos.writeUTF(aux);
                dos.flush();
            }

            String userHome = System.getProperty("user.home");
            String nombre = userHome + "/Downloads/";

            nombre = nombre + dis.readUTF();

            long tam = dis.readLong();
            System.out.println("\nSe recibe el archivo " + nombre + " con " + tam + "bytes");

            DataOutputStream dosArchivo = new DataOutputStream(new FileOutputStream(nombre)); // OutputStream

            long recibidos = 0;
            int n = 0, porciento = 0;
            byte[] b = new byte[2000];

            while (recibidos < tam) {
                n = dis.read(b);
                dosArchivo.write(b, 0, n);
                dosArchivo.flush();
                recibidos += n;
                porciento = (int) ((recibidos * 100) / tam);
                System.out.println("\r Recibiendo el " + porciento + "% --- " + recibidos + "/" + tam + " bytes");
            } // while

            JOptionPane.showMessageDialog(null, "Se ha descargado el archivo " + nombre + " con tamanio: " + tam);
            dos.close();
            dis.close();
            dosArchivo.close();
            cl.close();

        } catch (Exception e) {
            e.printStackTrace();
        }//catch
    }

    // 3) CREAR ARCHIVO
    public static void CrearArchivo() {
        try {
            Socket cl = new Socket(host, pto);
            DataOutputStream dos = new DataOutputStream(cl.getOutputStream()); //OutputStream
            DataInputStream dis = new DataInputStream(cl.getInputStream()); // InputStream

            //La bandera tiene el valor de 3 = Crear archivo
            dos.writeInt(3);
            dos.flush();

            String nombre = JOptionPane.showInputDialog("Ingrese el nombre para el nuevo archivo: ");
            dos.writeUTF(nombre);
            dos.flush();

            JOptionPane.showMessageDialog(null, "Se ha creado el archivo " + nombre);
            modelo.clear();
            Actualizar();

            dos.close();
            dis.close();
            cl.close();

        } catch (Exception e) {
            e.printStackTrace();
        }//catch
    }

    // 4) CREAR DIRECTORIO
    public static void CrearCarpeta() {
        try {
            Socket cl = new Socket(host, pto);
            DataOutputStream dos = new DataOutputStream(cl.getOutputStream()); //OutputStream
            DataInputStream dis = new DataInputStream(cl.getInputStream()); // InputStream

            //La bandera tiene el valor de 4 = Crear Directorio
            dos.writeInt(4);
            dos.flush();

            String nombre = JOptionPane.showInputDialog("Ingrese el nombre para la nueva carpeta: ");
            dos.writeUTF(nombre);
            dos.flush();

            JOptionPane.showMessageDialog(null, "Se ha creado la carpeta " + nombre);
            modelo.clear();
            Actualizar();

            dos.close();
            dis.close();
            cl.close();

        } catch (Exception e) {
            e.printStackTrace();
        }//catch
    }

    // 5) ELIMINAR ARCHIVO
    public static void EliminarArchivo(String[] nombresArchivos, int tama) {
        try {
            Socket cl = new Socket(host, pto);
            DataOutputStream dos = new DataOutputStream(cl.getOutputStream()); //OutputStream
            DataInputStream dis = new DataInputStream(cl.getInputStream()); // InputStream

            //La bandera tiene el valor de 5 = Eliminar Archivo
            dos.writeInt(5);
            dos.flush();

            dos.writeInt(tama);
            dos.flush();

            //Enviamos los indices de los archivos seleccionados
            String aux = "";

            for (int i = 0; i < tama; i++) {
                aux = nombresArchivos[i];
                dos.writeUTF(aux);
                dos.flush();
            }

            String nombre = dis.readUTF();
            long tam = dis.readLong();
            JOptionPane.showMessageDialog(null, "Se ha eliminado el archivo o carptea " + nombre + " con tamanio: " + tam);
            modelo.clear();
            Actualizar();

            dos.close();
            dis.close();
            cl.close();

        } catch (Exception e) {
            e.printStackTrace();
        }//catch
    }

    // 6) RENOMBRAR ARCHIVO
    public static void RenombrarArchivo(String[] nombresArchivos, int tama) {
        try {
            Socket cl = new Socket(host, pto);
            DataOutputStream dos = new DataOutputStream(cl.getOutputStream()); //OutputStream
            DataInputStream dis = new DataInputStream(cl.getInputStream()); // InputStream

            //La bandera tiene el valor de 6 = Renombrar archivo
            dos.writeInt(6);
            dos.flush();

            dos.writeInt(tama);
            dos.flush();

            //Enviamos los nombres de los archivos seleccionados
            String aux = "";

            for (int i = 0; i < tama; i++) {
                aux = nombresArchivos[i];
                dos.writeUTF(aux);
                dos.flush();

                String nuevoNombre = JOptionPane.showInputDialog("Ingrese el nuevo nombre para el archivo " + nombresArchivos[i]);
                dos.writeUTF(nuevoNombre);
                dos.flush();
            }

            JOptionPane.showMessageDialog(null, "Se ha renombrado el archivo.");
            modelo.clear();
            Actualizar();

            dos.close();
            dis.close();
            cl.close();

        } catch (Exception e) {
            e.printStackTrace();
        }//catch
    }

    // 7) ACTUALIZAR (Para actualiza directorio del servidor en la interfaz del cliente)
    public static void Actualizar() {
        try {
            Socket cl = new Socket(host, pto);
            DataOutputStream dos = new DataOutputStream(cl.getOutputStream()); //OutputStream

            //La bandera tiene el valor de 7 = Actualizar 
            dos.writeInt(7);
            dos.flush();

            DataInputStream dis = new DataInputStream(cl.getInputStream()); // InputStream

            int numArchivos = dis.readInt();
            tipoFile = new int[numArchivos];

            for (int i = 0; i < numArchivos; i++) {
                String archivoRecibido = dis.readUTF();
                Practica1.modelo.addElement(archivoRecibido);
                tipoFile[i] = dis.readInt();
            }//for

            dis.close();
            dos.close();
            cl.close();
            System.out.println("Carpeta del cliente actualizada.");

        } catch (Exception e) {
            e.printStackTrace();
        }//catch
    }

    // 8) ABRIR CARPETA (Funcion abrir carpetas del servidor en el cliente)
    public static void AbrirCarpeta(int indice) {
        try {
            Socket cl = new Socket(host, pto);
            DataOutputStream dos = new DataOutputStream(cl.getOutputStream()); //OutputStream  para enviar datos al servidor a través del socket
            //La bandera tiene el valor de 3 = AbrirCarpeta
            dos.writeInt(8); // Enviamos la bandera al servidor para indicarle que queremos abrir una carpeta
            dos.flush();

            //Enviamos el indice en donde se encuentra la carpeta dentro del arreglo de Files[]
            dos.writeInt(indice); // Enviamos el índice de la carpeta que queremos abrir
            dos.flush();

            DataInputStream dis = new DataInputStream(cl.getInputStream()); // Creamos un DataInputStream para recibir datos del servidor a través del socket

            int numArchivos = dis.readInt(); // Leemos el número de archivos que hay en la carpeta
            tipoFile = new int[numArchivos]; // Creamos un arreglo de ints para almacenar el tipo de cada archivo

            for (int i = 0; i < numArchivos; i++) {
                String archivoRecibido = dis.readUTF(); // Leemos el nombre del archivo
                Practica1.modelo.addElement(archivoRecibido); // Agregamos el nombre del archivo al modelo de la lista en el cliente
                tipoFile[i] = dis.readInt(); // Leemos el tipo del archivo y lo almacenamos en el arreglo de tipos
            }//for

            dis.close(); // Cerramos el DataInputStream
            dos.close(); // Cerramos el DataOutputStream
            cl.close(); // Cerramos el socket

        } catch (Exception e) {
            e.printStackTrace();
        }//catch
    }

    // 9) COPIAR ARCHIVO
    public static void CopiarArchivo(String[] nombresArchivos, int tama) {
        try {
            Socket cl = new Socket(host, pto);
            DataOutputStream dos = new DataOutputStream(cl.getOutputStream()); // OutputStream
            DataInputStream dis = new DataInputStream(cl.getInputStream()); // InputStream

            // La bandera tiene el valor de 9 = Copiar archivo
            dos.writeInt(9);
            dos.flush();

            dos.writeInt(tama);
            dos.flush();

            // Enviamos los nombres de los archivos seleccionados
            String aux = "";

            for (int i = 0; i < tama; i++) {
                aux = nombresArchivos[i];
                dos.writeUTF(aux);
                dos.flush();

                String destino = JOptionPane.showInputDialog("Ingrese la ruta de destino para copiar el archivo " + nombresArchivos[i]);
                dos.writeUTF(destino);
                dos.flush();
            }

            JOptionPane.showMessageDialog(null, "Se ha copiado el archivo.");
            modelo.clear();
            Actualizar();

            dos.close();
            dis.close();
            cl.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
