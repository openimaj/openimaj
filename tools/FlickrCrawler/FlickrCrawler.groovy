/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 /**
  * FlickrCrawler.groovy
  * An API crawler for Flickr
  * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@GrabResolver(name='openimaj', root='http://maven.openimaj.org')
@Grab('com.flickr4java:flickr4java:2.13_jonhare_0408059615bfa8da071c2e54268e3004ce62d6a4')
import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.REST
import com.flickr4java.flickr.photos.SearchParameters
import com.flickr4java.flickr.photos.Extras

//@GrabResolver(name='jboss', root='http://repository.jboss.org/maven2/')
@Grab(group='org.codehaus.gpars', module='gpars', version='1.2.0')
import static groovyx.gpars.GParsPool.*

//Configure crawler defaults
defaultConf = """
    crawler {
    	apikey=""
    	secret=""
    	apihitfreq=1000
    	hitfreq=1000
    	outputdir="."
    	maximages=-1
    	maxRetries=3000
    	force=false
    	perpage=500
    	queryparams {}
    	concurrentDownloads=16
    	pagingLimit=20
        maxretrytime=300000
        skipDownloadImages=false
    	data {
    	    info=true
    	    exif=true
    	}
    	images {
			targetSize=["large","original"]
    	    smallSquare=false
            thumbnail=false
            small=false
            medium=false
            large=false
            original=false
    	}
    }
    """

//predef ivars
stateFile = null
crawlState = null
crawlDirConfFile = null
crawlConf = null
currentDirs = null

//add shutdown hook to flush logs and state
addShutdownHook {
    println "\nEnding crawl"
    log("Crawl interrupted")
    if (crawlDirConfFile && crawlConf) {
        log("\tFlushing crawl configuration.")
        crawlDirConfFile.withWriter { writer -> crawlConf.writeTo(writer) }
    }
    if (stateFile && crawlState) {
        log("\tFlushing state information.")
        stateFile.withWriter { writer -> crawlState.writeTo(writer) }
    }
    if (crawlState.imageCount)
        log("\tGot "+crawlState.imageCount+" images in this session.")
    
    if (currentDirs) {
        currentDirs.each {
            log("removing unfinished crawl dir: " + it)
            it.delete()
        }
    }
}

//check commandline params
if (args.length != 1) {
    println "Usage: FlickrCrawler crawl_config_file"
    return
}

crawlDirectory = null
confFile = new File(args[0])
if (confFile.isDirectory()) {
    confFile = new File(confFile, "crawler.config")
    crawlDirectory = confFile
}

if (!confFile.canRead()) {
    println "Error: Unable to read crawl_config_file"
    return
}

//load config and set defaults
crawlConf = new ConfigSlurper().parse(defaultConf).merge(new ConfigSlurper().parse(confFile.toURL()))

//setup crawl dir
if (!crawlDirectory) {
    if (confFile.getParentFile()) {
        crawlDirectory = new File(confFile.getParentFile(), crawlConf.crawler.outputdir)
    } else {
        crawlDirectory = new File(crawlConf.crawler.outputdir)
    }
}

//make crawl dir if necessary
if (!crawlDirectory.exists()) crawlDirectory.mkdirs()

//setup logger
logger = new File(crawlDirectory, "crawler-info.log")
imagelog = new File(crawlDirectory, "images.csv")

//check for conflicts
crawlDirConfFile = new File(crawlDirectory, "crawler.config")
if (crawlDirConfFile.canRead()) {
    oldConf = new ConfigSlurper().parse(defaultConf).merge(new ConfigSlurper().parse(crawlDirConfFile.toURL()))
    
    if (oldConf.crawler.queryparams != crawlConf.crawler.queryparams) {
        println "Error: Existing crawler configuration found in " + crawlDirConfFile + " that has different queryparams from the one specified on the commandline"
        return
    }
}

//copy the configuration to the crawl dir
crawlDirConfFile.withWriter { writer -> crawlConf.writeTo(writer) }

//set crawl state
stateFile = new File(crawlDirectory, "crawler.state")

//current time in millisecs from epoch
now = new Date().time

if (!stateFile.canRead()) {
    //init state
    crawlState = [startDate: now, state:'INPROGRESS', lastDate:now, perpage:crawlConf.crawler.perpage] as ConfigObject
    log("Configured new crawl")
} else {
    crawlState = new ConfigSlurper().parse(stateFile.toURL())
    
    if (crawlState.state == "COMPLETED") {
        //setup new crawl for any images added since last crawl
        crawlState.stopDate = crawlState.startDate
        crawlState.startDate = now
        crawlState.lastDate = now
        crawlState.state = "INPROGRESS"
        
        log("Configured crawl to add new images uploaded since last crawl")
    } else {
        log("Configured crawl to restart from prior interrupt")
    }
}

//write initial state
stateFile.withWriter { writer -> crawlState.writeTo(writer) }

//start crawl
params = new SearchParameters(crawlConf.crawler.queryparams)
//if (crawlState.stopDate) params.setMinUploadDate(new Date(crawlState.stopDate))
params.setExtras(Extras.ALL_EXTRAS)
params.setSort(SearchParameters.DATE_TAKEN_DESC)

flickr = new Flickr(crawlConf.crawler.apikey, crawlConf.crawler.secret, new REST(Flickr.DEFAULT_HOST))
pi = flickr.getPhotosInterface()

log("Starting crawl")

//the crawl loop:
if (!crawlState.imageCount) crawlState.imageCount = 0L
while (true) {
    lastDateStart = crawlState.lastDate //record start date
    params.setMaxTakenDate(new Date(crawlState.lastDate))
    
    //determine pageNumber
    pageNo = 0
    if (crawlState.pageNumber)
        pageNo = crawlState.pageNumber

    //get results page
    try {
        results = pi.search(params, crawlState.perpage, pageNo)
    } catch (Throwable t) {
        results = null
    }
    if (results == null || results.size() == 0) {
        //either a bad search, or a problem
        for (int r=0; r<crawlConf.crawler.maxRetries; r++) {
            log("no results returned. attempting retry " + (r+1))
            
            Thread.sleep(Math.min(crawlConf.crawler.apihitfreq * (r+1), crawlConf.crawler.maxretrytime))
            
            try {
                results = pi.search(params, crawlState.perpage, pageNo)
            } catch (Throwable t) {
                results = null
            }
            if (results != null && results.size() > 0) 
                break
        }
    }

    log("crawling results page starting from date: " + new Date(lastDateStart) + " page: " + pageNo + " size: " + results.size() + " total: " + results.getTotal())
    
    //sometimes the api seems to return less than requested...
    if (results.getPerPage() != crawlState.perpage) crawlState.perpage = results.getPerPage()
    if (!crawlState.expected || crawlState.expected < results.getTotal()) crawlState.expected = results.getTotal()
    
    //got all the images we expected to get
    if (crawlState.imageCount == crawlState.expected) break
        
    if (results.size() == 0) {
        return //still no results, so exit (leaving status as 'INPROGRESS', as we probably hit some kind of fault in the api)
    }
    
    Thread.sleep(crawlConf.crawler.apihitfreq)

    currentDirs = [] as Set

    def lastDate = crawlState.lastDate

    //now loop over the images
    withPool(crawlConf.crawler.concurrentDownloads) {
        results.eachParallel { r ->
            synchronized(crawlState) {
                if (crawlConf.crawler.maximages>0 && crawlState.imageCount >= crawlConf.crawler.maximages) {
                    crawlState.state = "COMPLETED_MAXEDOUT"
                    stateFile.withWriter { writer -> crawlState.writeTo(writer) }
                    log("Reached maximum number of images")
                    return
                }
            }

            File imageDir
            if (r.id.length() > 5)
                imageDir = new File(crawlDirectory, r.farm + File.separator + r.server + File.separator + r.id[0..4] + File.separator + r.id)
            else
                imageDir = new File(crawlDirectory, r.farm + File.separator + r.server + File.separator + r.id[0..(r.id.length()-1)] + File.separator + r.id)

            def thisDate = r.dateTaken.time + 1000L
            synchronized(lastDate) {                 
                if (thisDate < lastDate)
                    lastDate = thisDate
            }
        
            //skip duplicates
            if (imageDir.exists() && !crawlConf.force) {
                log("skipping duplicate\t" + r.id)
            } else {
                synchronized(crawlState) {
                    crawlState.imageCount++
                }
                
                synchronized(currentDirs) {
                    currentDirs << imageDir
                }
                
                String csv = makeCSV(r, imageDir)
                synchronized(imagelog) {
                    imagelog.append(csv)
                }
                
                if (!crawlConf.crawler.skipDownloadImages) {
                    //Download images
                    imageDir.mkdirs()                
                    //Forced download of given sizes
                    if (crawlConf.crawler.images.original) saveImage(imageDir, r, "original")
                    if (crawlConf.crawler.images.large) saveImage(imageDir, r, "large")
                    if (crawlConf.crawler.images.medium) saveImage(imageDir, r, "medium")
                    if (crawlConf.crawler.images.small) saveImage(imageDir, r, "small")
                    if (crawlConf.crawler.images.thumbnail) saveImage(imageDir, r, "thumbnail")
                    if (crawlConf.crawler.images.smallSquare) saveImage(imageDir, r, "smallSquare")
            
                    //Try and get first targetSize, or an alternative one if not available
                    for (String size : crawlConf.crawler.images.targetSize) {
                        if (saveImage(imageDir, r, size, true))
                            break
                    }
                }

                //getInfo
                if (crawlConf.crawler.data.info) {
                    saveData(imageDir, "info.xml", "http://api.flickr.com/services/rest/?method=flickr.photos.getInfo&api_key=${crawlConf.crawler.apikey}&photo_id=${r.id}")
                    Thread.sleep(crawlConf.crawler.apihitfreq)
                }
        
                //getExif
                if (crawlConf.crawler.data.exif) {
                    saveData(imageDir, "exif.xml", "http://api.flickr.com/services/rest/?method=flickr.photos.getExif&api_key=${crawlConf.crawler.apikey}&photo_id=${r.id}")
                    Thread.sleep(crawlConf.crawler.apihitfreq)
                }

                //add one to ensure we get all images
                synchronized(crawlState) { 
                    synchronized(currentDirs) {
                        currentDirs.remove(imageDir)
                    }
                             
                    //crawlState.imageCount++
                    stateFile.withWriter { writer -> crawlState.writeTo(writer) }
                    
                    System.out.format("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b") 
                    System.out.format("Crawling: %10d", crawlState.imageCount) 
                    log("crawling\t" + r.dateTaken + "\t" + r.id + "\t" +crawlState.imageCount)
                }
            }
        }
    }
    
    if (crawlConf.crawler.maximages>0 && crawlState.imageCount >= crawlConf.crawler.maximages) {
        break;
    }
    
    crawlState.lastDate = lastDate
    //crawlState.lastDate -= 30*60*1000
    
    if (lastDateStart == crawlState.lastDate) {
        //we have a page with all the same dates,
        //so we need to increment the page number
        if (!crawlState.pageNumber) {
            crawlState.pageNumber = 1
        } else {
            crawlState.pageNumber++
            
            //too many pages....
            if (crawlState.pageNumber > crawlConf.crawler.pagingLimit) {
                crawlState.remove("pageNumber")
                crawlState.lastDate -= (60*60*1000) // decr 1 hrs
            }
        }
    } else {
        crawlState.remove('pageNumber')
    }
    
    stateFile.withWriter { writer -> crawlState.writeTo(writer) }
    
    //run out of images?
    //if (crawlState.perpage != results.size()) {
    //    log("out of images")
    //    break
    //}
}

crawlState.state = "COMPLETED"
stateFile.withWriter { writer -> crawlState.writeTo(writer) }

return

//***********************************************************************
//Utility functions below here
//***********************************************************************
boolean saveData(imageDir, imageName, url, retry=0) {
    try {
        def fst = new File(imageDir, imageName).newOutputStream()
		def ist = new URL(url).openStream()
        fst << ist
        fst.close()
		ist.close()
    } catch (Exception e) {
        log(e)
		if (retry > crawlConf.crawler.maxRetries) {
		    log("max retries reached")
            return false
        }
        log("Retrying data download")
        Thread.sleep(crawlConf.crawler.apihitfreq)
        return saveData(imageDir, imageName, url, retry+1)
    }
    return true
}

synchronized void log(str) {
    if (logger)
        logger.append(new Date().toString() + "\t" + str + "\n")
}

boolean saveImageData(imageDir, imageName, url, retry=0) {
    try {
        def file = new File(imageDir, imageName)
		def buffer = new URL(url).getBytes()
		if (buffer.length < 5000) 
		    return false //got not avail image
        file << buffer
    } catch (Exception e) {
        log(e)
		if (retry > crawlConf.crawler.maxRetries) {
		    log("max retries reached")
            return false
        }
        log("Retrying image download")
        Thread.sleep(crawlConf.crawler.hitfreq)
        return saveImageData(imageDir, imageName, url, retry+1)
    }
    return true
}

boolean saveImage(imageDir, r, size, shortname=false) {
    if (!r.originalSecret) r.originalSecret = r.secret //stop it falling over if originalSecret is null
    
    if (shortname) {
        if (size == "original") return saveImageData(imageDir, "${r.id}.${r.originalFormat}", r.getOriginalUrl())
        if (size == "large") return saveImageData(imageDir, "${r.id}.jpg", r.getLargeUrl())
        if (size == "medium") return saveImageData(imageDir, "${r.id}.jpg", r.getMediumUrl())
        if (size == "small") return saveImageData(imageDir, "${r.id}.jpg", r.getSmallUrl())
        if (size == "thumbnail") return saveImageData(imageDir, "${r.id}.jpg", r.getThumbnailUrl())
        if (size == "smallSquare") return saveImageData(imageDir, "${r.id}.jpg", r.getSmallSquareUrl())
    } else {
        if (size == "original") return saveImageData(imageDir, "${r.id}_${r.originalSecret}_o.${r.originalFormat}", r.getOriginalUrl())
        if (size == "large") return saveImageData(imageDir, "${r.id}_${r.secret}_b.jpg", r.getLargeUrl())
        if (size == "medium") return saveImageData(imageDir, "${r.id}_${r.secret}.jpg", r.getMediumUrl())
        if (size == "small") return saveImageData(imageDir, "${r.id}_${r.secret}_m.jpg", r.getSmallUrl())
        if (size == "thumbnail") return saveImageData(imageDir, "${r.id}_${r.secret}_t.jpg", r.getThumbnailUrl())
        if (size == "smallSquare") return saveImageData(imageDir, "${r.id}_${r.secret}_s.jpg", r.getSmallSquareUrl())        
    }
}

def processSizes(sizes) {
	def out = [] as Set
	for (t in sizes) {
		if (t.label == 0) out << "thumbnail"
		if (t.label == 1) out << "smallSquare"
		if (t.label == 2) out << "small"
		if (t.label == 3) out << "medium"
		if (t.label == 4) out << "large"
		if (t.label == 5) out << "original"
	}
	return out
}

def makeCSV(r, imageDir) {
    String csv = ""
    csv += String.format("%s, ", escapeCSV(r.farm))
    csv += String.format("%s, ", escapeCSV(r.server))
    csv += String.format("%s, ", escapeCSV(r.id))
    csv += String.format("%s, ", escapeCSV(r.secret))
    csv += String.format("%s, ", escapeCSV(r.originalSecret))
    csv += String.format("%s, ", escapeCSV(r.mediumUrl))
    csv += String.format("%s, ", escapeCSV(imageDir))
    csv += String.format("%s, ", escapeCSV(r.title))
    csv += String.format("%s, ", escapeCSV(r.description))
    csv += String.format("%s, ", escapeCSV(r.license))
    csv += String.format("%s, ", escapeCSV(r.datePosted))
    csv += String.format("%s, ", escapeCSV(r.dateTaken))
    csv += String.format("%s, ", escapeCSV(r.owner?.id))
    csv += String.format("%s, ", escapeCSV(r.owner?.username))
    csv += String.format("%s, ", escapeCSV(r.geoData?.accuracy))
    csv += String.format("%s, ", escapeCSV(r.geoData?.latitude))
    csv += String.format("%s, ", escapeCSV(r.geoData?.longitude))
    csv += String.format("%s, ", escapeCSV(r.tags?.value))
    csv += "\n"
    return csv
}

/*
 * CSV String Escape
 */
def escapeCSV(input) {
    if (!(input instanceof String)) input = input as String
    
    if (input == null) return ""
    if (input.contains(",") || input.contains("\n") || input.contains('"') || (!input.trim().equals(input))) {
        return '"' + input.replaceAll('"', '""') + '"'
    } else {
        return input
    }
}

/*
 * CSV String Unescape
 */
def unescapeCSV(input) {
    if (input == null) return input
    else if (input.length() < 2) return input
    else if (input.charAt(0) != '"' || input.charAt(input.length()-1) != '"') return input
    else {
        def quoteless = input.substring(1, input.length()-1)
        
        if (quoteless.contains(",") || quoteless.contains("\n") || quoteless.contains('"')) {
            quoteless = quoteless.replace('""', '"')
        }
        
        return quoteless
    }
}
