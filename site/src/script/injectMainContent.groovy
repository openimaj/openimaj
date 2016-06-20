@GrabResolver(name='groovy-template', root='http://maven.openimaj.org')
@Grab('org.codehaus.groovy:groovy-streaming-template-engine:2.3.6.1')
import groovy.text.StreamingTemplateEngine

/**
 * Script to patch static html pages into the maven site template, circumventing some 
 * of the limitations of doxia.
 */
File targetDir = new File(project.build.directory, "/site/");
File sourceDir = new File(project.basedir, "/site/src/site/static-html");

File templateFile = new File(targetDir, "template.html");
String insertPoint = '<p>content_goes_here</p>';

if (!templateFile.exists())
	fail("template doesn't exist!");

String templateContent = templateFile.text;

sourceDir.eachFile { file ->
	if (file.isDirectory()) return;
	File newFile = new File(targetDir, file.name);
	File tFile = new File(targetDir, "template-"+file.name);
    String sourceContent = file.text;
    //sourceContent = Class.forName("groovy.text.StreamingTemplateEngine").newInstance().createTemplate(sourceContent).make().writeTo(new StringWriter()).toString();
    sourceContent = new groovy.text.StreamingTemplateEngine().createTemplate(sourceContent).make(binding.variables).writeTo(new StringWriter()).toString();

	thisTemplateContent = templateContent;
	if (tFile.exists()) {
		thisTemplateContent = tFile.text;
	}

    String newContent = thisTemplateContent.replace(insertPoint, sourceContent);

    newFile.write(newContent);
}

