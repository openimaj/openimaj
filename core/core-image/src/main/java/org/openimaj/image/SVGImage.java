package org.openimaj.image;

import java.util.Comparator;

import org.openimaj.image.renderer.ImageRenderer;
import org.openimaj.image.renderer.RenderHints;
import org.openimaj.image.renderer.SVGRenderHints;
import org.openimaj.image.renderer.SVGRenderer;
import org.openimaj.math.geometry.shape.Rectangle;

public class SVGImage extends Image<Float[], SVGImage> {
	
	private SVGRenderer renderer;

	public SVGImage(SVGRenderHints hints) {
		this.renderer = new SVGRenderer(hints);
	}
	
	private SVGImage(SVGRenderer svgRenderer) {
		this.renderer = svgRenderer;
	}

	private SVGImage() {
		// TODO Auto-generated constructor stub
	}

	public SVGImage(int w, int h) {
		this(new SVGRenderHints(w, h));
	}

	@Override
	public SVGImage abs() {
		return this;
	}

	@Override
	public SVGImage addInplace(Image<?, ?> im) {
		if(!(im instanceof SVGImage)){
			this.renderer.drawOIImage(im);
		} else {
			this.renderer.drawImage((SVGImage) im, 0, 0);
		}
		return null;
	}

	@Override
	public SVGImage addInplace(Float[] num) {
		return this;
	}

	@Override
	public SVGImage clip(Float[] min, Float[] max) {
		return this;
	}

	@Override
	public SVGImage clipMax(Float[] thresh) {
		return this;
	}

	@Override
	public SVGImage clipMin(Float[] thresh) {
		return this;
	}

	@Override
	public SVGImage clone() {
		SVGImage svgImage = new SVGImage();
		svgImage.renderer = new SVGRenderer(svgImage, this.renderer.getGraphics2D().create());
		return svgImage;
	}
	
	@Override
	public SVGRenderer createRenderer() {
		return this.renderer;
	}

	@Override
	public ImageRenderer<Float[], SVGImage> createRenderer(RenderHints options) {
		return this.renderer;
	}

	@Override
	public SVGImage divideInplace(Image<?, ?> im) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SVGImage divideInplace(Float[] val) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SVGImage extractROI(int x, int y, SVGImage img) {
		img.renderer = new SVGRenderer(img,img.renderer.getRenderHints(),this.renderer.getGraphics2D().create(x, y, img.getWidth(), img.getHeight()));
		return img;
	}

	@Override
	public SVGImage extractROI(int x, int y, int w, int h) {
		SVGImage ret = new SVGImage(w,h);
		return null;
	}

	@Override
	public SVGImage fill(Float[] colour) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SVGImage flipX() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SVGImage flipY() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rectangle getContentArea() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SVGImage getField(org.openimaj.image.Image.Field f) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SVGImage getFieldCopy(org.openimaj.image.Image.Field f) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SVGImage getFieldInterpolate(org.openimaj.image.Image.Field f) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Float[] getPixel(int x, int y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<? super Float[]> getPixelComparator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Float[] getPixelInterp(double x, double y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Float[] getPixelInterp(double x, double y, Float[] backgroundColour) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SVGImage internalCopy(SVGImage im) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SVGImage internalAssign(SVGImage im) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SVGImage internalAssign(int[] pixelData, int width, int height) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SVGImage inverse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Float[] max() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Float[] min() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SVGImage multiplyInplace(Image<?, ?> im) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SVGImage multiplyInplace(Float[] num) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SVGImage newInstance(int width, int height) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SVGImage normalise() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPixel(int x, int y, Float[] val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SVGImage subtractInplace(Image<?, ?> im) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SVGImage subtractInplace(Float[] num) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SVGImage threshold(Float[] thresh) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] toByteImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] toPackedARGBPixels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SVGImage zero() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SVGImage overlayInplace(SVGImage image, int x, int y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SVGImage replace(Float[] target, Float[] replacement) {
		// TODO Auto-generated method stub
		return null;
	}

}
