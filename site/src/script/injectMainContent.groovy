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
    String sourceContent = file.text;

    String newContent = templateContent.replace(insertPoint, sourceContent);
    File newFile = new File(targetDir, file.name);

    newFile.write(newContent);
}

