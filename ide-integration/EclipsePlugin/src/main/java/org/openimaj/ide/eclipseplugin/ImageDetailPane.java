package org.openimaj.ide.eclipseplugin;

import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPartSite;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

/**
 * A pane for displaying image data graphically in the debugger
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class ImageDetailPane implements IDetailPane {
	/**
	 * The plugin id
	 */
	public static final String ID = "org.openimaj.ide.eclipseplugin.ImageDetailPane";
	
	/**
	 * The plugin name 
	 */
	public static final String NAME = "Image Detail Pane";
	
	/**
	 * The plugin description 
	 */
	public static final String DESCRIPTION = "Displays an image";

	Label label;

	@Override
	public Control createControl(Composite parent) {
		label = new Label(parent, SWT.CENTER);

		return label;
	}

	FImage readFImage(IJavaObject var) throws DebugException {
		int width = ((IJavaPrimitiveValue)var.getField("width", true).getValue()).getIntValue();
		int height = ((IJavaPrimitiveValue)var.getField("height", true).getValue()).getIntValue();
		
		IJavaValue[] data = ((IJavaArray)var.getField("pixels", false).getValue()).getValues();
		
		FImage img = new FImage(width, height);
		
		for (int y=0; y<height; y++) {
			IJavaValue[] row = ((IJavaArray) data[y]).getValues();
			
			for (int x=0; x<width; x++) {
				img.pixels[y][x] = ((IJavaPrimitiveValue) row[x]).getFloatValue();
			}
		}
		
		return img;
	}
	
	MBFImage readMBFImage(IJavaObject var) throws DebugException {
		return null;
	}
	
	org.openimaj.image.Image<?, ?> readImage(IJavaObject var) throws DebugException {
		if (var.getJavaType().getName().equals(FImage.class.getName())) {
			return readFImage(var);
		}
		
		if (var.getJavaType().getName().equals(MBFImage.class.getName())) {
			return readMBFImage(var);
		}
		
		return null;
	}
	
	@Override
	public void display(IStructuredSelection arg0) {
		if (arg0 == null || arg0.getFirstElement() == null) return;
		
		try {
			IJavaObject var = (IJavaObject) ((IJavaVariable) arg0.getFirstElement()).getValue();
			
			org.openimaj.image.Image<?, ?> img = readImage(var);
			ImageData imageData = convertToSWT(ImageUtilities.createBufferedImage(img));
			
			label.setImage(new Image(label.getDisplay(), imageData));
		} catch (DebugException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void dispose() {
		label.dispose();
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void init(IWorkbenchPartSite arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean setFocus() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * From http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/ConvertbetweenSWTImageandAWTBufferedImage.htm
	 */
	static ImageData convertToSWT(BufferedImage bufferedImage) {
		if (bufferedImage.getColorModel() instanceof DirectColorModel) {
			DirectColorModel colorModel = (DirectColorModel) bufferedImage.getColorModel();
			PaletteData palette = new PaletteData(colorModel.getRedMask(),
					colorModel.getGreenMask(), colorModel.getBlueMask());

			ImageData data = new ImageData(bufferedImage.getWidth(),
					bufferedImage.getHeight(), colorModel.getPixelSize(),
					palette);

			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[3];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					int pixel = palette.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
					data.setPixel(x, y, pixel);
				}
			}

			return data;
		} 
		else if (bufferedImage.getColorModel() instanceof IndexColorModel) 
		{
			IndexColorModel colorModel = (IndexColorModel) bufferedImage
			.getColorModel();
			int size = colorModel.getMapSize();
			byte[] reds = new byte[size];
			byte[] greens = new byte[size];
			byte[] blues = new byte[size];
			colorModel.getReds(reds);
			colorModel.getGreens(greens);
			colorModel.getBlues(blues);
			RGB[] rgbs = new RGB[size];
			for (int i = 0; i < rgbs.length; i++) {
				rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF,
						blues[i] & 0xFF);
			}
			PaletteData palette = new PaletteData(rgbs);
			ImageData data = new ImageData(bufferedImage.getWidth(),
					bufferedImage.getHeight(), colorModel.getPixelSize(),
					palette);
			data.transparentPixel = colorModel.getTransparentPixel();
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					data.setPixel(x, y, pixelArray[0]);
				}
			}
			
			return data;
		} else if (bufferedImage.getColorModel() instanceof ComponentColorModel) {
		    ComponentColorModel colorModel = (ComponentColorModel)bufferedImage.getColorModel();

		    //ASSUMES: 3 BYTE BGR IMAGE TYPE

		    PaletteData palette = new PaletteData(0x0000FF, 0x00FF00,0xFF0000);
		    ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);

		    //This is valid because we are using a 3-byte Data model with no transparent pixels
		    data.transparentPixel = -1;

		    WritableRaster raster = bufferedImage.getRaster();
		    int[] pixelArray = new int[3];
		    for (int y = 0; y < data.height; y++) {
		        for (int x = 0; x < data.width; x++) {
		            raster.getPixel(x, y, pixelArray);
		            int pixel = palette.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
		            data.setPixel(x, y, pixel);
		        }
		    }
		    return data;
		}
		
		return null;
	}
}
