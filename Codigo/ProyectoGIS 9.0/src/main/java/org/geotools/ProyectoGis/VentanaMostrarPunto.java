package org.geotools.ProyectoGis;


import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;

public class VentanaMostrarPunto extends JFrame implements ActionListener {
	private Container contenedor;
	
	private JButton editar;
	
	private JTextField nom;
	private JTextArea panelDescripcion;
	
	private File imagen; //archivo de la imagen
	private String rutaImg, descrip;
	
//	private JTextField textoImagen;
	private JTextField textoDescripcion;
	
	private JLabel panelImagen;
	
	
	public VentanaMostrarPunto(int pos, String nombre, String x, String y, String rutaImagen, String descripcion) {
		
		
		JLabel label=null;
		try {
			label=new JLabel(); // aqui dentro podemos agregar el fondo de la ventana
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(label!=null) {
			setContentPane(label);
		}
		
		setTitle("Punto Nro: "+pos);
		setSize(435,650);
		setLocationRelativeTo(null); //la ventana se abre al medio
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
		contenedor=getContentPane();
		contenedor.setLayout(null);
		
		editar= new JButton();
		editar.setText("Edit");
		editar.setBounds(150, 560, 90, 45);
		editar.setBackground(Color.gray);
		editar.setVerticalTextPosition(SwingConstants.CENTER);
		editar.setHorizontalTextPosition(SwingConstants.CENTER);
		editar.setForeground(Color.black);
		editar.addActionListener(this);
		editar.setOpaque(true);
		editar.setContentAreaFilled(true); //boton no transparente
		editar.setBorderPainted(true);
		
		nom=new JTextField();
		nom.setForeground(Color.BLACK);
		nom.setBounds(115, 2, 60, 30);
		nom.setEditable(false);
		nom.setHorizontalAlignment(JTextField.LEFT);
		nom.setFont(new Font("Tahoma", Font.PLAIN,15));
		nom.setSelectionColor(Color.gray);
		nom.setSelectionColor(Color.black);
		nom.setSelectedTextColor(Color.black);
		nom.setOpaque(true);
		nom.setText(nombre);
		
		panelDescripcion=new JTextArea(10,50); //10 filas 50 columnas
		panelDescripcion.setForeground(Color.black);
		panelDescripcion.setBounds(50, 285, 275, 250);
		panelDescripcion.setEditable(false);
		panelDescripcion.setFont(new Font("Tahoma", Font.PLAIN,12));
		panelDescripcion.setBackground(Color.WHITE);
		panelDescripcion.setSelectionColor(Color.LIGHT_GRAY);
		panelDescripcion.setSelectedTextColor(Color.cyan);
		panelDescripcion.setOpaque(true);
		panelDescripcion.setText(descripcion);
		
		
		textoDescripcion=new JTextField();
		textoDescripcion.setForeground(Color.BLACK);
		textoDescripcion.setBounds(25, 240, 245, 45);
		textoDescripcion.setEditable(false);
		textoDescripcion.setHorizontalAlignment(JTextField.LEFT);
		textoDescripcion.setFont(new Font("Tahoma", Font.PLAIN,14));
		textoDescripcion.setSelectionColor(Color.gray); //cuando pones el puse sale un cartel con lo que escribo
		textoDescripcion.setSelectionColor(Color.black);
		textoDescripcion.setSelectedTextColor(Color.black);
		textoDescripcion.setOpaque(false);
		textoDescripcion.setText("DESCRIPCION");

		panelImagen=new JLabel();
		panelImagen.setBounds(40, 30, 350, 200);
		ImageIcon fot=new ImageIcon(rutaImagen);
		Icon icono=new ImageIcon(fot.getImage().getScaledInstance(panelImagen.getWidth(), panelImagen.getHeight(), Image.SCALE_DEFAULT));
		panelImagen.setIcon(icono);
		this.repaint();
		
		
		
		//AGREGAMOS todo AL CONTENEDOR
		contenedor.add(editar);
		contenedor.add(nom);
		contenedor.add(panelDescripcion);
		contenedor.add(textoDescripcion);
//		contenedor.add(textoImagen);
		contenedor.add(panelImagen);
		

	}
	
	public void actionPerformed(ActionEvent evento) {
		
		if(evento.getSource()==editar) {
			System.out.println("se agregara el boton editar prontamente");
		}
		
	}
	
	
	public void run() {
		editar.setVisible(true);
		editar.setEnabled(true);
		panelImagen.setVisible(true);
		nom.setEditable(false);
		nom.setVisible(true);
		panelDescripcion.setVisible(true);
		textoDescripcion.setVisible(true);
//		textoImagen.setVisible(true);
		
	}
	
	
}
