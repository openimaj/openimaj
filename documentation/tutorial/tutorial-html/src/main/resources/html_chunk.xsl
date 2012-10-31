<?xml version="1.0" encoding="utf-8"?>
<!-- 
    This is the XSL HTML configuration file for the Spring Reference Documentation.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
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
    <xsl:param name="ignore.image.scaling">1</xsl:param>
    <xsl:param name="highlight.source">1</xsl:param>
    <!--###################################################
                      Table Of Contents
    ################################################### -->
    <!-- Generate the TOCs for named components only -->
    <xsl:param name="generate.toc">
        book toc,figure,example
        chapter toc
        part toc
        preface toc
        qandaset toc
    </xsl:param>
    <!-- Show only Sections up to level 2 in the TOCs -->
    <xsl:param name="toc.section.depth">3</xsl:param>
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

    </xsl:template>
    <xsl:template name="user.header.navigation">
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
                                        <span><xsl:text>Prev : </xsl:text><xsl:apply-templates select="$prev" mode="object.title.markup"/></span>
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
											<span>TOC</span>
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
									    <span>TOC</span>
                                </a></li>
                            </xsl:if>
                            <xsl:if test="count($next)>0">
                                <li class="last"><a accesskey="n">
                                        <xsl:attribute name="href">
                                            <xsl:call-template name="href.target">
                                                <xsl:with-param name="object" select="$next"/>
                                            </xsl:call-template>
                                        </xsl:attribute>
                                        <span><xsl:text>Next : </xsl:text><xsl:apply-templates select="$next" mode="object.title.markup"/></span>
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
                    <div id="nav-sub-block"> <a class="hide" name="nav-sub-a"></a> 
                        <ul id="nav-sub"> 
                            <xsl:if test="count($prev)>0">
                                <li class="first"><a accesskey="p">
                                        <xsl:attribute name="href">
                                            <xsl:call-template name="href.target">
                                                <xsl:with-param name="object" select="$prev"/>
                                            </xsl:call-template>
                                        </xsl:attribute>
                                        <span><xsl:text>Prev : </xsl:text><xsl:apply-templates select="$prev" mode="object.title.markup"/></span>
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
											<span>TOC</span>
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
									    <span>TOC</span>
                                </a></li>
                            </xsl:if>
                            <xsl:if test="count($next)>0">
                                <li class="last"><a accesskey="n">
                                        <xsl:attribute name="href">
                                            <xsl:call-template name="href.target">
                                                <xsl:with-param name="object" select="$next"/>
                                            </xsl:call-template>
                                        </xsl:attribute>
                                        <span><xsl:text>Next : </xsl:text><xsl:apply-templates select="$next" mode="object.title.markup"/></span>
                                </a></li>
                            </xsl:if>
                        </ul> 
                    </div> 
                </xsl:if>
            </xsl:if>
        </xsl:if>
    </xsl:template>
    
    
    <!--  Getting Rid of the Body Attributes - TOB -->
    <xsl:template name="body.attributes">
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

      <xsl:call-template name="header.navigation">
        <xsl:with-param name="prev" select="$prev"/>
        <xsl:with-param name="next" select="$next"/>
        <xsl:with-param name="nav.context" select="$nav.context"/>
      </xsl:call-template>
    
      <xsl:call-template name="user.header.content"/>

      <xsl:copy-of select="$content"/>

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
