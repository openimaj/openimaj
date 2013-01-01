@Grapes([
    @Grab('org.apache.maven:maven-model:3.0.4'),
    @Grab('org.apache.maven:maven-core:3.0.4')
])
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.FileReader;

MavenProject loadProject(File pomFile) {
    Model model = null;
    FileReader reader = null;
    MavenXpp3Reader mavenreader = new MavenXpp3Reader();
    try {
        reader = new FileReader(pomFile);
        model = mavenreader.read(reader);
        model.setPomFile(pomFile);
    } catch(Exception ex) {
        println ex;
        return null;
    }

    return new MavenProject(model);
}

void process(MavenProject p, int level, File baseDir) {
	println "<varlistentry>";
	println "<term><filename>" + (baseDir.name == "." ? "OpenIMAJ" : baseDir.name) + "</filename></term>";
	println "<listitem>";

    if (p.getDescription() != null)
		println "<para>" + p.getDescription().replaceAll("\\s+", " ") + "</para>";
	
    def modules = p.getModules();
    if (modules != null) {
		println "<variablelist>";
        modules.each { module ->
			File baseDir2 = new File(baseDir, module)
			MavenProject mp = loadProject(new File(baseDir2, "/pom.xml"));
            process(mp, level+1, baseDir2);
        }
		println "</variablelist>";
    }
	println "</listitem>";
	println "</varlistentry>";
}

File root = (args.length == 0 ? new File("./pom.xml") : new File(args[0]));

MavenProject project = loadProject(root);

println '<?xml version="1.0" encoding="UTF-8"?>';
println '<!DOCTYPE chapter PUBLIC "-//OASIS//DTD DocBook XML V4.5//EN" "http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd">';
println "<variablelist>";
process(project, 1, root.parentFile);
println "</variablelist>";

