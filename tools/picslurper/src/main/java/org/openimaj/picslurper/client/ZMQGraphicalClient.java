package org.openimaj.picslurper.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.resize.Lanczos3Filter;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.io.IOUtils;
import org.openimaj.picslurper.output.WriteableImageOutput;
import org.zeromq.ZMQ;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ZMQGraphicalClient {
	private static final int GRID_W = 160;
	private static final int GRID_H = 120;

	private static final int GRID_NX = 3;
	private static final int GRID_NY = 3;

	static class GridDisplay{
		private int displayWidth;
		private int displayHeight;
		private int thumbXOffset;
		private MBFImage display;
		private LinkedList<MBFImage> displayList;
		private ResizeProcessor mainResizer;
		private ResizeProcessor thumbResizer;

		public GridDisplay() {
			this.displayWidth = (GRID_NX * GRID_W) * 2;
			this.displayHeight = (GRID_NY * GRID_H);

			this.thumbXOffset = (GRID_NX * GRID_W);

			this.mainResizer = new ResizeProcessor((GRID_NX * GRID_W), (GRID_NY * GRID_H), ResizeProcessor.Mode.FIT_ASPECT_RATIO, new Lanczos3Filter());
			this.thumbResizer = new ResizeProcessor(GRID_W,GRID_H, ResizeProcessor.Mode.FIT_ASPECT_RATIO, new Lanczos3Filter());

			this.display = new MBFImage(displayWidth,displayHeight,ColourSpace.RGB);

			this.displayList = new LinkedList<MBFImage>();

			this.redraw();
		}

		private void redraw() {
			this.display.fill(RGBColour.WHITE);
			boolean first = true;
			// Start at the end (i.e. most recent)
			int ind = 0;
			for (MBFImage img : this.displayList) {
				if(first){
					first = false;
					// main image!
					this.display.drawImage(img.process(this.mainResizer), 0, 0);
				}
				else{
					int y = ind / GRID_NX;
					int x = (ind - (y * GRID_NX));
					this.display.drawImage(img.process(this.thumbResizer),this.thumbXOffset + (x * GRID_W), y * GRID_H);
					ind++;
				}
			}

			DisplayUtilities.displayName(display, "Pics, slurped!");
		}

		public void add(MBFImage img){
			if(this.displayList.size() == (GRID_NX * GRID_NY + 1)){
				this.displayList.removeLast();
			}
			this.displayList.addFirst(img);
			this.redraw();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String args[]) {

		// Prepare our context and subscriber
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket subscriber = context.socket(ZMQ.SUB);

		subscriber.connect("tcp://localhost:5563");
		subscriber.subscribe(new byte[0]);

		GridDisplay display = new GridDisplay();
		while (true) {
			ByteArrayInputStream stream = new ByteArrayInputStream(subscriber.recv(0));
			WriteableImageOutput instance;
			try {
				instance = IOUtils.read(stream, WriteableImageOutput.class, "UTF-8");
				System.out.println("Got URL: " + instance.url + " ( " + instance.stats.imageURLs + " ) (about to draw) ");
				for(File imageFile : instance.listImageFiles()){
					display.add(ImageUtilities.readMBF(imageFile));
				}
				System.out.println("SUCCESS!");
			} catch (IOException e) {
				System.out.println("FAILED!");
				e.printStackTrace();
			}
		}
	}
}
