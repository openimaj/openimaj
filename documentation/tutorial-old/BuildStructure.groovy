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
    println "." + level + " " + (baseDir.name == "." ? "OpenIMAJ" : baseDir.name) + "\\DTcomment{\\begin{minipage}[t]{\\descwidth}";
    if (p.getDescription() != null)
		println p.getDescription().replaceAll("\\s+", " ");
	println "\\end{minipage}}."
	
    def modules = p.getModules();
    
    if (modules != null) {
        modules.each { module ->
			File baseDir2 = new File(baseDir, module)
			MavenProject mp = loadProject(new File(baseDir2, "/pom.xml"));
            process(mp, level+1, baseDir2);
        }
    }
}

File root = (args.length == 0 ? new File("./pom.xml") : new File(args[0]));

MavenProject project = loadProject(root);

/*println "\\begin{figure*}[h!]"*/
println "\\renewcommand*\\DTstylecomment{\\rmfamily{ }{ }}"
println "\\newcommand{\\descwidth}{8.0cm}"
println "\\DTsetlength{0.2em}{0.4em}{0.2em}{0.4pt}{1.6pt}"
println "\\dirtree{%"
process(project, 1, root.parentFile);
println "}"
/*println "\\end{figure*}"*/
