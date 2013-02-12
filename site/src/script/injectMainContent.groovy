/**
 * Script to insert the raw html content of the index.html page, circumventing some 
 * of the limitations of doxia.
 */
File targetDoc = new File(project.build.directory, "/site/index.html");
File sourceDoc = new File(project.basedir, "/site/src/site/resources/main.html");

if (!targetDoc.exists())
	fail("target doesn't exist!");

String targetContent = targetDoc.text;
String sourceContent = sourceDoc.text;
String insertPoint = '<p>content_goes_here</p>';

targetContent = targetContent.replace(insertPoint, sourceContent);

targetDoc.write(targetContent);
