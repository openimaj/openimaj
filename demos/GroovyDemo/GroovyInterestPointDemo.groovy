@GrabResolver(name='octopussy-releases', root='http://octopussy.ecs.soton.ac.uk/m2/releases/')
@Grab('org.openimaj:core-image:1.0-SNAPSHOT')
import org.openimaj.io.*
import org.openimaj.image.*
import org.openimaj.image.colour.*
import org.openimaj.math.geometry.shape.*

@Grab('org.openimaj:image-local-features:1.0-SNAPSHOT')
import org.openimaj.image.feature.local.interest.*

//Load an image
img = ImageUtilities.readMBF( args.length>0 ? new File(args[0]) : getClass().getResource("/org/openimaj/OpenIMAJ.png"))

//make a grey version
gimg = Transforms.calculateIntensityNTSC(img)

//set the scales (std.dev of Gaussian)
float integrationScale = 2.5f
float differentiationScale = 0.6f * integrationScale

//calculate the variance
float integrationScaleVar = integrationScale**2
float differentiationScaleVar = differentiationScale**2

//set up a list of detectors
ipds = [
    [ipd: new HarrisIPD(differentiationScaleVar, integrationScaleVar), colour:RGBColour.RED],
    [ipd: new HessianIPD(differentiationScaleVar, integrationScaleVar), colour:RGBColour.MAGENTA],
    [ipd: new LaplaceIPD(differentiationScaleVar, integrationScaleVar), colour:RGBColour.GREEN],
]

//loop through detectors and draw 100 best points found
ipds.each{rec ->
    rec.ipd.findInterestPoints(gimg)
    img.drawPoints(rec.ipd.getInterestPoints(100), rec.colour, 3)
}
        
//display the result
DisplayUtilities.display(img)