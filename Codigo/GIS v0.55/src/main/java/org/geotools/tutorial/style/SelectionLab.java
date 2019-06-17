package org.geotools.tutorial.style;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
public class SelectionLab {

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

    /*
     * Main
     */
    public static void main(String[] args) throws Exception {
        SelectionLab me = new SelectionLab();

        File file = JFileDataStoreChooser.showOpenFile("shp", null); //creamos un StoreChooser para elegir el archivo .shp
        if (file == null) {
            return;
        }

        me.displayShapefile(file); //mostramos el shapefile
    }

/**
 * Este metodo se conecta con el shapefile; recupera la informacion de sus caracteristicas (geometria, tipos de coordenadas, etc
 * crea un mapFrame para mostrar el shapefile y crea una herramienta de seleccion a la barra de erra mientas del mapframe 
 */
public void displayShapefile(File file) throws Exception {
    FileDataStore store = FileDataStoreFinder.getDataStore(file);
    featureSource = store.getFeatureSource();
    setGeometry(); //definimos la geometria

    /*
     * Crea el jmapFrame y lo configura para mostrar el shapefile con estilo por defecto
     */
    MapContent map = new MapContent();
    map.setTitle("Ejemplo seleccion y dibujo");
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
        	if(respuesta==0) {
        		String texto=JOptionPane.showInputDialog(null, "Nombre del lugar");
        		DirectPosition2D p = ev.getWorldPos();
                System.out.println(p.getX() + " -- " + p.getY());
                drawMyPoint(p.getX(), p.getY(), map, texto);
        	}
        	else{
        		System.out.println("Punto no guardado");
        	}
            
        }
    }));        

    JToolBar toolBar = mapFrame.getToolBar();
    JButton btn = new JButton("Select");    //el boton que agregamos
    toolBar.addSeparator();
    toolBar.add(btn);


    btn.addActionListener(e ->mapFrame.getMapPane().setCursorTool(new CursorTool() {@Override //accion al seleccionar el boton "select"
                                        public void onMouseClicked(MapMouseEvent ev) {
                                            selectFeatures(ev);
                                        }
                                    }));

    
    mapFrame.setSize(600, 600);
    mapFrame.setVisible(true);
}

void selectFeatures(MapMouseEvent ev) { // metodo que define la accion a realizar cuando el ususario presiona el boton "select"

    System.out.println("Mouse click at: " + ev.getWorldPos()); //mostramos las coordenadas por consola
    
    /*
     * construimos un rectangulo de 5x5 pixeles centrado en la posicion donde se hizo click      
     */
        
    Point screenPos = ev.getPoint();
    Rectangle screenRect = new Rectangle(screenPos.x - 2, screenPos.y - 2, 5, 5); 
    
    /*
     * Transformamos el rectangulo de pantalla en una caja de coordenadas en el sistema de referencia de nuestro mapa
     */
    AffineTransform screenToWorld = mapFrame.getMapPane().getScreenToWorldTransform();
    Rectangle2D worldRect = screenToWorld.createTransformedShape(screenRect).getBounds2D();
    ReferencedEnvelope bbox =
            new ReferencedEnvelope(
                    worldRect, mapFrame.getMapContent().getCoordinateReferenceSystem());

    /*
     * Creamos un filtro para seleccionar las caraceristicas que intersecten con nuestra caja
     */
    Filter filter = ff.intersects(ff.property(geometryAttributeName), ff.literal(bbox));

    /*
     * Usamos el filtro para identificar la caracteristica seleccionada
     */
    try {
        SimpleFeatureCollection selectedFeatures = featureSource.getFeatures(filter);

        Set<FeatureId> IDs = new HashSet<>();
        try (SimpleFeatureIterator iter = selectedFeatures.features()) {
            while (iter.hasNext()) {
                SimpleFeature feature = iter.next();
                IDs.add(feature.getIdentifier());

                System.out.println("   " + feature.getIdentifier());
            }
        }

        if (IDs.isEmpty()) {
            System.out.println("   no feature selected");
        }

        displaySelectedFeatures(IDs); //mostramos lo seleccionado

    } catch (Exception ex) {
        ex.printStackTrace();
    }
}

