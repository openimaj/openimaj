Using OpenIMAJ within Groovy code is really easy. Just use [Grape](http://groovy.codehaus.org/Grape) annotations or method calls to download and link the OpenIMAJ jars automatically. You also need to specify the 
repository from which to download the OpenIMAJ jars using the `GrabResolver` method or annotation:

    @GrabResolver(name='openimaj-releases', 
	              root='http://maven.openimaj.org/')

The following code demonstrates how difference-of-Gaussian SIFT features can be extracted in a simple Groovy program:

~~~~~~
@GrabResolver(name='openimaj-releases', 
              root='http://maven.openimaj.org/')
@Grab('org.openimaj:core-image:1.1')
import org.openimaj.io.*
import org.openimaj.image.*
import org.openimaj.image.colour.*
import org.openimaj.math.geometry.shape.*

@Grab('org.openimaj:image-local-features:1.1')
import org.openimaj.image.feature.local.engine.*

//Load an image
img = ImageUtilities.readMBF( args.length>0 ? 
              new File(args[0]) : 
              getClass().getResource("/org/openimaj/OpenIMAJ.png"))

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
~~~~~~

The above code can be copied into the groovyConsole application and run directly:

<img src='images/groovy-sift.png' alt='SIFT Extraction in Groovy' width='90%'/>

Additional examples of using OpenIMAJ in Groovy can be found in the GroovyDemo folder at [/trunk/demo/GroovyDemo](https://sourceforge.net/p/openimaj/code/1781/tree/trunk/demos/GroovyDemo/) in the source repository.
