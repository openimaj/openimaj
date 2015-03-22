This is the readme file for the OpenIMAJ FlickrCrawler Tools. 
The most recent version of this file is available on the OpenIMAJ wiki: 
https://sourceforge.net/p/openimaj/wiki/The%20FlickrCrawler%20Tools/

---

The OpenIMAJ FlickrCrawler tools enable you to download large collections of images using the Flickr API for experimentation purposes. The FlickCrawler tools are implemented as simple [Groovy](http://groovy.codehaus.org) scripts, and as such require that you have Groovy version 1.7 or later installed on your system.

#The FlickrCrawler.groovy Script
The FlickrCrawler.groovy script is the main tool for downloading images using the [flickr.photos.search](http://www.flickr.com/services/api/flickr.photos.search.html) API. It has a number of useful features:

- Support for stopping and resuming image crawls.
- Ability to download metadata and EXIF for each image crawled.
- Ability to configure complex multi-parameter API queries.
- Control over the sizes of images being downloaded.
    - Specify an ordered list of preferred size and the first available one will be downloaded,
    - and/or force the download of any/all specific sizes.

The FlickrCrawler.groovy script is invoked from the command-line as follows:

    groovy FlickrCrawler.groovy config_file

where `config_file` is the path to the configuration file that describes the parameters of your crawl, as described below.

##FlickrCrawler Configuration
The FlickrCrawler.groovy configuration file is a simple text file that contains the information the crawler needs to find the relevant images to download. A complete configuration file will look like the following:

~~~~~
:::java
crawler {
	apikey="ENTER_YOUR_FLICKR_API_KEY_HERE" //your flickr api key
	secret="ENTER_YOUR_FLICKR_API_SECRET_HERE" //your flickr api secret
	apihitfreq=1000 //number of milliseconds between api calls
	hitfreq=1000    //number of milliseconds between retries of failed downloads
	outputdir="crawl-data"   //name of directory to save images and data to
	maximages=-1    //limit the number of images to be downloaded; -1 is unlimited
	maxRetries=3000 //maximum number of retries after failed api calls
	force=false     //force re-download of duplicate images
	perpage=500     //number of results to request from the api per call
	queryparams {   //the parameters describing the query
	
	}
	concurrentDownloads=16  //max number of concurrent image downloads
	pagingLimit=20          //max number of pages to look through
	maxretrytime=300000     //maximum amout of time between retries
    skipDownloadImages=false //skip image downloading
	data {                  
	    info=true           //download all the information about each image
	    exif=true           //download all the exif information about each image
	}
	images {
		targetSize=["large","original"] //preferred image sizes in order
		smallSquare=false               //should small square images be downloaded
        thumbnail=false                 //should thumbnail images be downloaded
        small=false                     //should small images be downloaded
        medium=false                    //should medium images be downloaded
        large=false                     //should large images be downloaded
        original=false                  //should original size images be downloaded
	}
}
~~~~~

In practice however, the crawler has sensible defaults for most of the configuration and many of the options can be omitted. For most crawls, the important parts of the configuration are:

- `crawler.apikey`. This is your Flickr API key; if you don't have one you can generate one [here](http://www.flickr.com/services/api/keys/).
- `crawler.secret`. This is your Flickr API secret which you got when you generated your key.
- `crawler.outputdir`. This specifies where you want to save the images.
- `crawler.maximages`. This specifies how many images you want.
- `crawler.images.targetSize`. This specifies which size of image you would prefer.
- `crawler.data.info`. This specifies whether the crawler should attempt to download all the available metadata for an image. Normally you don't want this as the crawl will be very slow as this creates many extra API calls. Even if this is set to false, a large amount of metadata will be downloaded to the images.csv file automatically (see below).
- `crawler.data.exif`. This specifies whether the crawler should attempt to download all the available EXIF data for an image. Normally you don't want this as the crawl will be very slow as this creates many extra API calls.
- `crawler.queryparams`. This is where the query to the [flickr.photos.search](http://www.flickr.com/services/api/flickr.photos.search.html) is configured. See below for some example configurations. The [flickr.photos.search](http://www.flickr.com/services/api/flickr.photos.search.html) page describes the various search options available. Note that in the parameters described on the Flickr API options page are written with underscores, however in the configuration file they must be written as camelCase (i.e. the content_type option would be written as contentType in the configuration file).

###Example Crawl Configurations
The following examples demonstrate practical usage of FlickrCrawler.groovy.

####Example 1: Creative-commons images of Southampton
The following configuration can be used used to download all of the geo-tagged images from Southampton, UK that are licensed with the [Creative Commons Attribution-NonCommercial License](http://creativecommons.org/licenses/by-nc/2.0/):

~~~~~
:::java
crawler {
	apikey="..."
	secret="..."
	outputdir="southampton-cc"
	queryparams {
		woeId="35356" //from flickr.places.find
		license="2" //from flickr.photos.licenses.getInfo
	}
	data {
	    info=false
	    exif=false
	}
	images {
		targetSize=["large", "original", "medium"]
	}
}
~~~~~

The important parts of the configuration are `crawler.queryparams.woeId` which tells the crawler to find images with the specified flickr `where-on-earth identifier`, and the `crawler.queryparams.license` which specifies the license requirements for the downloaded images. Specific `woeId`s can be looked up using the [flickr.places.find explorer page](http://www.flickr.com/services/api/explore/?method=flickr.places.find). The mapping between actual licenses and license identifiers can be found on the [flickr.photos.licenses.getInfo explorer page](http://www.flickr.com/services/api/explore/?method=flickr.photos.licenses.getInfo).

####Example 2: Images tagged with "city" but not "night"
The following configuration illustrates how the FlickrCrawler.groovy script can be made to download 100 images tagged with "city" but not "night":

~~~~~
:::java
crawler {
    apikey="..."
    secret="..."
    outputdir="city-not-night"
	maximages=100
    queryparams {
        tags=["city", "-night"]
		tagMode="bool"
    }
    data {
        info=false
        exif=false
    }
    images {
        targetSize=["large", "original", "medium"]
    }
}
~~~~~

The `crawler.queryparams` part is self explanatory. It should be noted however, that the Flickr API will not allow you to search only with negative terms, so it isn't possible to to search for just "not night".

##Crawl output and images.csv
As the crawler runs it will download images to a directory structure inside the `outputdir` specified in the configuration. In addition to the images, the directory contains a number of other files which relate to the crawl:

- `crawler.config` contains a complete copy of the crawler configuration with all the default variables expanded. Do not edit this file.
- `crawler.state` contains internal information about the state of the crawl, and can be used by the crawler to resume if it is interrupted.
- `crawler-info.log` contains a log of the crawlers actions.
- `images.csv` contains a large amount of metadata about each downloaded image in CSV format, which each line corresponding to a single image. Specifically the fields correspond to all the metadata that the flickr.photo.search API can return with each list of images:
    1. The flickr farm identifier.
    - The flickr server identifier.
    - The flickr image identifier.
    - The image secret.
    - The original image secret (if available).
    - The URL to the medium sized image.
    - The directory the image is stored in after being downloaded.
    - The image title (if present).
    - The image description (if present).
    - The license identifier of the image (see [flickr.photos.licenses.getInfo](http://www.flickr.com/services/api/flickr.photos.licenses.getInfo.html) to see what this means)
    - The date the photo was posted to Flickr.
    - The date the photo was taken taken.
    - The Flickr identifier of the photos owner.
    - The Flickr username of the owner.
    - The geo accuracy ([see flickr.photos.geo.setLocation](http://www.flickr.com/services/api/flickr.photos.geo.setLocation.html)).
    - The latitude at which the photo was taken, if available.
    - The longitude at which the photo was taken, if available.
    - The Flickr tags associated with the image (if present).

#The DownloadMissingImages.groovy Script
Sometimes the FlickrCrawler will fail to download some images (for example, because of network issues). The `DownloadMissingImages.groovy` script will parse the `images.csv` file from a crawl and automatically attempt to download any missing images. Usage is simple; just run the script with the path to the crawl output directory (the `outputdir` specified in your original configuration):

    groovy DownloadMissingImages.groovy crawldir




