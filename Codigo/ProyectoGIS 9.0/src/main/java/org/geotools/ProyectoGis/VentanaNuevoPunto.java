package org.geotools.ProyectoGis;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

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

public class VentanaNuevoPunto extends JFrame implements ActionListener {
	private Container contenedor;
	
	private JButton buscar, guardar, cerrar;
	private JButton msjError; //para cuando no elija una imagen
	
	private JTextField direccion;
	private JTextArea panelDescripcion;
	private JFileChooser fileChooser;
	
	private File imagen; //archivo de la imagen
	private String rutaImagen, descripcion;
	
	private JTextField textoImagen;
	private JTextField textoDescripcion;
	
	String nombrePunto;
	double x;
	double y;
	int puntosAgregados;
	String rutaTxt;
	
	public VentanaNuevoPunto(String nombre, double x, double y, int puntosAgregados, String rutaDirecciones) {
		
		nombrePunto=nombre;
		this.x=x;
		this.y=y;
		this.puntosAgregados=puntosAgregados;
		this.descripcion="";
		this.rutaTxt=rutaDirecciones;
		
		JLabel label=null;
		try {
			label=new JLabel(); // aqui dentro podemos agregar el fondo de la ventana
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(label!=null) {
			setContentPane(label);
		}
		
		setTitle("Agregar Nuevo Punto");
		setSize(435,600);
		setLocationRelativeTo(null); //la ventana se abre al medio
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		fileChooser=new JFileChooser();
		JFileChooser fileChooser = new JFileChooser();; 
        FileNameExtensionFilter filtro = new FileNameExtensionFilter(".jpg & .gif", "jpg", "gif"); 
        fileChooser.setFileFilter(filtro); 
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY); 
        
		contenedor=getContentPane();
		contenedor.setLayout(null);
		
		buscar= new JButton();
		buscar.setText("Buscar");
		buscar.setBounds(10, 50, 90, 45);
		buscar.setBackground(Color.gray);
		buscar.setVerticalTextPosition(SwingConstants.CENTER);
		buscar.setHorizontalTextPosition(SwingConstants.CENTER);
		buscar.setForeground(Color.black);
		buscar.addActionListener(this);
		buscar.setOpaque(true);
		buscar.setContentAreaFilled(true); //boton no transparente
		buscar.setBorderPainted(true);
		
		direccion=new JTextField();
		direccion.setForeground(Color.BLACK);
		direccion.setBounds(105, 60, 275, 25);
		direccion.setEditable(true);
		direccion.setHorizontalAlignment(JTextField.LEFT);
		direccion.setFont(new Font("Tahoma", Font.PLAIN,12));
		direccion.setBackground(Color.white);
		direccion.setSelectionColor(Color.gray);
		direccion.setToolTipText("Ingrese la Direccion"); //cuando pones el puse sale un cartel con lo que escribo
		direccion.setSelectionColor(Color.black);
		direccion.setSelectedTextColor(Color.black);
		direccion.setOpaque(true);
		
		panelDescripcion=new JTextArea(10,50); //10 filas 50 columnas
		panelDescripcion.setForeground(Color.black);
		panelDescripcion.setBounds(25, 150, 275, 300);
		panelDescripcion.setEditable(true);
		panelDescripcion.setFont(new Font("Tahoma", Font.PLAIN,12));
		panelDescripcion.setBackground(Color.WHITE);
		panelDescripcion.setSelectionColor(Color.LIGHT_GRAY);
		panelDescripcion.setToolTipText("Ingrese la descripcion");
		panelDescripcion.setSelectedTextColor(Color.cyan);
		panelDescripcion.setOpaque(true);
		
		guardar=new JButton();
		guardar.setText("Guardar");
		guardar.setBounds(100, 500, 90, 45);
		guardar.setBackground(Color.gray);
		guardar.setVerticalTextPosition(SwingConstants.CENTER);
		guardar.setHorizontalTextPosition(SwingConstants.CENTER);
		guardar.addActionListener(this);
		guardar.setOpaque(true);
		guardar.setForeground(Color.black);
		guardar.setContentAreaFilled(true);
		guardar.setBorderPainted(true);
		
		msjError=new JButton();
		msjError.setBounds(30,280,410,30);
		msjError.setVerticalTextPosition(SwingConstants.CENTER);
		msjError.setHorizontalTextPosition(SwingConstants.CENTER);
		msjError.setOpaque(false);
		msjError.setContentAreaFilled(false);
		
		textoImagen=new JTextField();
		textoImagen.setForeground(Color.BLACK);
		textoImagen.setBounds(10, 10, 130, 45);
		textoImagen.setEditable(false);
		textoImagen.setHorizontalAlignment(JTextField.LEFT);
		textoImagen.setFont(new Font("Tahoma", Font.PLAIN,14));
		textoImagen.setSelectionColor(Color.gray); //cuando pones el puse sale un cartel con lo que escribo
		textoImagen.setSelectionColor(Color.black);
		textoImagen.setSelectedTextColor(Color.black);
		textoImagen.setOpaque(false);
		textoImagen.setText("ELEGIR IMAGEN");
		
		
		textoDescripcion=new JTextField();
		textoDescripcion.setForeground(Color.BLACK);
		textoDescripcion.setBounds(25, 110, 100, 45);
		textoDescripcion.setEditable(false);
		textoDescripcion.setHorizontalAlignment(JTextField.LEFT);
		textoDescripcion.setFont(new Font("Tahoma", Font.PLAIN,14));
		textoDescripcion.setSelectionColor(Color.gray); //cuando pones el puse sale un cartel con lo que escribo
		textoDescripcion.setSelectionColor(Color.black);
		textoDescripcion.setSelectedTextColor(Color.black);
		textoDescripcion.setOpaque(false);
		textoDescripcion.setText("DESCRIPCION");
		
		//AGREGAMOS todo AL CONTENEDOR
		contenedor.add(buscar);
		contenedor.add(guardar);
		//contenedor.add(msjError);
		contenedor.add(direccion);
		contenedor.add(panelDescripcion);
		contenedor.add(textoDescripcion);
		contenedor.add(textoImagen);
				
	}
	
