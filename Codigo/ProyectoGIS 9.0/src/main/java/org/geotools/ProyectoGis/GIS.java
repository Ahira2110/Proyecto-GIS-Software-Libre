package org.geotools.ProyectoGis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
//estos 3 para crear el txt con la ubicacion donde el usuario dio click



import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import org.geotools.data.DataUtilities;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.Rule;
import org.geotools.styling.SLD;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.MapPane;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.tool.CursorTool;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;

import com.vividsolutions.jts.geom.GeometryFactory;

/**
 *  en el siguiente ejemplo vamos a agregar una herramienta de seleccion que nos permita ahcer clic sobre el mapa y pintar la geometria de amarillo
 */
public class GIS {

    /*
     * Objetos encesarios para crear el estilo y los filtros
     */
    private StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

    /*
     * constantes con los tipos de geometrias de un shapefile
     */
    private enum GeomType {
        POINT,
        LINE,
        POLYGON
    };

    /*
     * algunas variables de estilos predeterminadas
     */
    private static final Color LINE_COLOUR = Color.BLUE;
    private static final Color FILL_COLOUR = Color.CYAN;
    private static final Color SELECTED_COLOUR = Color.YELLOW;
    private static final float OPACITY = 1.0f;
    private static final float LINE_WIDTH = 1.0f;
    private static final float POINT_SIZE = 10.0f;

    private JMapFrame mapFrame;
    private SimpleFeatureSource featureSource;

    private String geometryAttributeName;
    private GeomType geometryType;
    
    private int puntosAgregados;
    private static boolean conTxt;
    
