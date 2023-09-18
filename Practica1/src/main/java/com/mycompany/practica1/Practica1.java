package com.mycompany.practica1;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;


public class Practica1 extends JFrame implements ActionListener {

    JButton BtnSubir, BtnDescargar, BtnEliminar,BtnRenombrar,BtnCrearArch,BtnCrearCarp,BtnActualizar;
    static JList<String> archivos;
    static DefaultListModel<String> modelo;
    MouseListener mouseListener;
    JPanel panelBotones;
    //static JProgressBar BarraProgreso;
    JScrollPane scroll;
    //File list[];

    public Practica1() {
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.setBackground(new Color(238, 238, 238));
        
        // Crear un nuevo JPanel para los botones en la parte superior (norte)
        JPanel panelBotonesSuperior = new JPanel();
        panelBotonesSuperior.setBackground(Color.WHITE); // Establecer el fondo verde
        panelBotonesSuperior.setLayout(new GridLayout(1, 7, 10, 10)); // Una fila de botones
        panelBotonesSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        archivos = new JList<String>();
        archivos.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        archivos.setBackground(Color.WHITE);
        archivos.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        archivos.setForeground(Color.DARK_GRAY);
        archivos.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1)
        ));
        /*Añadimos la funcionalidad de doble clic para navegar por directorios*/
        mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {

                if (e.getClickCount() == 2) {
                    int index = archivos.locationToIndex(e.getPoint());
                    String nombreSeleccion = modelo.getElementAt(index);

                    //Revisamos que la seleccion sea un directorio
                    if (Cliente.tipoFile[index] == 1) {
                        modelo.clear();
                        Cliente.AbrirCarpeta(index);
                    }
                }

            }
        };
        archivos.addMouseListener(mouseListener);

        modelo = new DefaultListModel<>();
        Cliente.Actualizar();

        archivos.setModel(modelo);

        scroll = new JScrollPane(archivos);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        c.add(scroll, BorderLayout.CENTER);

        panelBotones = new JPanel();
        panelBotones.setBackground(new Color(238, 238, 238));
        panelBotonesSuperior.setLayout(new GridLayout(0, 2, 10, 10)); // 2 columnas y filas automáticas
        panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        BtnSubir = new JButton("Subir");
        BtnSubir.setBackground(Color.RED);
        BtnSubir.setForeground(Color.WHITE);
        BtnDescargar = new JButton("Descargar");
        BtnDescargar.setBackground(Color.GREEN);
        BtnDescargar.setForeground(Color.WHITE);
        BtnEliminar = new JButton("Eliminar");
        BtnEliminar.setBackground(new Color(66, 133, 244));
        BtnEliminar.setForeground(Color.WHITE);
        BtnRenombrar = new JButton("Renombrar");
        BtnRenombrar.setBackground(new Color(66, 133, 244));
        BtnRenombrar.setForeground(Color.WHITE);
        BtnCrearArch = new JButton("Crear Archivo");
        BtnCrearArch.setBackground(new Color(66, 133, 244));
        BtnCrearArch.setForeground(Color.WHITE);
        BtnCrearCarp = new JButton("Crear carpeta");
        BtnCrearCarp.setBackground(new Color(66, 133, 244));
        BtnCrearCarp.setForeground(Color.WHITE);
        BtnActualizar = new JButton("<- Regresar");
        BtnActualizar.setBackground(Color.BLACK);
        BtnActualizar.setForeground(Color.WHITE);
        BtnActualizar.setPreferredSize(new Dimension(1, 40));
        
        // Agregar los botones al panel en la parte superior (norte)
        panelBotonesSuperior.add(BtnSubir);
        panelBotonesSuperior.add(BtnDescargar);
        panelBotonesSuperior.add(BtnCrearArch);
        panelBotonesSuperior.add(BtnCrearCarp);
        panelBotonesSuperior.add(BtnEliminar);
        panelBotonesSuperior.add(BtnRenombrar);
        panelBotonesSuperior.add(BtnActualizar);

        // Agregar el panel de botones en la parte superior (norte) del BorderLayout
        c.add(panelBotonesSuperior, BorderLayout.NORTH);

        BtnSubir.addActionListener(this);
        BtnActualizar.addActionListener(this);
        BtnDescargar.addActionListener(this);
        BtnEliminar.addActionListener(this);
        BtnRenombrar.addActionListener(this);
        BtnCrearArch.addActionListener(this);
        BtnCrearCarp.addActionListener(this);
        
    }
public void actionPerformed(ActionEvent e) {
    JButton b = (JButton) e.getSource();

    if (b == BtnSubir) {
        Cliente.SeleccionarArchivos();
    }

    if (b == BtnActualizar) {
        modelo.clear();
        Cliente.Actualizar();
    }

    if (b == BtnDescargar) {
        if (!archivos.isSelectionEmpty()) {
            int[] indices = archivos.getSelectedIndices();
            String[] nombreSeleccion = new String[indices.length];

            for (int i = 0; i < indices.length; i++) {
                System.out.println("El indice es: " + indices[i]);
                nombreSeleccion[i] = modelo.getElementAt(indices[i]);
                System.out.println("Nombre: " + nombreSeleccion[i]);
            }

            Cliente.RecibirArchivos(nombreSeleccion, indices.length);
        } else {
            JOptionPane.showMessageDialog(null, "Seleccione archivos para descargarlos.");
        }
    }

    if(b == BtnEliminar){
        if (!archivos.isSelectionEmpty()) {
            int[] indices = archivos.getSelectedIndices();
            String[] nombreSeleccion = new String[indices.length];

            for (int i = 0; i < indices.length; i++) {
                System.out.println("El indice es: " + indices[i]);
                nombreSeleccion[i] = modelo.getElementAt(indices[i]);
                System.out.println("Nombre: " + nombreSeleccion[i]);
            }

            Cliente.EliminarArchivo(nombreSeleccion, indices.length);

        } else {
            JOptionPane.showMessageDialog(null, "Seleccione archivos para eliminarlos.");
        }
    }
    if (b == BtnRenombrar){
        
        if (!archivos.isSelectionEmpty()) {
            int[] indices = archivos.getSelectedIndices();
            String[] nombreSeleccion = new String[indices.length];

            for (int i = 0; i < indices.length; i++) {
                System.out.println("El indice es: " + indices[i]);
                nombreSeleccion[i] = modelo.getElementAt(indices[i]);
                System.out.println("Nombre: " + nombreSeleccion[i]);
            }

            Cliente.RenombrarArchivo(nombreSeleccion, indices.length);

        } else {
            JOptionPane.showMessageDialog(null, "Seleccione archivos para renombrar.");
        }
    }
    if(b == BtnCrearArch){
        Cliente.CrearArchivo();
    }
    if(b == BtnCrearCarp){
        Cliente.CrearCarpeta();
    }
}


    public static void main(String s[]) {
        Practica1 f = new Practica1();
        f.setTitle("Practica 1 ");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(600, 500);
        f.setVisible(true);
        f.setLocationRelativeTo(null);
    }
}