Building OpenIMAJ from Source
=============================

Introduction
------------
The OpenIMAJ framework was written with varying levels of interest and development use cases in mind. If you are looking for simplified functionality for a specific task, we recommend that you look at the [OpenIMAJ Tools](Tools.html) which details the set of tools that allow for command line access to much of the functionality in the OpenIMAJ library. If you want to properly integrate a stable version of OpenIMAJ into your java project as a library, please follow the instructions in [OpenIMAJ Library](UseLibrary.html) to see how to integrate the pre-built OpenIMAJ jar files into your existing projects. 

If you are trying to develop core OpenIMAJ functionality, hack around with its inner workings or simply compile OpenIMAJ from scratch, you are in the right place. The instructions for building OpenIMAJ on this page should work with any unix-like operating system, as well as with Windows.

The source for the project is available from our [Sourceforge svn repository](https://sourceforge.net/p/openimaj/code). If you already have subversion installed you can download the source using this command.

	> svn checkout svn://svn.code.sf.net/p/openimaj/code/trunk openimaj

The rest of these instructions concern themselves with building the project using a project management tool called [Apache Maven](http://maven.apache.org). These instructions were created by installing OpenIMAJ on a fresh install of Ubuntu. They include the process of installing java, maven, svn and eclipse leaving you with a working development copy of OpenIMAJ. The process is as verbose as possible to guarantee correct results. If any steps go wrong, or you have problems, please get in [contact](Contact.html).

Prerequisites - Java and Maven
------------------------------

### Java 1.6.0
The OpenIMAJ library is written to be compatible with Java 1.6 or later. **We make heavy use of complex Java generics which had some major bugs up until java 1.6.0_20**; therefore first thing which should be installed on your system is a version>=1.6.0_20 of the java jdk.

#### Installation

To install the latest version of the Java JDK please follow [the instructions found here](http://www.oracle.com/technetwork/java/javase/downloads/index.html). 

On **Ubuntu** you can install openjdk-6 (or openjdk-7 should you prefer) using the command: 

	> sudo apt-get install openjdk-6-jdk

Once this is complete you should be able to do:
	
	> java -version
	> javac -version

and get something along the lines of:

	> java -version
	java version "1.6.0_22"
	OpenJDK Runtime Environment (IcedTea6 1.10.1) (6b22-1.10.1-0ubuntu1)
	OpenJDK Client VM (build 20.0-b11, mixed mode, sharing)
	> javac -version
	javac 1.6.0_22


### Maven
Once Java is installed, you can install Maven. [Apache Maven](http://maven.apache.org) is a project management and build automation tool. The OpenIMAJ project uses Maven to manage the complete project lifecycle. Maven takes care of many things including the download and installation of all the dependencies of all OpenIMAJ sub projects. Maven also takes care of testing and generation of settings files to allow the import of OpenIMAJ projects into Eclipse. 

#### Installation
To install Maven on **Windows** please follow the instructions [here](http://maven.apache.org/download.html). 

On **MacOSX** 10.4+ Maven 2 or Maven 3 may already be installed, but you can always download it [here](http://maven.apache.org/download.html).

On **Ubuntu** you can install Maven 2 using:

	> sudo apt-get install maven2

Once Maven is properly installed, you should be able to do:

	> mvn -version

and get something along the lines of:

	> mvn -version
	Apache Maven 2.2.1 (rdebian-4)
	Java version: 1.6.0_22
	Java home: /usr/lib/jvm/java-6-openjdk/jre
	Default locale: en_GB, platform encoding: UTF-8
	OS name: "linux" version: "2.6.38-8-generic" arch: "i386" Family: "unix"
	
### Subversion
If everything went well so far you can install subversion. The current bleeding edge version of our source code can be found on our subversion repository. 

#### Installation

You can install subversion on **Windows** or any other environment by following your [development environment instructions here](http://subversion.apache.org/packages.html). 

On **MacOSX** 10.4+ subversion should come pre-installed

On **Ubuntu** you can install subversion using the command:

	> sudo apt-get install subversion

At this point you should be able to do:

	> svn --version

and get something along the lines of:

	> svn --version
	svn, version 1.6.12 (r955767)
	   compiled Mar 22 2011, 19:28:17

	Copyright (C) 2000-2009 CollabNet.
	Subversion is open source software, see http://subversion.tigris.org/
	This product includes software developed by CollabNet (http://www.Collab.Net/).

	The following repository access (RA) modules are available:

	* ra_neon : Module for accessing a repository via WebDAV protocol using Neon.
	  - handles 'http' scheme
	  - handles 'https' scheme
	* ra_svn : Module for accessing a repository using the svn network protocol.
	  - with Cyrus SASL authentication
	  - handles 'svn' scheme
	* ra_local : Module for accessing a repository on local disk.
	  - handles 'file' scheme

At this point you are ready to download the OpenIMAJ source code and prepare the project allowing for development and integration with other projects.

## Eclipse

The eclipse IDE was used to develop OpenIMAJ and we highly recommend it for any development you do. Other IDE's can of course be used however - please consult the Maven documentation to see how Maven integrates with your IDE of choice.

### Installation

For all operating systems the eclipse IDE can be downloaded from the [eclipse website](http://www.eclipse.org/downloads/). If your installing Eclipse for the first time, we recommend the **Eclipse IDE for Java Developers** version of eclipse.

Downloading and Installing
--------------------------

### Checking Out OpenIMAJ

To checkout a readonly version of the OpenIMAJ codebase you can use the command:

	> svn checkout svn://svn.code.sf.net/p/openimaj/code/trunk openimaj

By doing this a new directory will be created called openimaj. This directory is the root of the openimaj project. Inside this directory there are several sub directories containing the modules and sub-modules of the OpenIMAJ project. The dependencies can be explored by reading the `pom.xml` files in each directory. 


### Installing OpenIMAJ

To compile, test and install OpenIMAJ, navigate to the root of the project and run the maven command:

	> MAVEN_OPTS="-Xmx2G -XX:MaxPermSize=128" mvn install

The same command can be run within the directories of individual sub-projects for finer grain project compilation. From the root of the project, this may take some time, especially on the first run as this process will systematically download each dependency (and that dependencies dependencies, and so on) of each project and also run all the tests for each project. To install the projects and download all necessary jar files without running tests use:

	> MAVEN_OPTS="-Xmx2G -XX:MaxPermSize=128" mvn install -DskipTests

The `MAVEN_OPTS="-Xmx2G -XX:MaxPermSize=128"` increases the amount of memory that Maven is allowed to use (OpenIMAJ is a fairly large project and needs a bit more than the defaults). If you're building OpenIMAJ often, this can be added to your exported environment variables to save you having to re-type it every time you want to rebuild.

### OpenIMAJ and Eclipse

Once this process is complete, the OpenIMAJ project is compiled and installed. The jar files for each project can be found in their respective target directories. However, for more effective development you may want to import OpenIMAJ into eclipse. To allow for this use the command in the root directory:

	mvn eclipse:eclipse
	
This will create the appropriate Eclipse settings/classpath files for each project. From Eclipse, you can import all the OpenIMAJ modules by importing the root project directory. When importing it is often most useful to not copy the source files into your eclipse workspace, as this will mean any changes you make will be reflected in the OpenIMAJ hierarchy. 

**IMPORTANT** By default Eclipse doesn't know about maven and it's reposistories of jars. When you first import the OpenIMAJ projects into eclipse they will all have errors. You can fix this by added a new Java classpath variable (Eclipse>Preferences>Java>Build Path>Classpath Variables) called "M2_REPO". The value of this variable is the location of your .m2/repository directory. For unix systems this is usally found in your home directory, for windows systems it is found in Documents and Settings/&lt;your user&gt;

Once imported into Eclipse all submodules should correctly reference each other's Eclipse projects and therefore changes between sub-projects should be useable without the need for a Maven recompilation. 