    /*
     * Main
     */
    public static void main(String[] args) throws Exception {
        GIS me = new GIS();
        
        
        String ruta=JFileDataStoreChooser.showOpenFile("shp", null).getPath();
        File file = new File(ruta); //creamos un StoreChooser para elegir el archivo .shp
        if (file == null) {
            return;
        }
        String nomFile= file.getName();
        System.out.println("ruta: "+ruta+ "    nombre archivo:"+nomFile);
        String rutaDirecciones="";
        for(int i=0;i<ruta.length()-nomFile.length(); i++) {
        	rutaDirecciones=rutaDirecciones+ruta.charAt(i);
        }
        rutaDirecciones=rutaDirecciones+"direcciones.txt";
        File f=new File(rutaDirecciones); //veremos si el archivo existe en la misma ruta del shpFile
        if(!f.exists()) {
        	f.createNewFile(); //si no existe crea uno nuevo para guardar lo que pongamos
        	conTxt=false;
        }
        else {
        	conTxt=true;
        }
        me.displayShapefile(file,rutaDirecciones); //mostramos el shapefile
        
    }

/**
 * Este metodo se conecta con el shapefile; recupera la informacion de sus caracteristicas (geometria, tipos de coordenadas, etc
 * crea un mapFrame para mostrar el shapefile y crea una herramienta de seleccion a la barra de erra mientas del mapframe 
 */
public void displayShapefile(File file, String rutaDirecciones) throws Exception {
    FileDataStore store = FileDataStoreFinder.getDataStore(file);
    featureSource = store.getFeatureSource();
    setGeometry(); //definimos la geometria
    
    puntosAgregados=0; //inicializamos el contador de puntos servira para tener un numero unico para cada controlar cada punto
    //en el archivo de texto
    

    /*
     * Crea el jmapFrame y lo configura para mostrar el shapefile con estilo por defecto
     */
    MapContent map = new MapContent();
    map.setTitle("Mapa");
    Style style = createDefaultStyle();
    Layer layer = new FeatureLayer(featureSource, style);
    map.addLayer(layer);
    mapFrame = new JMapFrame(map);
    mapFrame.enableToolBar(true);
    mapFrame.enableStatusBar(true);
    
    
    /**
     * Agregamos los atributos necesarios para crear los botones que permita dibujar un punto en el mapa y  otro para seleccionar la figura
     * geometrica mas cercana a donde el usuario haga click
     */
    JToolBar toolBarDibujo = mapFrame.getToolBar();
    JButton botonDibujo = new JButton("Draw");
    toolBarDibujo.addSeparator();
    toolBarDibujo.add(botonDibujo);

    botonDibujo.addActionListener(e -> mapFrame.getMapPane().setCursorTool(new CursorTool() { //accion al seleccionar el boton "draw"
        @Override
        public void onMouseClicked(MapMouseEvent ev) {
        	int respuesta= JOptionPane.showConfirmDialog(null, "¿Desea guardar este punto?", "Confirmar",JOptionPane.YES_NO_OPTION);
        	if(respuesta==0) { //si responde que desea guardar
        		String texto=JOptionPane.showInputDialog(null, "Nombre del lugar");
        		DirectPosition2D p = ev.getWorldPos();
                //System.out.println(p.getX() + " -- " + p.getY());
                puntosAgregados++;
                drawMyPoint(p.getX(), p.getY(), map, texto, rutaDirecciones);
        	}
        	else{
        		System.out.println("Punto no guardado");
        	}
            
        }
    }));
    
    JToolBar toolBarMostrar = mapFrame.getToolBar();
    JButton botonMostrar = new JButton("Mostrar");
    toolBarMostrar.addSeparator();
    toolBarMostrar.add(botonMostrar);

    botonMostrar.addActionListener(e -> mapFrame.getMapPane().setCursorTool(new CursorTool() { //accion al seleccionar el boton "draw"
        @Override
        public void onMouseClicked(MapMouseEvent ev) {
        	DirectPosition2D p = ev.getWorldPos();
        	String punto=encontrarPunto(p.getX(), p.getY(), rutaDirecciones);
        	mostrar(punto);            
        }
    }));
    
    
   // JRadioButton() iconos=new JRadioButton(); //probar con esto lina de codigo 174 de lo de Jorge
    
    if(conTxt==true) { //tiene un archivo con los marcadores asociados
    	//primero procederemos a marcar el mapa
    	
    	boolean vacio=true;
    	try {
	        File fileD = new File(rutaDirecciones);
	        // Se supone q si no existia al abrir ya lo creo ahora leeremos si tiene algun punto
	        FileReader fr = new FileReader(fileD);
	        BufferedReader lector = new BufferedReader(fr);
	        String linea;

	    	double cordX, cordY;
	        while((linea = lector.readLine())!=null) { //vamos leyendo punto por punto y buscando su coordenada para ver si el usuario clikeo cerca de uno
		        String textX="";
		        String textY="";
	        	vacio=false;
	        	int k=0;
	        	for(int i=0;i<linea.length();i++) {
	        		if(linea.charAt(i)==';') {
	        			k++;
	            	}else {
	            		if(k==2) { //si es k=2 entonces esta leyendo coordenada x
	            	    System.out.print(linea.charAt(i));
	        			textX=textX+linea.charAt(i);
	            		}
	            		if(k==3) { //si es k=3 esta leyendo coordenada y
	            			textY=textY+linea.charAt(i);
	            		}
	            		if(k==4) {
	            			i=linea.length(); //ya no necesitamos seguir recorriendo
	            		}
	            	}
	        		
	        	}

	        	System.out.println("textX: "+textX);
	        	System.out.println("textY: "+textY);
	        	cordX= Double.parseDouble(textX);
	        	cordY= Double.parseDouble(textY);
	        	soloDibujarPunto(cordX, cordY, map);
	        }
	        if(vacio==true) {
	        	System.out.println("mapa sin marcadores asociados");
	        }
	        lector.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
    }
    
    
    mapFrame.setSize(600, 600);
    mapFrame.setVisible(true);
}




/**
 * Metodo que define como se va a dibujar el punto
 * primerio creara un punto geometrico con las coordenadas de donde se clikeo y luego guardara dicha posicion en un archivo de texto
 */

void drawMyPoint(double x, double y, MapContent map, String nombre, String rutaDirecciones) {
	SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
    builder.setName("MyFeatureType");                                     // 
    builder.setCRS( DefaultGeographicCRS.WGS84 ); // set crs              // Definimos las caracteristicas de tipo del punto q vamos a crear
    builder.add("location", Point.class); // add geometry                 //
    org.locationtech.jts.geom.GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();  //objeto q usaremos para crear el punto
    Coordinate coord = new Coordinate( x,y );  //creamos la coordenada con los puntos pasados como parametros
    org.locationtech.jts.geom.Point point = geometryFactory.createPoint(coord); // Creamos el punto geometrico

SimpleFeatureType pointtype = null; //definimos el typo de punto que necesitamos apra agregarlo a la capa del mapa
   try {
       pointtype = DataUtilities.createType("POINT", "geom:Point,name:String");
   } catch (SchemaException ex) {
       Logger.getLogger(GIS.class.getName()).log(Level.SEVERE, null, ex);
   }
SimpleFeatureBuilder featureBuilderPoints = new SimpleFeatureBuilder(pointtype); //creamos el constructor de lo que agregaremos
DefaultFeatureCollection collectionPoints = new DefaultFeatureCollection("internal", pointtype);  //creamos el contenedor donde pondremos lo que agregaremos
featureBuilderPoints.add(point);  //agregamos el punto en el constructor
SimpleFeature featureLine = featureBuilderPoints.buildFeature(null);  
((DefaultFeatureCollection)collectionPoints).add(featureLine);     
Style PointStyle = SLD.createPointStyle("circle", Color.RED, Color.RED,(float) 0.5,(float) 10);  //definimos el estilo del punto (icono, color contorno, color, opacidad y tamaño)
//Obserervacion: el dibujo del punto siempre se mantiene del mismo tamaño por mas que se use el zoom
map.addLayer(new FeatureLayer(collectionPoints, PointStyle)); //agregamos el punto a la capa

//guardarCoordenadas(nombre,x,y);
VentanaNuevoPunto ventana=new VentanaNuevoPunto(nombre,x,y,puntosAgregados, rutaDirecciones);
ventana.setVisible(true);
ventana.run();
}

public void soloDibujarPunto(double x, double y, MapContent map) {
	SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
    builder.setName("MyFeatureType");                                     // 
    builder.setCRS( DefaultGeographicCRS.WGS84 ); // set crs              // Definimos las caracteristicas de tipo del punto q vamos a crear
    builder.add("location", Point.class); // add geometry                 //
    org.locationtech.jts.geom.GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();  //objeto q usaremos para crear el punto
    Coordinate coord = new Coordinate( x,y );  //creamos la coordenada con los puntos pasados como parametros
    org.locationtech.jts.geom.Point point = geometryFactory.createPoint(coord); // Creamos el punto geometrico

SimpleFeatureType pointtype = null; //definimos el typo de punto que necesitamos apra agregarlo a la capa del mapa
   try {
       pointtype = DataUtilities.createType("POINT", "geom:Point,name:String");
   } catch (SchemaException ex) {
       Logger.getLogger(GIS.class.getName()).log(Level.SEVERE, null, ex);
   }
SimpleFeatureBuilder featureBuilderPoints = new SimpleFeatureBuilder(pointtype); //creamos el constructor de lo que agregaremos
DefaultFeatureCollection collectionPoints = new DefaultFeatureCollection("internal", pointtype);  //creamos el contenedor donde pondremos lo que agregaremos
featureBuilderPoints.add(point);  //agregamos el punto en el constructor
SimpleFeature featureLine = featureBuilderPoints.buildFeature(null);  
((DefaultFeatureCollection)collectionPoints).add(featureLine);     
Style PointStyle = SLD.createPointStyle("circle", Color.RED, Color.RED,(float) 0.5,(float) 10);  //definimos el estilo del punto (icono, color contorno, color, opacidad y tamaño)
//Obserervacion: el dibujo del punto siempre se mantiene del mismo tamaño por mas que se use el zoom
map.addLayer(new FeatureLayer(collectionPoints, PointStyle)); //agregamos el punto a la capa
}

public void mostrar(String punto) {
	int pos=0;
	String nombre="";
	String x="";
	String y="";
	String rutaImg="";
	String descripcion="";
	int k=0;
	for(int i=0;i<punto.length();i++) {
		if(punto.charAt(i)==';') {
			k++;
		}else {
			if(k==0) { //esta leyendo el numero
			pos=Integer.parseInt(Character.toString(punto.charAt(i)));
			}
			if(k==1) { //esta leyendo el nombre
				nombre=nombre+punto.charAt(i);
			}
			if(k==2) { //esta leyendo X
				x=x+punto.charAt(i);
			}
			if(k==3) {//esta leyendo Y
				x=x+punto.charAt(i);
			}
			if(k==4) {// esta leyendo ruta de la imagen
				rutaImg=rutaImg+punto.charAt(i);
			}
			if(k==5) { //esta leyendo la descripcion
				descripcion=descripcion+punto.charAt(i);
			}
		}
	}
	
	VentanaMostrarPunto ventana=new VentanaMostrarPunto(pos, nombre, x, y, rutaImg, descripcion);
	ventana.setVisible(true);
	ventana.run();
	
}


public String encontrarPunto(double x, double y, String rutaDirecciones) {
	String seEncontro="";
	int pos=comparar(x,y, rutaDirecciones);
	if(pos!=-1){ //si es -1 no se encontro entonces al leer una cadena vacia no seguimos ya q clikeo muy lejos
		try {
	        File file = new File(rutaDirecciones);
	        FileReader fr;
			fr = new FileReader(file);
			BufferedReader lector = new BufferedReader(fr);
			String linea=lector.readLine();
			while((linea!=null)&&(pos!=Integer.parseInt(Character.toString(linea.charAt(0))))) { //vamos leyendo todo el contenido del txt por si ya habia ubicaciones agregadas
				linea=lector.readLine();
			}
			if(linea==null) {
				seEncontro="";
			}
			else {
				seEncontro=linea;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	System.out.println("encontro este punto: "+seEncontro);
	return seEncontro;
}


public int comparar(double x, double y, String rutaDirecciones) { //retorna -1 si no esta osea ususario clikeo lejos
	int pos=-1;
	try {
        File file = new File(rutaDirecciones);
        FileReader fr = new FileReader(file);
        BufferedReader lector = new BufferedReader(fr);
        String linea;
    	double cordX, cordY;
        while(((linea = lector.readLine())!=null)&&(pos==-1)) { //vamos leyendo punto por punto y buscando su coordenada para ver si el usuario clikeo cerca de uno
        	int k=0;
            String textX="";
            String textY="";
        	System.out.println("linea: "+linea+" tamaño linea: "+linea.length());
        	for(int i=0;i<linea.length();i++) {
//        		System.out.print(linea.charAt(i));
        		if(linea.charAt(i)==';') {
        			k++;
            	}else {
            		if(k==2) { //si es k=2 entonces esta leyendo coordenada x
        			textX=textX+linea.charAt(i);
            		}
            		if(k==3) { //si es k=3 esta leyendo coordenada y
            			textY=textY+linea.charAt(i);
            		}
            		if(k==4) {
            			i=linea.length(); //ya no necesitamos seguir recorriendo
            		}
            	}
        		
        	}
        	cordX= Double.parseDouble(textX);
        	cordY= Double.parseDouble(textY);
        	if(((x-2<cordX)&&(cordX<x+2))&&((y-2<cordY)&&(cordY<y+2))) { //si esta dentro de un cuadro +2-2 el punto
        		pos=Integer.parseInt(Character.toString(linea.charAt(0))); //guarda la posicion
        	}
        }
        lector.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
	return pos;
}




/** creamos el estilo por defecto para mostrar las caracteristicas */
private Style createDefaultStyle() {
    Rule rule = createRule(LINE_COLOUR, FILL_COLOUR); //utilizamos las definiciones anteriores

    FeatureTypeStyle fts = sf.createFeatureTypeStyle();
    fts.rules().add(rule);

    Style style = sf.createStyle();
    style.featureTypeStyles().add(fts);
    return style;
}

/**
 * Create a Style where features with given IDs are painted yellow, while others are painted
 * with the default colors.
 */
private Style createSelectedStyle(Set<FeatureId> IDs) {
    Rule selectedRule = createRule(SELECTED_COLOUR, SELECTED_COLOUR);
    selectedRule.setFilter(ff.id(IDs));

    Rule otherRule = createRule(LINE_COLOUR, FILL_COLOUR);
    otherRule.setElseFilter(true);

    FeatureTypeStyle fts = sf.createFeatureTypeStyle();
    fts.rules().add(selectedRule);
    fts.rules().add(otherRule);

    Style style = sf.createStyle();
    style.featureTypeStyles().add(fts);
    return style;
}

//-------------------------------------------------------

/**
 * Helper for createXXXStyle methods. Creates a new Rule containing a Symbolizer tailored to the
 * geometry type of the features that we are displaying.
 */
private Rule createRule(Color outlineColor, Color fillColor) {
    Symbolizer symbolizer = null;
    Fill fill = null;
    Stroke stroke = sf.createStroke(ff.literal(outlineColor), ff.literal(LINE_WIDTH));

    switch (geometryType) {
        case POLYGON:
            fill = sf.createFill(ff.literal(fillColor), ff.literal(OPACITY));
            symbolizer = sf.createPolygonSymbolizer(stroke, fill, geometryAttributeName);
            break;

        case LINE:
            symbolizer = sf.createLineSymbolizer(stroke, geometryAttributeName);
            break;

        case POINT:
            fill = sf.createFill(ff.literal(fillColor), ff.literal(OPACITY));

            Mark mark = sf.getCircleMark();
            mark.setFill(fill);
            mark.setStroke(stroke);

            Graphic graphic = sf.createDefaultGraphic();
            graphic.graphicalSymbols().clear();
            graphic.graphicalSymbols().add(mark);
            graphic.setSize(ff.literal(POINT_SIZE));

            symbolizer = sf.createPointSymbolizer(graphic, geometryAttributeName);
    }

    Rule rule = sf.createRule();
    rule.symbolizers().add(symbolizer);
    return rule;
}

//-----------------------------------

/** Retrieve information about the feature geometry */
private void setGeometry() {
    GeometryDescriptor geomDesc = featureSource.getSchema().getGeometryDescriptor();
    geometryAttributeName = geomDesc.getLocalName();

    Class<?> clazz = geomDesc.getType().getBinding();

    if (Polygon.class.isAssignableFrom(clazz) || MultiPolygon.class.isAssignableFrom(clazz)) {
        geometryType = GeomType.POLYGON;

    } else if (LineString.class.isAssignableFrom(clazz)
            || MultiLineString.class.isAssignableFrom(clazz)) {

        geometryType = GeomType.LINE;

    } else {
        geometryType = GeomType.POINT;
    }
}

}

//-----------------------------------
