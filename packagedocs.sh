#!/bin/sh
if `which jsgrep` then
	echo "Found JSGrep"
else
	echo "jsgrep not found, to compile the generated javadocs together you need jsgrep. You can install jsgrep by following instructions on http://github.org/sinjax/jsgrep"
fi
rm -rf $1
mkdir -p $1

echo "<html><head><title>OpenIMAJ Javadoc index</title></head><body>" >> $1/index.html

echo "<h1>Introduction</h1>" >> $1/index.html
echo "<p>Below are all the javadocs of the OpenIMAJ project (including that of the tools)</p>" >> $1/index.html

echo "<ul>" >> $1/index.html
for X in `find . -iname apidocs`; 
do
	if [ -e $X/index.html ]
		then
		echo `echo $X | sed s/".[/]\(.*\)[/]target.*$"/"mkdir -p $1\/\1"/g` | sh;
		echo `echo $X | sed s/".[/]\(.*\)[/]target.*$"/"cp -rf &\/ $1\/\1"/g` | sh;
		echo `echo $X | sed s/".[/]\(.*\)[/]target.*$"/"<li><\a href=\"\1\/index.html\">\1<\/a>"/g` >> $1/index.html;
		echo "<p>" >> $1/index.html
		echo `echo $X | sed s/".[/]\(.*\)[/]target.*$"/"cat \1\/pom.xml | jsgrep -s description -nt"/g` | sh >> $1/index.html;
		echo "</p>" >> $1/index.html
		echo "</li>" >> $1/index.html
	fi
done
echo "</ul>" >> $1/index.html

echo "</body>" >> $1/index.html
