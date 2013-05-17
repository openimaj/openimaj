copy = { File src,File dest-> 
 
	def input = src.newDataInputStream()
	def output = dest.newDataOutputStream()
 
	output << input 
 
	input.close()
	output.close()
}
File assembledJar = new File(project.basedir, "/target/openimaj-processing.jar");
File libraryProp = new File(project.basedir, "/src/script/library.properties");
File tmpDir = new File(project.basedir, "/target/openimaj-processing.tmp");
File rootOut = new File(tmpDir, "/openimaj_processing");
File processingRoot = new File(project.properties.processingRoot,"openimaj_processing")

new File(rootOut, "/library").mkdirs();
new File(rootOut, "/reference").mkdirs();
new File(rootOut, "/examples").mkdirs();
new File(rootOut, "/src").mkdirs();

def ant = new AntBuilder()  

outjar = new File(rootOut, "/library/openimaj_processing.jar")
ant.copy( file:assembledJar, 
                       tofile:outjar)
outprop = new File(rootOut, "/library.properties")
ant.copy( file:libraryProp, tofile:outprop)

File zipFileName = new File(project.basedir, "/target/openimaj_processing.zip")

ant.zip(destfile:zipFileName,basedir:tmpDir)
ant.delete(dir:processingRoot,failonerror:false)
ant.copy(todir: processingRoot) {
    fileset(dir : rootOut)
}


ant.delete(dir:tmpDir,failonerror:false)  
