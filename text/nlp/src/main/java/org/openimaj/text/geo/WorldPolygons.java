package org.openimaj.text.geo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openimaj.knn.approximate.FloatKDTreeEnsemble;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.aetrion.flickr.places.Place;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class WorldPolygons {
	
	
	private static Document doc;
	private Map<String,WorldPlace> countryShapes;
	private Map<String,WorldPlace> countryCodeShapes;
	private Rectangle bounds;
	
	
	static{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(WorldPolygons.class.getResourceAsStream("./countries_world.kml"));
			
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

	public WorldPolygons() {
		this.countryShapes = new HashMap<String,WorldPlace>();
		countryCodeShapes = new  HashMap<String,WorldPlace>();
		doc.getDocumentElement().normalize();
		NodeList places = doc.getElementsByTagName("Placemark");
		float minx = Integer.MAX_VALUE, miny = Integer.MAX_VALUE, maxx = -Integer.MAX_VALUE, maxy = -Integer.MAX_VALUE;
		for (int i = 0; i < places.getLength(); i++) {
			Node placeNode = places.item(i);
			/**
				[name: null]
				[description: null]
				[LookAt: null]
				[Style: null]
				[MultiGeometry: null]
			 */
			String name = getNodeValue(placeNode,"name");
			String desc = getNodeValue(placeNode,"description");
			String countryCode = desc.split(":")[0].split("=")[1].trim().toLowerCase();
			Node lookat = getFirstNode(placeNode,"LookAt");
			String latStr = getNodeValue(lookat,"latitude");
			String lonStr = getNodeValue(lookat,"longitude");
			Element multiGeom = (Element)getFirstNode(placeNode,"MultiGeometry");
			NodeList polygonNodes = multiGeom.getElementsByTagName("Polygon");
			List<Shape> polygons = new ArrayList<Shape>();
			for (int j = 0; j < polygonNodes.getLength(); j++) {
				String[] coords = getNodeValue(polygonNodes.item(j),"coordinates").split(" ");
				List<Point2d> points = new ArrayList<Point2d>();
				for (String coord : coords) {
					String[] xy = coord.split(",");
					float fx = Float.parseFloat(xy[0]);
					float fy = Float.parseFloat(xy[1]);
					minx = Math.min(minx, fx);
					miny = Math.min(miny, fy);
					maxx = Math.max(maxx, fx);
					maxy = Math.max(maxy, fy);
					points.add(new Point2dImpl(fx, fy));
				}
				polygons.add(new Polygon(points));
			}
			
			WorldPlace place = new WorldPlace(
				name, countryCode, 
				Float.parseFloat(latStr),Float.parseFloat(lonStr), 
				polygons);
			this.countryShapes.put(name, place);
			this.countryCodeShapes.put(countryCode,place);
		}
		this.bounds = new Rectangle(minx,miny,maxx-minx,maxy-miny);
	}

	private String getNodeValue(Node node, String nodeName) {
		Node firstNode = getFirstNode(node, nodeName);
		return firstNode.getFirstChild().getNodeValue();
	}

	private Node getFirstNode(Node node, String nodeName) {
		return ((Element)node).getElementsByTagName(nodeName).item(0);
	}

	/**
	 * @return
	 */
	public Collection<WorldPlace> getShapes() {
		return this.countryShapes.values();
	}
	
	public WorldPlace byCountryCode(String countryCode){
		return this.countryCodeShapes.get(countryCode.toLowerCase());
	}
	
	public WorldPlace byCountry(String country){
		return this.countryShapes.get(country);
	}

	public Rectangle getBounds() {
		return bounds;
	}
}
