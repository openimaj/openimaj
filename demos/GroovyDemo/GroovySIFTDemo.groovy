@GrabResolver(name='octopussy-releases', root='http://octopussy.ecs.soton.ac.uk/m2/releases/')
@Grab('org.openimaj:core-image:1.0-SNAPSHOT')
import org.openimaj.io.*
import org.openimaj.image.*
import org.openimaj.image.colour.*
import org.openimaj.math.geometry.shape.*

@Grab('org.openimaj:image-local-features:1.0-SNAPSHOT')
import org.openimaj.image.feature.local.engine.*

//Load an image
img = ImageUtilities.readMBF( args.length>0 ? new File(args[0]) : getClass().getResource("/org/openimaj/OpenIMAJ.png"))

//create a default difference-of-Gaussian SIFT extraction engine
engine = new DoGSIFTEngine()

//extract the features
features = engine.findFeatures(Transforms.calculateIntensityNTSC(img))

//draw the regions
features.each { feature ->
    img.drawShape(new Circle(feature.x, feature.y, feature.scale), RGBColour.RED)
}

//display the result
DisplayUtilities.display(img)

//write to standard output in the format used by Lowe's tool
IOUtils.writeASCII(System.out, features)
