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
  * DownloadMissingImages.groovy
  * Use this to download any images mentioned in an images.csv from a crawl that 
  * were not downloaded for any reason (i.e. network problems, etc). On the 
  * command-line just pass the path to the crawl directory (outputdir in the 
  * FlickrCrawler configuration) and it will do the rest.
  * 
  * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
 
if (args.length < 1) {
    println "Usage: groovy DownloadMissingImages.groovy crawlDir"
    return
}

def crawlDir = new File(args[0])
 
def inputcsv = new File(crawlDir, "images.csv")
def imgdir = crawlDir.parent

def csvregex = ',(?=(?:[^\"]*\"[^\"]*\")*(?![^\"]*\"))'

int count1 = 0, count2 = 0
inputcsv.eachLine {
    def parts
    def imgfile
    try {
        parts = it.split(csvregex)
        imgfile = new File(imgdir, unescapeCSV(parts[6]).trim())
        imgfile = new File(imgfile, imgfile.getName()+".jpg")
    } catch (Exception e) {
        println "error" + e
        println it
        System.exit(1)
    }
        
    if (!imgfile.exists()) {
        println "missing " + unescapeCSV(parts[5])
        try {
            imgfile << new URL(unescapeCSV(parts[5])).bytes
        } catch (Exception e) {
            println "error" + e
            println it
        }
        count2++
    }
    count1++
    
    print "$count1\r"
}

def unescapeCSV(input) {
    if (input == null) input
    else if (input.length() < 2) input
    else if (input.charAt(0) != '"' || input.charAt(input.length()-1) != '"') input
    else {
        def quoteless = input.substring(1, input.length()-1)
        
        if (quoteless.contains(",") || quoteless.contains("\n") || quoteless.contains('"')) {
            quoteless = quoteless.replace('""', '"')
        }
        
        quoteless
    }
}
