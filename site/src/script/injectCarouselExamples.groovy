/**
 * Script to patch slide examples into the carousel on the front page
 * so they can be written separately and kept clean
 */
File targetDir = new File(project.build.directory, "/site/");
File sourceDir = new File(project.basedir, "/site/src/site/static-html/examples");

File templateFile = new File(targetDir, "index.html");
String insertPoint = '<p>carousel_examples_go_here</p>';
String exampleText = ""
int nseen = 0
int maxslides = 3
sourceDir.eachFile { file ->
	if (file.isDirectory()) return;
	String classText = "item"
	if(nseen == 0){
		classText += " active"
	}
	exampleText += "<div class=\"" + classText + "\">" + file.text + "</div>\n";
	
	nseen+=1;
	if(nseen >= maxslides) return;
}
String newContent = templateFile.text.replace(insertPoint, exampleText);
templateFile.write(newContent);

