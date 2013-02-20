OpenIMAJ Tools
============

Introduction
----------------

OpenIMAJ provides a suite of command line tools which provide convenient access to core functionality in the OpenIMAJ library. As new features are added to the library, they will likely be exposed as tools in order to to allow easy integration and testing of implemented tools and techniques. The tools provide the ability to construct an entire image retrieval pipeline, that is: image download, feature extraction, codebook generation, quantisation and indexing. There are also hadoop versions of these tools which allow scalable versions of each task. We also provide some simple helper tools for feature/image visualisation.

## Installation
In the future we will release the tools as a separate binary release.

For the moment please follow the [OpenIMAJ From Source] instructions, and in the submodule directories of each tool you wish to use simple run:

~~~~
mvn assembly:assembly
~~~~

Now inside the target directory of the tool you will find a jar file which can be run with 

~~~~
java -jar target/NameOfTheTool.jar
~~~~

The tools will now display their command line options.

List of Tools
-------------

### General Tools

**GlobalFeatureTool**. Allows extraction of low-level global features from images. For example: colourfulness, sharpness, number of faces and average brightness. Also allows comparison of extracted features.

**LocalFeatureTool**. Allows extraction of local features using internal implementations of various detectors and descriptors. 

**ClusterQuantiserTool**. Provides methods for both creation and usage of clusters. Various kinds of clusters can be trained on various data sources and data sources can also be quantised using clusters. Currently random, random forest, RAC and KMeans (exact, approximate and hierarchical) clusters are supported. This tool can be used to create visual-term representations of images through SIFT features extracted with the LocalFeatureTool.

**FlickrCrawler**. A set of tools for the targeted downloading of images from Flickr in order to create experimental datasets.

### Hadoop Tools

**SequenceFileTool** Tool allowing the easy creation, examination and extraction of Hadoop sequence-files. Sequence-files are a form of archive file containing many smaller files, used by the Hadoop framework. The tool may be used to construct a sequence-file containing a large number of images as a precursor step to extracting image features.

**HadoopClusterQuantiserTool**. Multithreaded map-reduce implementation of vector quantisation. Given an existing quantiser definition (usually created by the ClusterQuantiserTool), Hadoop compute nodes load the quantiser into memory and quantise large volumes of data points in parallel.

**HadoopFastKMeans**. Iterative map-reduce implementation of the exact and approximate K-Means algorithms.

**HadoopGlobalFeaturesTool and HadoopLocalFeaturesTool**. Distributed feature extraction from large volumes of images.

**HadoopImageDownload**. Given a file containing a large number of URLs, download a the images in parallel. Used to download all the images from image-net for example.

**SequenceFileIndexer**. Construct ImageTerrier indexes using sequence-files as the source.