	public void actionPerformed(ActionEvent evento) {
		
		if(evento.getSource()==buscar) {
	        int result = fileChooser.showOpenDialog(this); 
	        if (result == JFileChooser.APPROVE_OPTION){ 
	            imagen= fileChooser.getSelectedFile();
				rutaImagen=imagen.getPath();
				direccion.setText(rutaImagen);
				msjError.setText("");
	        }
	        else {
	        	System.out.println("Error?????");
	        }
		}
		if(evento.getSource()==guardar) {
			guardarCoordenadas();
		}
		
	}
	
	public void guardarCoordenadas() {
		//se guardara en el archivo de texto separado los atributos entre si por ";"
		//NUMERO;NOMBRE;X;Y;ICONO;IMAGEN;DESCRIPCION
		//Separados entre si por un salto de linea "\r\n"
	    try {
	        File file = new File(rutaTxt);
	        // Si el archivo no existe es creado
	        if (!file.exists()) {
	            file.createNewFile();
	        }
	        FileReader fr = new FileReader(file);
	        BufferedReader lector = new BufferedReader(fr);
	        String contenido="";
	        String linea;
	        while((linea = lector.readLine())!=null) { //vamos leyendo todo el contenido del txt por si ya habia ubicaciones agregadas
//	        	System.out.println(contenido);
	            contenido=contenido+linea+"\r\n";
	        }
	        descripcion=panelDescripcion.getText();
	        //contenido=contenido+puntosAgregados+";"+"nombre:"+nombrePunto+";x:"+String.valueOf(x)+";y:"+String.valueOf(y)+";imagen:"+rutaImagen+";Descripcion:"+descripcion+"\r\n";
	        contenido=contenido+puntosAgregados+";"+nombrePunto+";"+String.valueOf(x)+";"+String.valueOf(y)+";"+rutaImagen+";"+descripcion+"\r\n";
	        FileWriter fw = new FileWriter(file);
	        BufferedWriter bw = new BufferedWriter(fw);
	        bw.write(contenido);
	        bw.close();
	        this.hide();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		
	}
	
	public void run() {
		buscar.setVisible(true);
		buscar.setEnabled(true);
		guardar.setVisible(true);
		guardar.setEnabled(true);
		direccion.setEditable(true);
		textoDescripcion.setVisible(true);
		textoImagen.setVisible(true);
		
	}
	
	
}