//---------------------------------------




/**
 * Metodo que define como se va a dibujar el punto
 * primerio creara un punto geometrico con las coordenadas de donde se clikeo y luego guardara dicha posicion en un archivo de texto
 */

void drawMyPoint(double x, double y, MapContent map, String nombre) { 
//    SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
//    builder.setName("MyFeatureType");
//    builder.setCRS( DefaultGeographicCRS.WGS84 ); // set crs        
//    builder.add("location", Point.class); // add geometry
//
//    // build the type
//    SimpleFeatureType TYPE = builder.buildFeatureType();
//
//    // create features using the type defined
//    SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
//    org.locationtech.jts.geom.GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
//    org.locationtech.jts.geom.Point point = geometryFactory.createPoint(new Coordinate(x, y));
//    //
//    
//    //
//    featureBuilder.add(point);
//    SimpleFeature feature = featureBuilder.buildFeature("FeaturePoint");
//    DefaultFeatureCollection featureCollection = new DefaultFeatureCollection("internal", TYPE);
//    featureCollection.add(feature); // Add feature 1, 2, 3, etc
//
//    Style style = SLD.createPointStyle("Star", Color.BLUE, Color.BLUE, 0.3f, 200);
//    Layer layer = new FeatureLayer(featureCollection, style);
//    layer.setTitle("NewPointLayer");
//    map.addLayer(layer);
//    //mapFrame.getMapPane().repaint();// MapPane.repaint();
//}
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
       Logger.getLogger(SelectionLab.class.getName()).log(Level.SEVERE, null, ex);
   }
SimpleFeatureBuilder featureBuilderPoints = new SimpleFeatureBuilder(pointtype); //creamos el constructor de lo que agregaremos
DefaultFeatureCollection collectionPoints = new DefaultFeatureCollection("internal", pointtype);  //creamos el contenedor donde pondremos lo que agregaremos
//PointString Point=builder.createPointString(Point);
//LineString Point = builder.createLineString(Point);
featureBuilderPoints.add(point);  //agregamos el punto en el constructor
SimpleFeature featureLine = featureBuilderPoints.buildFeature(null);  
((DefaultFeatureCollection)collectionPoints).add(featureLine);     
Style PointStyle = SLD.createPointStyle("circle", Color.RED, Color.RED,(float) 0.5,(float) 10);  //definimos el estilo del punto (icono, color contorno, color, opacidad y tamaño)
//Obserervacion: el dibujo del punto siempre se mantiene del mismo tamaño por mas que se use el zoom
map.addLayer(new FeatureLayer(collectionPoints, PointStyle)); //agregamos el punto a la capa

guardarCoordenadas(nombre,x,y);
}

public void guardarCoordenadas(String nombre, double x, double y) {
	
    try {
        String ruta = "C:\\Users\\Usuario\\eclipse-workspace\\tutorial/direcciones.txt";
        File file = new File(ruta);
        // Si el archivo no existe es creado
        if (!file.exists()) {
            file.createNewFile();
        }
        FileReader fr = new FileReader(file);
        BufferedReader lector = new BufferedReader(fr);
        String contenido="";
        String linea;
        while((linea = lector.readLine())!=null) { //vamos leyendo todo el contenido del txt por si ya habia ubicaciones agregadas
        	System.out.println(contenido);
            contenido=contenido+linea+"\n";
        }
        contenido=contenido+"nombre:"+nombre+"x:"+String.valueOf(x)+"y:"+String.valueOf(y)+"\n";
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(contenido);
        bw.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
	
}


/**
 * Sets the display to paint selected features yellow and unselected features in the default
 * style.
 *
 * @param IDs identifica las caracteristica seleccionadas actualmente
 */
public void displaySelectedFeatures(Set<FeatureId> IDs) {
    Style style;

    if (IDs.isEmpty()) {
        style = createDefaultStyle();

    } else {
        style = createSelectedStyle(IDs);
    }

    Layer layer = mapFrame.getMapContent().layers().get(0);
    ((FeatureLayer) layer).setStyle(style);
    mapFrame.getMapPane().repaint();
}

//---------------------------------------------

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
