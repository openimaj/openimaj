package org.openimaj.text.geo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class WorldPolygons {

	private static Document doc;
	private Map<String, WorldPlace> countryShapes;
	private Map<String, WorldPlace> countryCodeShapes;
	private Rectangle bounds;

	static {
		final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

		try {
			final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(WorldPolygons.class.getResourceAsStream("./countries_world.kml"));

		} catch (final Exception e) {
			throw new RuntimeException();
		}
	}

	public WorldPolygons() {
		this.countryShapes = new HashMap<String, WorldPlace>();
		countryCodeShapes = new HashMap<String, WorldPlace>();
		doc.getDocumentElement().normalize();
		final NodeList places = doc.getElementsByTagName("Placemark");
		float minx = Integer.MAX_VALUE, miny = Integer.MAX_VALUE, maxx = -Integer.MAX_VALUE, maxy = -Integer.MAX_VALUE;
		for (int i = 0; i < places.getLength(); i++) {
			final Node placeNode = places.item(i);
			/**
			 * [name: null] [description: null] [LookAt: null] [Style: null]
			 * [MultiGeometry: null]
			 */
			final String name = getNodeValue(placeNode, "name");
			final String desc = getNodeValue(placeNode, "description");
			final String countryCode = desc.split(":")[0].split("=")[1].trim().toLowerCase();
			final Node lookat = getFirstNode(placeNode, "LookAt");
			final String latStr = getNodeValue(lookat, "latitude");
			final String lonStr = getNodeValue(lookat, "longitude");
			final Element multiGeom = (Element) getFirstNode(placeNode, "MultiGeometry");
			final NodeList polygonNodes = multiGeom.getElementsByTagName("Polygon");
			final List<Shape> polygons = new ArrayList<Shape>();
			for (int j = 0; j < polygonNodes.getLength(); j++) {
				final String[] coords = getNodeValue(polygonNodes.item(j), "coordinates").split(" ");
				final List<Point2d> points = new ArrayList<Point2d>();
				for (final String coord : coords) {
					final String[] xy = coord.split(",");
					final float fx = Float.parseFloat(xy[0]);
					final float fy = Float.parseFloat(xy[1]);
					minx = Math.min(minx, fx);
					miny = Math.min(miny, fy);
					maxx = Math.max(maxx, fx);
					maxy = Math.max(maxy, fy);
					points.add(new Point2dImpl(fx, fy));
				}
				polygons.add(new Polygon(points));
			}

			final WorldPlace place = new WorldPlace(
					name, countryCode,
					Float.parseFloat(latStr), Float.parseFloat(lonStr),
					polygons);
			this.countryShapes.put(name, place);
			this.countryCodeShapes.put(countryCode, place);
		}
		this.bounds = new Rectangle(minx, miny, maxx - minx, maxy - miny);
	}

	private String getNodeValue(Node node, String nodeName) {
		final Node firstNode = getFirstNode(node, nodeName);
		return firstNode.getFirstChild().getNodeValue();
	}

	private Node getFirstNode(Node node, String nodeName) {
		return ((Element) node).getElementsByTagName(nodeName).item(0);
	}

	/**
	 * @return
	 */
	public Collection<WorldPlace> getShapes() {
		return this.countryShapes.values();
	}

	public WorldPlace byCountryCode(String countryCode) {
		return this.countryCodeShapes.get(countryCode.toLowerCase());
	}

	public WorldPlace byCountry(String country) {
		return this.countryShapes.get(country);
	}

	public Rectangle getBounds() {
		return bounds;
	}
}
