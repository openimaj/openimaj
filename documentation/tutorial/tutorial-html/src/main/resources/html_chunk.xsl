<?xml version="1.0" encoding="utf-8"?>
<!-- 
    This is the XSL HTML configuration file for the Spring Reference Documentation.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
								xmlns:d="http://docbook.org/ns/docbook"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version="1.0">

    <xsl:import href="urn:docbkx:stylesheet"/>
    <!--###################################################
                     HTML Settings
    ################################################### -->
    <xsl:param name="chunk.section.depth">'2'</xsl:param>
	<xsl:param name="chunker.output.indent">yes</xsl:param>
    <xsl:param name="use.id.as.filename">'1'</xsl:param>
    <!-- These extensions are required for table printing and other stuff -->
    <xsl:param name="use.extensions">1</xsl:param>
    <xsl:param name="tablecolumns.extension">0</xsl:param>
    <xsl:param name="callout.extensions">1</xsl:param>
    <xsl:param name="callout.graphics.path">../images/callouts/</xsl:param>
    <xsl:param name="admon.graphics.path">../images/</xsl:param>
    <xsl:param name="graphicsize.extension">0</xsl:param>
    <xsl:param name="ignore.image.scaling">0</xsl:param>
    <xsl:param name="highlight.source">1</xsl:param>
    <!--###################################################
                      Table Of Contents
    ################################################### -->
    <!-- Generate the TOCs for named components only -->
    <xsl:param name="generate.toc">
        book toc,title
        part toc
        preface toc
        qandaset toc
    </xsl:param>
    <!-- Show only Sections up to level 2 in the TOCs -->
    <xsl:param name="toc.section.depth">2</xsl:param>
		<xsl:param name="toc.max.depth">2</xsl:param>
    <!--###################################################
                         Labels
    ################################################### -->
    <!-- Label Chapters and Sections (numbering) -->
    <xsl:param name="chapter.autolabel">1</xsl:param>
    <xsl:param name="section.autolabel" select="1"/>
    <xsl:param name="section.label.includes.component.label" select="1"/>
    <!--###################################################
                         Callouts
    ################################################### -->
    <!-- Place callout marks at this column in annotated areas -->
    <xsl:param name="callout.graphics">1</xsl:param>
    <xsl:param name="callout.defaultcolumn">90</xsl:param>
    <!--###################################################
                          Misc
    ################################################### -->

		<xsl:template name="book.titlepage.verso">
			<div class="cover">
				<a href="../tutorial-pdf.pdf">
					<img>
						<xsl:attribute name="src">
							<xsl:value-of select="//d:mediaobject[@role='cover']/d:imageobject[@role='front-large']/d:imagedata/@fileref" />
						</xsl:attribute>
					</img>
				</a>
			</div>
		</xsl:template>

    <!-- Placement of titles -->
    <xsl:param name="formal.title.placement">
        figure after
        example before
        equation before
        table before
        procedure before
    </xsl:param>
    <xsl:template match="author" mode="titlepage.mode">
        <div class="{name(.)}">
            <xsl:call-template name="person.name"/> 
            <!-- (<xsl:value-of select="affiliation"/> -->
            <xsl:apply-templates mode="titlepage.mode" select="./contrib"/>
        </div>
    </xsl:template>
    <xsl:template match="editor" mode="titlepage.mode">
        <div class="{name(.)}">
            <xsl:call-template name="person.name"/> 
            <!-- (<xsl:value-of select="affiliation"/> -->
            <xsl:apply-templates mode="titlepage.mode" select="./contrib"/>
        </div>
    </xsl:template>
    <xsl:template match="authorgroup" mode="titlepage.mode">
        <div class="{name(.)}">
            <h2>Authors</h2>
            <p/>
            <xsl:apply-templates mode="titlepage.mode"/>
        </div>
    </xsl:template>
    <!--###################################################
                     Headers and Footers
    ################################################### -->
    <xsl:template name="user.head.content">
			<meta property="fb:admins" content="286108206" />
			<meta property="og:type" content="website" />
			<link rel="image_src" href="../images/OpenImaj-sq.png" />
			<meta property="og:image" content="http://openimaj.org/images/OpenImaj-sq.png" />
			<meta property="og:title" content="OpenIMAJ: Open Intelligent Multimedia Analysis" />
			<meta property="og:url" content="http://www.openimaj.org" />
			<meta property="og:description" content="OpenIMAJ is an award-winning set of libraries and tools for multimedia content analysis and content generation." />
			<script type="text/javascript" src="./js/apache-maven-fluido-1.3.0.min.js"></script>
			<script type="text/javascript">
	      var _gaq = _gaq || [];
	      _gaq.push(['_setAccount', 'UA-38338744-1']);
	      _gaq.push(['_trackPageview']);

	      (function() {
	        var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
	        ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
	        var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
	      })();
	    </script>
    </xsl:template>

    <xsl:template name="user.header.navigation">
			<div id="topbar" class="navbar navbar-fixed-top navbar-inverse">
				<div class="navbar-inner">
					<div class="container">
						<div class="nav-collapse">
							<a class="brand" href="../index.html" title="OpenIMAJ">
								<img src="images/logo-tiny.png" alt="OpenIMAJ" />
              </a>
							<ul class="nav">
								<li class="dropdown">
									<a href="#" class="dropdown-toggle" data-toggle="dropdown">Overview <b class="caret"/></a>
									<ul class="dropdown-menu">
										<li>
											<a href="../index.html" title="Introduction">Introduction</a>
										</li>
										<li>
											<a href="../sponsors.html" title="History &amp; Sponsors">History &amp; Sponsors</a>
										</li>
										<li>
											<a href="../contact.html" title="Support &amp; Contacts">Support &amp; Contacts</a>
										</li>
										<li>
											<a href="team.html"  title="Team">Team</a>
										</li>										
										<li>
											<a href="http://blogs.ecs.soton.ac.uk/multimedia" title="The Blog">The Blog</a>
										</li>
										<li>
											<a href="http://www.sf.net/p/openimaj/code" title="Source Code">Source Code</a>
										</li>
										<li class="dropdown-submenu">
											<a href="../#related" title="Related Projects">Related Projects</a>
											<ul class="dropdown-menu">
												<li>
													<a href="http://www.imageterrier.org/" title="ImageTerrier">ImageTerrier</a>
												</li>
											</ul>
										</li>
									</ul>
								</li>
								<li class="dropdown">
									<a href="#" class="dropdown-toggle" data-toggle="dropdown">Documentation <b class="caret"/></a>
									<ul class="dropdown-menu">
										<li class="dropdown-submenu">
											<a href="../#gettingstarted" title="Getting Started">Getting Started</a>
											<ul class="dropdown-menu">
												<li>
													<a href="../tutorial-pdf.pdf" title="The Tutorial (PDF)">The Tutorial (PDF)</a>
												</li>
												<li>
													<a href="../tutorial/index.html" title="The Tutorial (HTML)">The Tutorial (HTML)</a>
												</li>
											</ul>
										</li>
										<li class="dropdown-submenu">
											<a href="../#using" title="Using OpenIMAJ">Using OpenIMAJ</a>
											<ul class="dropdown-menu">
												<li>
													<a href="../BuildFromSource.html" title="Building OpenIMAJ from Source">Building OpenIMAJ from Source</a>
												</li>
												<li>
													<a href="../UseLibrary.html" title="Using OpenIMAJ as a Library">Using OpenIMAJ as a Library</a>
												</li>
												<li>
													<a href="../Groovy.html" title="Using OpenIMAJ with Groovy">Using OpenIMAJ with Groovy</a>
												</li>
												<li>
													<a href="../tools.html"  title="OpenIMAJ commandline tools introduction">OpenIMAJ commandline tools introduction</a>
												</li>
												<li>
													<a href="../flickrCrawler.html"  title="The FlickrCrawler Tool">The FlickrCrawler Tool</a>
												</li>
												<li>
													<a href="../bibliography.html"  title="Bibliography">Bibliography</a>
												</li>
											</ul>
										</li>
										<li>
											<a href="../apidocs/index.html" title="API Reference">API Reference</a>
										</li>
										<li class="dropdown-submenu">
	       							<a href="#info" title="Project Information">Project Information</a>
						          <ul class="dropdown-menu">
												<li><a href="../plugin-management.html" title="Plugin Management">Plugin Management</a></li>
												<li><a href="../mail-lists.html" title="Mailing Lists">Mailing Lists</a></li>
												<li><a href="../integration.html" title="Continuous Integration">Continuous Integration</a></li>
												<li><a href="../license.html" title="Project License">Project License</a></li>
												<li><a href="../team-list.html" title="Project Team">Project Team</a></li>
												<li><a href="../source-repository.html" title="Source Repository">Source Repository</a></li>
												<li><a href="../index.html" title="About">About</a></li>
												<li><a href="../issue-tracking.html" title="Issue Tracking">Issue Tracking</a></li>
												<li><a href="../project-summary.html" title="Project Summary">Project Summary</a></li>
												<li><a href="../plugins.html" title="Project Plugins">Project Plugins</a></li>
												<li><a href="../dependency-convergence.html" title="Dependency Convergence">Dependency Convergence</a></li>
												<li><a href="../dependencies.html" title="Dependencies">Dependencies</a></li>
                      </ul>
				            </li>
									</ul>
								</li>
								<li class="dropdown">
									<a href="#" class="dropdown-toggle" data-toggle="dropdown">Modules <b class="caret"/></a>
									<ul class="dropdown-menu">
										<li>
											<a href="/openimaj-maven-archetypes/index.html" title="OpenIMAJ Maven Archetypes">OpenIMAJ Maven Archetypes</a>
										</li>
										<li>
											<a href="/openimaj-core-libs/index.html" title="OpenIMAJ Core Libraries">OpenIMAJ Core Libraries</a>
										</li>
										<li>
											<a href="/openimaj-image/index.html" title="OpenIMAJ Image Processing Libraries">OpenIMAJ Image Processing Libraries</a>
										</li>
										<li>
											<a href="/openimaj-video/index.html" title="OpenIMAJ Video Processing Libraries">OpenIMAJ Video Processing Libraries</a>
										</li>
										<li>
											<a href="/openimaj-audio/index.html" title="OpenIMAJ Audio Processing Libraries">OpenIMAJ Audio Processing Libraries</a>
										</li>
										<li>
											<a href="/openimaj-machine-learning/index.html" title="OpenIMAJ Machine Learning Subprojects">OpenIMAJ Machine Learning Subprojects</a>
										</li>
										<li>
											<a href="/openimaj-text/index.html" title="OpenIMAJ Text Analysis Subprojects">OpenIMAJ Text Analysis Subprojects</a>
										</li>
										<li>
											<a href="/thirdparty/index.html" title="OpenIMAJ Third Party Ported Libraries">OpenIMAJ Third Party Ported Libraries</a>
										</li>
										<li>
											<a href="/openimaj-demos/index.html" title="OpenIMAJ Demos Subproject">OpenIMAJ Demos Subproject</a>
										</li>
										<li>
											<a href="/openimaj-knowledge/index.html" title="OpenIMAJ Knowledge Representation and Reasoning Libraries">OpenIMAJ Knowledge Representation and Reasoning Libraries</a>
										</li>
										<li>
											<a href="/test-resources/index.html" title="OpenIMAJ Unit Test Resources">OpenIMAJ Unit Test Resources</a>
										</li>
										<li>
											<a href="/openimaj-tools/index.html" title="OpenIMAJ Tools">OpenIMAJ Tools</a>
										</li>
										<li>
											<a href="/openimaj-hadoop/index.html" title="OpenIMAJ Hadoop Subproject">OpenIMAJ Hadoop Subproject</a>
										</li>
										<li>
											<a href="/openimaj-storm/index.html" title="OpenIMAJ Storm Subproject">OpenIMAJ Storm Subproject</a>
										</li>
										<li>
											<a href="/openimaj-web/index.html" title="OpenIMAJ web subproject">OpenIMAJ web subproject</a>
										</li>
										<li>
											<a href="/openimaj-hardware/index.html" title="OpenIMAJ Hardware Subprojects">OpenIMAJ Hardware Subprojects</a>
										</li>
										<li>
											<a href="/openimaj-content-libs/index.html" title="OpenIMAJ Content Creation Libraries">OpenIMAJ Content Creation Libraries</a>
										</li>
										<li>
											<a href="/openimaj-ide-integration/index.html" title="OpenIMAJ IDE Integration Plugins">OpenIMAJ IDE Integration Plugins</a>
										</li>
										<li>
											<a href="/openimaj-documentation/index.html" title="OpenIMAJ Documentation">OpenIMAJ Documentation</a>
										</li>
									</ul>
								</li>
							</ul>
							<form id="search-form" action="http://www.google.com/search" method="get" class="navbar-search pull-right" name="search-form">
								<input value="" name="sitesearch" type="hidden"></input> <input class="search-query" name="q" id="query" type="text"></input>
							</form><script type="text/javascript" src="http://www.google.com/coop/cse/brand?form=search-form">
			</script> <iframe src="http://www.facebook.com/plugins/like.php?href=http://www.openimaj.org/&amp;send=false&amp;layout=button_count&amp;show-faces=false&amp;action=like&amp;colorscheme=dark" scrolling="no" frameborder="0" style="border:none; width:80px; height:20px; margin-top: 10px;" class="pull-right"></iframe> <script type="text/javascript" src="https://apis.google.com/js/plusone.js">
			</script>
							<ul class="nav pull-right">
								<li style="margin-top: 10px;">
									<div class="g-plusone" data-href="http://www.openimaj.org/" data-size="medium" width="60px" align="right"></div>
								</li>
							</ul>
						</div>
					</div>
				</div>
			</div>
    </xsl:template>

    <!-- no other header navigation (prev, next, etc.) -->
    <xsl:template name="header.navigation">
        <xsl:param name="prev" select="/foo"/>
        <xsl:param name="next" select="/foo"/>
        <xsl:param name="nav.context"/>
        <xsl:variable name="home" select="/*[1]"/>
        <xsl:variable name="up" select="parent::*"/>
        <xsl:variable name="row1" select="count($prev) &gt; 0
                                        or count($up) &gt; 0
                                        or count($next) &gt; 0"/>
        <xsl:variable name="row2" select="($prev and $navig.showtitles != 0)
                                        or (generate-id($home) != generate-id(.)
                                            or $nav.context = 'toc')
                                        or ($chunk.tocs.and.lots != 0
                                        and $nav.context != 'toc')
                                        or ($next and $navig.showtitles != 0)"/>
        <xsl:if test="$suppress.navigation = '0' and $suppress.footer.navigation = '0'">
            <xsl:if test="$row1 or $row2">
                <xsl:if test="$row1">
                    <div id="nav-sub-block"> <a class="hide" name="nav-sub-a"></a> 
                        <ul id="nav-sub"> 
                            <xsl:if test="count($prev)>0">
                                <li class="first"><a accesskey="p">
                                        <xsl:attribute name="href">
                                            <xsl:call-template name="href.target">
                                                <xsl:with-param name="object" select="$prev"/>
                                            </xsl:call-template>
                                        </xsl:attribute>
                                        <span><xsl:text>Prev : </xsl:text><xsl:apply-templates select="$prev" mode="object.titleabbrev.markup"/></span>
                                </a></li>
                            </xsl:if> 
                            <xsl:choose>
                                <xsl:when test="$home != . or $nav.context = 'toc'">
                                    <li><a accesskey="h">
                                            <xsl:attribute name="href">
                                                <xsl:call-template name="href.target">
                                                    <xsl:with-param name="object" select="$home"/>
                                                </xsl:call-template>
                                            </xsl:attribute>
											<span>Contents</span>
                                    </a></li>
                                    <xsl:if test="$chunk.tocs.and.lots != 0 and $nav.context != 'toc'">
                                        <xsl:text>&#160;|&#160;</xsl:text>
                                    </xsl:if>
                                </xsl:when>
                                <xsl:otherwise>&#160;</xsl:otherwise>
                            </xsl:choose>                             
                            <xsl:if test="$chunk.tocs.and.lots != 0 and $nav.context != 'toc'">
                                <li><a accesskey="t">
                                        <xsl:attribute name="href">
                                            <xsl:apply-templates select="/*[1]" mode="recursive-chunk-filename">
                                                <xsl:with-param name="recursive" select="true()"/>
                                            </xsl:apply-templates>
                                            <xsl:text>-toc</xsl:text>
                                            <xsl:value-of select="$html.ext"/>
                                        </xsl:attribute>
									    <span>Contents</span>
                                </a></li>
                            </xsl:if>
                            <xsl:if test="count($next)>0">
                                <li class="last"><a accesskey="n">
                                        <xsl:attribute name="href">
                                            <xsl:call-template name="href.target">
                                                <xsl:with-param name="object" select="$next"/>
                                            </xsl:call-template>
                                        </xsl:attribute>
                                        <span><xsl:text>Next : </xsl:text><xsl:apply-templates select="$next" mode="object.titleabbrev.markup"/></span>
                                </a></li>
                            </xsl:if>
                        </ul> 
                    </div> 
                </xsl:if>
            </xsl:if>
        </xsl:if>
    </xsl:template>
    <xsl:param name="navig.showtitles">1</xsl:param>
    <xsl:template name="footer.navigation">
        <xsl:param name="prev" select="/foo"/>
        <xsl:param name="next" select="/foo"/>
        <xsl:param name="nav.context"/>
        <xsl:variable name="home" select="/*[1]"/>
        <xsl:variable name="up" select="parent::*"/>
        <xsl:variable name="row1" select="count($prev) &gt; 0
                                        or count($up) &gt; 0
                                        or count($next) &gt; 0"/>
        <xsl:variable name="row2" select="($prev and $navig.showtitles != 0)
                                        or (generate-id($home) != generate-id(.)
                                            or $nav.context = 'toc')
                                        or ($chunk.tocs.and.lots != 0
                                            and $nav.context != 'toc')
                                        or ($next and $navig.showtitles != 0)"/>
        <xsl:if test="$suppress.navigation = '0' and $suppress.footer.navigation = '0'">
            <xsl:if test="$footer.rule != 0">
            </xsl:if>
            <xsl:if test="$row1 or $row2">
                <xsl:if test="$row1">
                    <div id="nav-sub-block" class="bottom"> <a class="hide" name="nav-sub-a"></a> 
                        <ul id="nav-sub"> 
                            <xsl:if test="count($prev)>0">
                                <li class="first"><a accesskey="p">
                                        <xsl:attribute name="href">
                                            <xsl:call-template name="href.target">
                                                <xsl:with-param name="object" select="$prev"/>
                                            </xsl:call-template>
                                        </xsl:attribute>
                                        <span><xsl:text>Prev : </xsl:text><xsl:apply-templates select="$prev" mode="object.titleabbrev.markup"/></span>
                                </a></li>
                            </xsl:if> 
                            <xsl:choose>
                                <xsl:when test="$home != . or $nav.context = 'toc'">
                                    <li><a accesskey="h">
                                            <xsl:attribute name="href">
                                                <xsl:call-template name="href.target">
                                                    <xsl:with-param name="object" select="$home"/>
                                                </xsl:call-template>
                                            </xsl:attribute>
											<span>Contents</span>
                                    </a></li>
                                    <xsl:if test="$chunk.tocs.and.lots != 0 and $nav.context != 'toc'">
                                        <xsl:text>&#160;|&#160;</xsl:text>
                                    </xsl:if>
                                </xsl:when>
                                <xsl:otherwise>&#160;</xsl:otherwise>
                            </xsl:choose>                             
                            <xsl:if test="$chunk.tocs.and.lots != 0 and $nav.context != 'toc'">
                                <li><a accesskey="t">
                                        <xsl:attribute name="href">
                                            <xsl:apply-templates select="/*[1]" mode="recursive-chunk-filename">
                                                <xsl:with-param name="recursive" select="true()"/>
                                            </xsl:apply-templates>
                                            <xsl:text>-toc</xsl:text>
                                            <xsl:value-of select="$html.ext"/>
                                        </xsl:attribute>
									    <span>Contents</span>
                                </a></li>
                            </xsl:if>
                            <xsl:if test="count($next)>0">
                                <li class="last"><a accesskey="n">
                                        <xsl:attribute name="href">
                                            <xsl:call-template name="href.target">
                                                <xsl:with-param name="object" select="$next"/>
                                            </xsl:call-template>
                                        </xsl:attribute>
                                        <span><xsl:text>Next : </xsl:text><xsl:apply-templates select="$next" mode="object.titleabbrev.markup"/></span>
                                </a></li>
                            </xsl:if>
                        </ul> 
                    </div> 
                </xsl:if>
            </xsl:if>
        </xsl:if>
    </xsl:template>
    
		<xsl:template name="user.footer.navigation">
			<footer>
				<div class="container">
			  	<div class="row span12">Copyright © 2011-2014 <a href="http://www.soton.ac.uk">The University of Southampton</a>. All Rights Reserved.
  				</div>
					<p id="poweredBy" class="pull-right">
			  		<a href="http://maven.apache.org/" title="Built by Maven" class="poweredBy">
			    		<img class="builtBy" alt="Built by Maven" src="./images/logos/maven-feather.png" />
			    	</a>
		    	</p>
					<div id="ohloh" class="pull-right">
			      <script type="text/javascript" src="http://www.ohloh.net/p/openimaj/widgets/project_partner_badge.js"></script>
			    </div>
				</div>
   		</footer>
		</xsl:template>
    
    <!--  Getting Rid of the Body Attributes - TOB -->
    <xsl:template name="body.attributes">
			<xsl:attribute name="class">topBarEnabled</xsl:attribute>
    </xsl:template>
    	
		<xsl:template name="user.preroot">
			<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html></xsl:text>
		</xsl:template>
    	
    <!-- Customizing Overall Chunk Output -->
    <xsl:template name="chunk-element-content">
  <xsl:param name="prev"/>
  <xsl:param name="next"/>
  <xsl:param name="nav.context"/>
  <xsl:param name="content">
    <xsl:apply-imports/>
  </xsl:param>

  <xsl:call-template name="user.preroot"/>

  <html>
    <xsl:call-template name="html.head">
      <xsl:with-param name="prev" select="$prev"/>
      <xsl:with-param name="next" select="$next"/>
    </xsl:call-template>

    <body>
      <xsl:call-template name="body.attributes"/>
      <xsl:call-template name="user.header.navigation"/>

			<div class="container">
				<div id="banner">
					<div class="pull-left">
						<div id="bannerLeft">
				    	<h2>OpenIMAJ</h2>
				    </div>
					</div>
				  <div class="pull-right">  </div>
				  <div class="clear"><hr /></div>
				</div>

				<xsl:variable name="Version">
						<xsl:value-of select="//d:edition" />
		    </xsl:variable>
				<xsl:variable name="Date">
						<xsl:value-of select="//d:date" />
		    </xsl:variable>

				<div id="breadcrumbs">
				  <ul class="breadcrumb">
				    <li id="publishDate" class="pull-right">Last Published: <xsl:value-of select="$Date"/></li> <li class="divider pull-right">|</li>
				   	<li id="projectVersion" class="pull-right">Version: <xsl:value-of select="$Version"/></li>
				  </ul>
				</div>

      <xsl:call-template name="header.navigation">
        <xsl:with-param name="prev" select="$prev"/>
        <xsl:with-param name="next" select="$next"/>
        <xsl:with-param name="nav.context" select="$nav.context"/>
      </xsl:call-template>
    
      <xsl:call-template name="user.header.content"/>				
      	<xsl:copy-of select="$content"/>
			</div>
			
      <xsl:call-template name="user.footer.content"/>

      <xsl:call-template name="footer.navigation">
        <xsl:with-param name="prev" select="$prev"/>
        <xsl:with-param name="next" select="$next"/>
        <xsl:with-param name="nav.context" select="$nav.context"/>
      </xsl:call-template>

      <xsl:call-template name="user.footer.navigation"/>
    </body>
  </html>
  <xsl:value-of select="$chunk.append"/>
</xsl:template>
        
<xsl:template name="section.heading">
  <xsl:param name="section" select="."/>
  <xsl:param name="level" select="1"/>
  <xsl:param name="allow-anchors" select="1"/>
  <xsl:param name="title"/>
  <xsl:param name="class" select="'title'"/>

  <xsl:variable name="id">
    <xsl:choose>
      <!-- if title is in an *info wrapper, get the grandparent -->
      <xsl:when test="contains(local-name(..), 'info')">
        <xsl:call-template name="object.id">
          <xsl:with-param name="object" select="../.."/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="object.id">
          <xsl:with-param name="object" select=".."/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <!-- HTML H level is one higher than section level -->
  <xsl:variable name="hlevel">
    <xsl:choose>
      <!-- highest valid HTML H level is H6; so anything nested deeper
           than 5 levels down just becomes H6 -->
      <xsl:when test="$level &gt; 5">6</xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$level + 1"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:element name="h{$hlevel}">
    <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
    <xsl:if test="$css.decoration != '0'">
      <xsl:if test="$hlevel&lt;3">
        <xsl:attribute name="style">clear: both</xsl:attribute>
      </xsl:if>
    </xsl:if>
    <xsl:if test="$allow-anchors != 0">
      <xsl:call-template name="anchor">
        <xsl:with-param name="node" select="$section"/>
        <xsl:with-param name="conditional" select="0"/>
      </xsl:call-template>
    </xsl:if>
    <xsl:copy-of select="$title"/>
  </xsl:element>
</xsl:template>

</xsl:stylesheet>
