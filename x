Apache Maven 3.0.4 (r1232337; 2012-01-17 03:44:56-0500)
Maven home: c:\app\apache-maven-3.0.4\bin\..
Java version: 1.6.0_33, vendor: Sun Microsystems Inc.
Java home: c:\app\jdk1.6.0_33\jre
Default locale: en_US, platform encoding: Cp1252
OS name: "windows 7", version: "6.1", arch: "amd64", family: "windows"
[INFO] Error stacktraces are turned on.
[DEBUG] Reading global settings from c:\app\apache-maven-3.0.4\bin\..\conf\settings.xml
[DEBUG] Reading user settings from c:\dev\crcsim\conf\settings.xml
[DEBUG] Using local repository at C:\Users\scox\.m2\repository
[DEBUG] Using manager EnhancedLocalRepositoryManager with priority 10 for C:\Users\scox\.m2\repository
[INFO] Scanning for projects...
[DEBUG] Extension realms for project epi:population:jar:1.0-SNAPSHOT: (none)
[DEBUG] Looking up lifecyle mappings for packaging jar from ClassRealm[plexus.core, parent: null]
[DEBUG] Resolving plugin prefix scala from [org.apache.maven.plugins, org.codehaus.mojo]
[DEBUG] Resolved plugin prefix scala to org.scala-tools:maven-scala-plugin from POM epi:population:jar:1.0-SNAPSHOT
[DEBUG] === REACTOR BUILD PLAN ================================================
[DEBUG] Project: epi:population:jar:1.0-SNAPSHOT
[DEBUG] Tasks:   [scala:script]
[DEBUG] Style:   Regular
[DEBUG] =======================================================================
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building epi-population 1.0-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[DEBUG] Resolving plugin prefix scala from [org.apache.maven.plugins, org.codehaus.mojo]
[DEBUG] Resolved plugin prefix scala to org.scala-tools:maven-scala-plugin from POM epi:population:jar:1.0-SNAPSHOT
[DEBUG] Lifecycle default -> [validate, initialize, generate-sources, process-sources, generate-resources, process-resources, compile, process-classes, generate-test-sources, process-test-sources, generate-test-resources, process-test-resources, test-compile, process-test-classes, test, prepare-package, package, pre-integration-test, integration-test, post-integration-test, verify, install, deploy]
[DEBUG] Lifecycle clean -> [pre-clean, clean, post-clean]
[DEBUG] Lifecycle site -> [pre-site, site, post-site, site-deploy]
[DEBUG] === PROJECT BUILD PLAN ================================================
[DEBUG] Project:       epi:population:1.0-SNAPSHOT
[DEBUG] Dependencies (collect): []
[DEBUG] Dependencies (resolve): [runtime]
[DEBUG] Repositories (dependencies): [com.springsource.repository.bundles.release (http://repository.springsource.com/maven/bundles/release, releases+snapshots), com.springsource.repository.bundles.external (http://repository.springsource.com/maven/bundles/external, releases+snapshots), com.springsource.repository.bundles.milestone (http://repository.springsource.com/maven/bundles/milestone, releases+snapshots), com.springsource.repository.bundles.snapshot (http://repository.springsource.com/maven/bundles/snapshot, releases+snapshots), repository.springframework.maven.release (http://maven.springframework.org/release, releases+snapshots), repository.springframework.maven.milestone (http://maven.springframework.org/milestone, releases+snapshots), repository.springframework.maven.snapshot (http://maven.springframework.org/snapshot, releases+snapshots), central (http://repo1.maven.org/maven2, releases+snapshots), maven2-repository.dev.java.net (http://download.java.net/maven/2, releases+snapshots), osgeo (http://download.osgeo.org/webdav/geotools/, releases+snapshots)]
[DEBUG] Repositories (plugins)     : [central (http://repo1.maven.org/maven2, releases+snapshots), scala-tools.org (http://scala-tools.org/repo-releases, releases+snapshots)]
[DEBUG] -----------------------------------------------------------------------
[DEBUG] Goal:          org.scala-tools:maven-scala-plugin:2.15.2:script (default-cli)
[DEBUG] Style:         Regular
[DEBUG] Configuration: <?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <addToClasspath>c:\dev\crcsim\common\population\target/classes</addToClasspath>
  <checkMultipleScalaVersions default-value="true">false</checkMultipleScalaVersions>
  <displayCmd default-value="false">${displayCmd}</displayCmd>
  <excludeScopes>${maven.scala.excludeScopes}</excludeScopes>
  <failOnMultipleScalaVersions default-value="false"/>
  <forceUseArgFile default-value="false"/>
  <fork default-value="true"/>
  <includeScopes>${maven.scala.includeScopes}</includeScopes>
  <keepGeneratedScript default-value="false">${maven.scala.keepGeneratedScript}</keepGeneratedScript>
  <localRepo>${localRepository}</localRepo>
  <localRepository>${localRepository}</localRepository>
  <outputDir>${project.build.directory}</outputDir>
  <project>${project}</project>
  <remoteRepos>${project.remoteArtifactRepositories}</remoteRepos>
  <removeFromClasspath>${removeFromClasspath}</removeFromClasspath>
  <scalaClassName default-value="scala.tools.nsc.Main">${maven.scala.className}</scalaClassName>
  <scalaVersion>2.10.0-M7</scalaVersion>
  <script>import org.renci.epi.population.PopulationMain
	    println (&quot;Hey&quot;)
	    try {
  	       val app = new PopulationMain 
	       app.compileModelInput ()
	    } catch {
	       case e: Exception =&gt; 
                  e.printStackTrace ()
            }</script>
  <scriptFile>${scriptFile}</scriptFile>
  <session>${session}</session>
</configuration>
[DEBUG] =======================================================================
[DEBUG] Skipped remote update check for epi:util:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:util:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:util:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:util:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:util:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:util:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:util:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:util:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:util:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Using connector WagonRepositoryConnector with priority 0 for http://repo1.maven.org/maven2
Downloading: http://repo1.maven.org/maven2/epi/util/1.0-SNAPSHOT/maven-metadata.xml
[DEBUG] Writing resolution tracking file C:\Users\scox\.m2\repository\epi\util\1.0-SNAPSHOT\resolver-status.properties
[DEBUG] Could not find metadata epi:util:1.0-SNAPSHOT/maven-metadata.xml in central (http://repo1.maven.org/maven2)
[DEBUG] Skipped remote update check for epi:util:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:util:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:util:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:util:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:util:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:util:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:util:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:util:1.0-SNAPSHOT/maven-metadata.xml, already updated during this session.
[DEBUG] Failure to find epi:util:1.0-SNAPSHOT/maven-metadata.xml in http://repo1.maven.org/maven2 was cached in the local repository, resolution will not be reattempted until the update interval of central has elapsed or updates are forced
[DEBUG] Skipped remote update check for epi:util:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:util:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:common:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:common:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:common:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:common:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:common:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:common:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:common:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:common:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:common:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Using connector WagonRepositoryConnector with priority 0 for http://repo1.maven.org/maven2
Downloading: http://repo1.maven.org/maven2/epi/common/1.0-SNAPSHOT/maven-metadata.xml
[DEBUG] Writing resolution tracking file C:\Users\scox\.m2\repository\epi\common\1.0-SNAPSHOT\resolver-status.properties
[DEBUG] Could not find metadata epi:common:1.0-SNAPSHOT/maven-metadata.xml in central (http://repo1.maven.org/maven2)
[DEBUG] Skipped remote update check for epi:root:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:root:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:root:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:root:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:root:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:root:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:root:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:root:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:root:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Using connector WagonRepositoryConnector with priority 0 for http://repo1.maven.org/maven2
Downloading: http://repo1.maven.org/maven2/epi/root/1.0-SNAPSHOT/maven-metadata.xml
[DEBUG] Writing resolution tracking file C:\Users\scox\.m2\repository\epi\root\1.0-SNAPSHOT\resolver-status.properties
[DEBUG] Could not find metadata epi:root:1.0-SNAPSHOT/maven-metadata.xml in central (http://repo1.maven.org/maven2)
[DEBUG] Skipped remote update check for epi:geography:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:geography:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:geography:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:geography:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:geography:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:geography:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:geography:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:geography:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:geography:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Using connector WagonRepositoryConnector with priority 0 for http://repo1.maven.org/maven2
Downloading: http://repo1.maven.org/maven2/epi/geography/1.0-SNAPSHOT/maven-metadata.xml
[DEBUG] Writing resolution tracking file C:\Users\scox\.m2\repository\epi\geography\1.0-SNAPSHOT\resolver-status.properties
[DEBUG] Could not find metadata epi:geography:1.0-SNAPSHOT/maven-metadata.xml in central (http://repo1.maven.org/maven2)
[DEBUG] Skipped remote update check for epi:geography:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:geography:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:geography:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:geography:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:geography:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:geography:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:geography:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:geography:1.0-SNAPSHOT/maven-metadata.xml, already updated during this session.
[DEBUG] Failure to find epi:geography:1.0-SNAPSHOT/maven-metadata.xml in http://repo1.maven.org/maven2 was cached in the local repository, resolution will not be reattempted until the update interval of central has elapsed or updates are forced
[DEBUG] Skipped remote update check for epi:geography:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:geography:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] epi:population:jar:1.0-SNAPSHOT
[DEBUG]    epi:util:jar:1.0-SNAPSHOT:compile
[DEBUG]       org.springframework:spring-webmvc:jar:3.1.2.RELEASE:compile
[DEBUG]          org.springframework:spring-context-support:jar:3.1.2.RELEASE:compile
[DEBUG]          org.springframework:spring-web:jar:3.1.2.RELEASE:compile
[DEBUG]       net.sourceforge.javacsv:javacsv:jar:2.0:compile
[DEBUG]    epi:geography:jar:1.0-SNAPSHOT:compile
[DEBUG]       org.geotools:gt-shapefile:jar:8.0:compile
[DEBUG]          org.geotools:gt-data:jar:8.0:compile
[DEBUG]             org.geotools:gt-main:jar:8.0:compile
[DEBUG]                org.geotools:gt-api:jar:8.0:compile
[DEBUG]                com.vividsolutions:jts:jar:1.12:compile
[DEBUG]          org.geotools:gt-referencing:jar:8.0:compile
[DEBUG]             java3d:vecmath:jar:1.3.2:compile
[DEBUG]             org.geotools:gt-metadata:jar:8.0:compile
[DEBUG]                org.geotools:gt-opengis:jar:8.0:compile
[DEBUG]                   net.java.dev.jsr-275:jsr-275:jar:1.0-beta-2:compile
[DEBUG]             jgridshift:jgridshift:jar:1.0:compile
[DEBUG]          jdom:jdom:jar:1.0:compile
[DEBUG]          javax.media:jai_core:jar:1.1.3:compile
[DEBUG]       org.geotools:gt-swing:jar:8.0:compile
[DEBUG]          org.geotools:gt-render:jar:8.0:compile
[DEBUG]             org.geotools:gt-coverage:jar:8.0:compile
[DEBUG]                javax.media:jai_imageio:jar:1.1:compile
[DEBUG]                it.geosolutions.imageio-ext:imageio-ext-tiff:jar:1.1.4:compile
[DEBUG]                   it.geosolutions.imageio-ext:imageio-ext-utilities:jar:1.1.4:compile
[DEBUG]                   javax.media:jai_codec:jar:1.1.3:compile
[DEBUG]                org.jaitools:jt-zonalstats:jar:1.2.0:compile
[DEBUG]                org.jaitools:jt-utils:jar:1.2.0:compile
[DEBUG]             org.geotools:gt-cql:jar:8.0:compile
[DEBUG]          com.miglayout:miglayout:jar:swing:3.7:compile
[DEBUG]    org.springframework:spring-jdbc:jar:3.1.2.RELEASE:compile
[DEBUG]       org.springframework:spring-beans:jar:3.1.2.RELEASE:compile
[DEBUG]       org.springframework:spring-tx:jar:3.1.2.RELEASE:compile
[DEBUG]          aopalliance:aopalliance:jar:1.0:compile
[DEBUG]    org.springframework:org.springframework.test:jar:3.1.2.RELEASE:test
[DEBUG]    org.junit:com.springsource.org.junit:jar:4.7.0:test
[DEBUG]    net.sf.opencsv:opencsv:jar:2.3:compile
[DEBUG]    org.xerial:sqlite-jdbc:jar:3.7.2:compile
[DEBUG]    commons-dbcp:commons-dbcp:jar:1.4:compile
[DEBUG]       commons-pool:commons-pool:jar:1.5.4:compile
[DEBUG]    org.scala-lang:scala-compiler:jar:2.10.0-M7:compile
[DEBUG]       org.scala-lang:scala-library:jar:2.10.0-M7:compile
[DEBUG]       org.scala-lang:scala-reflect:jar:2.10.0-M7:compile
[DEBUG]    org.apache.commons:commons-lang3:jar:3.0:compile
[DEBUG]    commons-io:commons-io:jar:2.3:compile
[DEBUG]    commons-logging:commons-logging:jar:1.1.1:compile
[DEBUG]    org.json:json:jar:20090211:compile
[DEBUG]    org.springframework:spring-core:jar:3.1.2.RELEASE:compile
[DEBUG]       org.springframework:spring-asm:jar:3.1.2.RELEASE:compile
[DEBUG]    org.springframework:spring-context:jar:3.1.2.RELEASE:compile
[DEBUG]       org.springframework:spring-aop:jar:3.1.2.RELEASE:compile
[DEBUG]       org.springframework:spring-expression:jar:3.1.2.RELEASE:compile
[DEBUG]    junit:junit:jar:4.5:test
[DEBUG]    log4j:log4j:jar:1.2.17:compile
[DEBUG]    xerces:xercesImpl:jar:2.9.0:compile
[DEBUG]       xml-apis:xml-apis:jar:1.3.04:compile
[INFO] 
[INFO] --- maven-scala-plugin:2.15.2:script (default-cli) @ population ---
[DEBUG] Created new class realm maven.api
[DEBUG] Importing foreign packages into class realm maven.api
[DEBUG]   Imported: org.apache.maven.wagon.events < plexus.core
[DEBUG]   Imported: org.sonatype.aether.transfer < plexus.core
[DEBUG]   Imported: org.apache.maven.exception < plexus.core
[DEBUG]   Imported: org.sonatype.aether.metadata < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.util.xml.Xpp3Dom < plexus.core
[DEBUG]   Imported: org.sonatype.aether.collection < plexus.core
[DEBUG]   Imported: org.sonatype.aether.version < plexus.core
[DEBUG]   Imported: org.apache.maven.monitor < plexus.core
[DEBUG]   Imported: org.apache.maven.wagon.repository < plexus.core
[DEBUG]   Imported: org.apache.maven.repository < plexus.core
[DEBUG]   Imported: org.apache.maven.wagon.resource < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.logging < plexus.core
[DEBUG]   Imported: org.apache.maven.profiles < plexus.core
[DEBUG]   Imported: org.sonatype.aether.repository < plexus.core
[DEBUG]   Imported: org.apache.maven.classrealm < plexus.core
[DEBUG]   Imported: org.apache.maven.execution < plexus.core
[DEBUG]   Imported: org.sonatype.aether.artifact < plexus.core
[DEBUG]   Imported: org.sonatype.aether.spi < plexus.core
[DEBUG]   Imported: org.apache.maven.reporting < plexus.core
[DEBUG]   Imported: org.apache.maven.usability < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.container < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.component < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.util.xml.pull.XmlSerializer < plexus.core
[DEBUG]   Imported: org.apache.maven.wagon.authentication < plexus.core
[DEBUG]   Imported: org.apache.maven.lifecycle < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.classworlds < plexus.core
[DEBUG]   Imported: org.sonatype.aether.graph < plexus.core
[DEBUG]   Imported: org.sonatype.aether.* < plexus.core
[DEBUG]   Imported: org.apache.maven.settings < plexus.core
[DEBUG]   Imported: org.codehaus.classworlds < plexus.core
[DEBUG]   Imported: org.sonatype.aether.impl < plexus.core
[DEBUG]   Imported: org.apache.maven.wagon.* < plexus.core
[DEBUG]   Imported: org.apache.maven.toolchain < plexus.core
[DEBUG]   Imported: org.sonatype.aether.deployment < plexus.core
[DEBUG]   Imported: org.apache.maven.wagon.observers < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.util.xml.pull.XmlPullParserException < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.util.xml.pull.XmlPullParser < plexus.core
[DEBUG]   Imported: org.apache.maven.configuration < plexus.core
[DEBUG]   Imported: org.apache.maven.cli < plexus.core
[DEBUG]   Imported: org.sonatype.aether.installation < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.context < plexus.core
[DEBUG]   Imported: org.apache.maven.wagon.authorization < plexus.core
[DEBUG]   Imported: org.apache.maven.project < plexus.core
[DEBUG]   Imported: org.apache.maven.rtinfo < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.lifecycle < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.configuration < plexus.core
[DEBUG]   Imported: org.apache.maven.artifact < plexus.core
[DEBUG]   Imported: org.apache.maven.model < plexus.core
[DEBUG]   Imported: org.apache.maven.* < plexus.core
[DEBUG]   Imported: org.apache.maven.wagon.proxy < plexus.core
[DEBUG]   Imported: org.sonatype.aether.resolution < plexus.core
[DEBUG]   Imported: org.apache.maven.plugin < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.* < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.personality < plexus.core
[DEBUG] Populating class realm maven.api
[DEBUG] org.scala-tools:maven-scala-plugin:jar:2.15.2:
[DEBUG]    org.scala-lang:scala-library:jar:2.10.0-M7:runtime
[DEBUG]    org.springframework:spring-jdbc:jar:3.1.2.RELEASE:runtime
[DEBUG]       org.springframework:spring-beans:jar:3.1.2.RELEASE:runtime
[DEBUG]       org.springframework:spring-core:jar:3.1.2.RELEASE:runtime
[DEBUG]          org.springframework:spring-asm:jar:3.1.2.RELEASE:runtime
[DEBUG]       org.springframework:spring-tx:jar:3.1.2.RELEASE:runtime
[DEBUG]          aopalliance:aopalliance:jar:1.0:runtime
[DEBUG]          org.springframework:spring-aop:jar:3.1.2.RELEASE:runtime
[DEBUG]    net.sf.opencsv:opencsv:jar:2.3:runtime
[DEBUG]    epi:util:jar:1.0-SNAPSHOT:runtime
[DEBUG]       org.apache.commons:commons-lang3:jar:3.0:runtime
[DEBUG]       commons-io:commons-io:jar:2.3:runtime
[DEBUG]       commons-logging:commons-logging:jar:1.1.1:runtime
[DEBUG]       org.springframework:spring-webmvc:jar:3.1.2.RELEASE:runtime
[DEBUG]          org.springframework:spring-context-support:jar:3.1.2.RELEASE:runtime
[DEBUG]          org.springframework:spring-expression:jar:3.1.2.RELEASE:runtime
[DEBUG]          org.springframework:spring-web:jar:3.1.2.RELEASE:runtime
[DEBUG]       net.sourceforge.javacsv:javacsv:jar:2.0:runtime
[DEBUG]       org.xerial:sqlite-jdbc:jar:3.7.2:runtime
[DEBUG]       org.json:json:jar:20090211:runtime
[DEBUG]       org.springframework:spring-context:jar:3.1.2.RELEASE:runtime
[DEBUG]       log4j:log4j:jar:1.2.17:runtime
[DEBUG]       xerces:xercesImpl:jar:2.9.0:runtime
[DEBUG]          xml-apis:xml-apis:jar:1.3.04:runtime
[DEBUG]    epi:geography:jar:1.0-SNAPSHOT:runtime
[DEBUG]       org.geotools:gt-shapefile:jar:8.0:runtime
[DEBUG]          org.geotools:gt-data:jar:8.0:runtime
[DEBUG]             org.geotools:gt-main:jar:8.0:runtime
[DEBUG]                org.geotools:gt-api:jar:8.0:runtime
[DEBUG]                com.vividsolutions:jts:jar:1.12:runtime
[DEBUG]          org.geotools:gt-referencing:jar:8.0:runtime
[DEBUG]             java3d:vecmath:jar:1.3.2:runtime
[DEBUG]             commons-pool:commons-pool:jar:1.5.4:runtime
[DEBUG]             org.geotools:gt-metadata:jar:8.0:runtime
[DEBUG]                org.geotools:gt-opengis:jar:8.0:runtime
[DEBUG]                   net.java.dev.jsr-275:jsr-275:jar:1.0-beta-2:runtime
[DEBUG]             jgridshift:jgridshift:jar:1.0:runtime
[DEBUG]          jdom:jdom:jar:1.0:runtime
[DEBUG]          javax.media:jai_core:jar:1.1.3:runtime
[DEBUG]       org.geotools:gt-swing:jar:8.0:runtime
[DEBUG]          org.geotools:gt-render:jar:8.0:runtime
[DEBUG]             org.geotools:gt-coverage:jar:8.0:runtime
[DEBUG]                javax.media:jai_imageio:jar:1.1:runtime
[DEBUG]                it.geosolutions.imageio-ext:imageio-ext-tiff:jar:1.1.4:runtime
[DEBUG]                   it.geosolutions.imageio-ext:imageio-ext-utilities:jar:1.1.4:runtime
[DEBUG]                   javax.media:jai_codec:jar:1.1.3:runtime
[DEBUG]                org.jaitools:jt-zonalstats:jar:1.2.0:runtime
[DEBUG]                org.jaitools:jt-utils:jar:1.2.0:runtime
[DEBUG]             org.geotools:gt-cql:jar:8.0:runtime
[DEBUG]          com.miglayout:miglayout:jar:swing:3.7:runtime
[DEBUG]    org.apache.maven:maven-plugin-api:jar:2.2.1:compile
[DEBUG]    org.apache.maven:maven-project:jar:2.2.1:compile
[DEBUG]       org.apache.maven:maven-settings:jar:2.2.1:compile
[DEBUG]       org.apache.maven:maven-profile:jar:2.2.1:compile
[DEBUG]       org.apache.maven:maven-artifact-manager:jar:2.2.1:compile
[DEBUG]          backport-util-concurrent:backport-util-concurrent:jar:3.1:compile
[DEBUG]       org.apache.maven:maven-plugin-registry:jar:2.2.1:compile
[DEBUG]       org.codehaus.plexus:plexus-interpolation:jar:1.11:compile
[DEBUG]       org.codehaus.plexus:plexus-container-default:jar:1.0-alpha-9-stable-1:compile
[DEBUG]          junit:junit:jar:3.8.1:compile
[DEBUG]    org.apache.maven.reporting:maven-reporting-api:jar:2.2.1:compile
[DEBUG]       org.apache.maven.doxia:doxia-logging-api:jar:1.1:compile
[DEBUG]    org.apache.maven:maven-core:jar:2.2.1:compile
[DEBUG]       org.apache.maven:maven-plugin-parameter-documenter:jar:2.2.1:compile
[DEBUG]       org.slf4j:slf4j-jdk14:jar:1.5.6:runtime
[DEBUG]          org.slf4j:slf4j-api:jar:1.5.6:runtime
[DEBUG]       org.slf4j:jcl-over-slf4j:jar:1.5.6:runtime
[DEBUG]       org.apache.maven:maven-repository-metadata:jar:2.2.1:compile
[DEBUG]       org.apache.maven:maven-error-diagnostics:jar:2.2.1:compile
[DEBUG]       commons-cli:commons-cli:jar:1.2:compile
[DEBUG]       org.apache.maven:maven-plugin-descriptor:jar:2.2.1:compile
[DEBUG]       org.codehaus.plexus:plexus-interactivity-api:jar:1.0-alpha-4:compile
[DEBUG]       org.apache.maven:maven-monitor:jar:2.2.1:compile
[DEBUG]       classworlds:classworlds:jar:1.1:compile
[DEBUG]       org.sonatype.plexus:plexus-sec-dispatcher:jar:1.3:compile
[DEBUG]          org.sonatype.plexus:plexus-cipher:jar:1.4:compile
[DEBUG]    org.apache.maven.shared:maven-dependency-tree:jar:1.2:compile
[DEBUG]    org.apache.commons:commons-exec:jar:1.0.1:compile
[DEBUG]    org.yaml:snakeyaml:jar:1.4:compile
[DEBUG]    org.apache.maven:maven-artifact:jar:2.2.1:compile
[DEBUG]    org.codehaus.plexus:plexus-utils:jar:2.0.1:compile
[DEBUG]    org.apache.maven.doxia:doxia-sink-api:jar:1.1.2:compile
[DEBUG]    org.apache.maven:maven-model:jar:2.2.1:compile
[DEBUG]    org.codehaus.jackson:jackson-core-asl:jar:1.6.1:compile
[DEBUG]    org.codehaus.jackson:jackson-mapper-asl:jar:1.6.1:compile
[DEBUG]    org.apache.maven.shared:maven-invoker:jar:2.0.11:compile
[DEBUG] Skipped remote update check for epi:util:1.0-SNAPSHOT/maven-metadata.xml, already updated during this session.
[DEBUG] Failure to find epi:util:1.0-SNAPSHOT/maven-metadata.xml in http://repo1.maven.org/maven2 was cached in the local repository, resolution will not be reattempted until the update interval of central has elapsed or updates are forced
[DEBUG] Skipped remote update check for epi:util:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Skipped remote update check for epi:geography:1.0-SNAPSHOT/maven-metadata.xml, already updated during this session.
[DEBUG] Failure to find epi:geography:1.0-SNAPSHOT/maven-metadata.xml in http://repo1.maven.org/maven2 was cached in the local repository, resolution will not be reattempted until the update interval of central has elapsed or updates are forced
[DEBUG] Skipped remote update check for epi:geography:1.0-SNAPSHOT/maven-metadata.xml, locally installed metadata up-to-date.
[DEBUG] Verifying availability of C:\Users\scox\.m2\repository\org\geotools\gt-shapefile\8.0\gt-shapefile-8.0.jar from [central (http://repo1.maven.org/maven2, releases+snapshots), scala-tools.org (http://scala-tools.org/repo-releases, releases+snapshots), jboss.releases (http://repository.jboss.org/maven2, releases)]
[DEBUG] Verifying availability of C:\Users\scox\.m2\repository\org\geotools\gt-swing\8.0\gt-swing-8.0.jar from [central (http://repo1.maven.org/maven2, releases+snapshots), scala-tools.org (http://scala-tools.org/repo-releases, releases+snapshots), jboss.releases (http://repository.jboss.org/maven2, releases)]
[DEBUG] Using connector WagonRepositoryConnector with priority 0 for http://repo1.maven.org/maven2
Downloading: http://repo1.maven.org/maven2/org/geotools/gt-swing/8.0/gt-swing-8.0.jar
Downloading: http://repo1.maven.org/maven2/org/geotools/gt-shapefile/8.0/gt-shapefile-8.0.jar
[DEBUG] Writing resolution tracking file C:\Users\scox\.m2\repository\org\geotools\gt-shapefile\8.0\gt-shapefile-8.0.jar.lastUpdated
[DEBUG] Writing resolution tracking file C:\Users\scox\.m2\repository\org\geotools\gt-swing\8.0\gt-swing-8.0.jar.lastUpdated
[DEBUG] Using connector WagonRepositoryConnector with priority 0 for http://repository.jboss.org/maven2
Downloading: http://repository.jboss.org/maven2/org/geotools/gt-shapefile/8.0/gt-shapefile-8.0.jar
Downloading: http://repository.jboss.org/maven2/org/geotools/gt-swing/8.0/gt-swing-8.0.jar
[DEBUG] Writing resolution tracking file C:\Users\scox\.m2\repository\org\geotools\gt-shapefile\8.0\gt-shapefile-8.0.jar.lastUpdated
[DEBUG] Writing resolution tracking file C:\Users\scox\.m2\repository\org\geotools\gt-swing\8.0\gt-swing-8.0.jar.lastUpdated
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 4.869s
[INFO] Finished at: Fri Jan 18 14:28:04 EST 2013
[INFO] Final Memory: 8M/152M
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.scala-tools:maven-scala-plugin:2.15.2:script (default-cli) on project population: Execution default-cli of goal org.scala-tools:maven-scala-plugin:2.15.2:script failed: Plugin org.scala-tools:maven-scala-plugin:2.15.2 or one of its dependencies could not be resolved: The following artifacts could not be resolved: org.geotools:gt-shapefile:jar:8.0, org.geotools:gt-swing:jar:8.0: Could not transfer artifact org.geotools:gt-shapefile:jar:8.0 from/to jboss.releases (http://repository.jboss.org/maven2): Access denied to: http://repository.jboss.org/maven2/org/geotools/gt-shapefile/8.0/gt-shapefile-8.0.jar, ReasonPhrase:Forbidden. -> [Help 1]
org.apache.maven.lifecycle.LifecycleExecutionException: Failed to execute goal org.scala-tools:maven-scala-plugin:2.15.2:script (default-cli) on project population: Execution default-cli of goal org.scala-tools:maven-scala-plugin:2.15.2:script failed: Plugin org.scala-tools:maven-scala-plugin:2.15.2 or one of its dependencies could not be resolved: The following artifacts could not be resolved: org.geotools:gt-shapefile:jar:8.0, org.geotools:gt-swing:jar:8.0: Could not transfer artifact org.geotools:gt-shapefile:jar:8.0 from/to jboss.releases (http://repository.jboss.org/maven2): Access denied to: http://repository.jboss.org/maven2/org/geotools/gt-shapefile/8.0/gt-shapefile-8.0.jar, ReasonPhrase:Forbidden.
	at org.apache.maven.lifecycle.internal.MojoExecutor.execute(MojoExecutor.java:225)
	at org.apache.maven.lifecycle.internal.MojoExecutor.execute(MojoExecutor.java:153)
	at org.apache.maven.lifecycle.internal.MojoExecutor.execute(MojoExecutor.java:145)
	at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject(LifecycleModuleBuilder.java:84)
	at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject(LifecycleModuleBuilder.java:59)
	at org.apache.maven.lifecycle.internal.LifecycleStarter.singleThreadedBuild(LifecycleStarter.java:183)
	at org.apache.maven.lifecycle.internal.LifecycleStarter.execute(LifecycleStarter.java:161)
	at org.apache.maven.DefaultMaven.doExecute(DefaultMaven.java:320)
	at org.apache.maven.DefaultMaven.execute(DefaultMaven.java:156)
	at org.apache.maven.cli.MavenCli.execute(MavenCli.java:537)
	at org.apache.maven.cli.MavenCli.doMain(MavenCli.java:196)
	at org.apache.maven.cli.MavenCli.main(MavenCli.java:141)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
	at java.lang.reflect.Method.invoke(Method.java:597)
	at org.codehaus.plexus.classworlds.launcher.Launcher.launchEnhanced(Launcher.java:290)
	at org.codehaus.plexus.classworlds.launcher.Launcher.launch(Launcher.java:230)
	at org.codehaus.plexus.classworlds.launcher.Launcher.mainWithExitCode(Launcher.java:409)
	at org.codehaus.plexus.classworlds.launcher.Launcher.main(Launcher.java:352)
Caused by: org.apache.maven.plugin.PluginExecutionException: Execution default-cli of goal org.scala-tools:maven-scala-plugin:2.15.2:script failed: Plugin org.scala-tools:maven-scala-plugin:2.15.2 or one of its dependencies could not be resolved: The following artifacts could not be resolved: org.geotools:gt-shapefile:jar:8.0, org.geotools:gt-swing:jar:8.0: Could not transfer artifact org.geotools:gt-shapefile:jar:8.0 from/to jboss.releases (http://repository.jboss.org/maven2): Access denied to: http://repository.jboss.org/maven2/org/geotools/gt-shapefile/8.0/gt-shapefile-8.0.jar, ReasonPhrase:Forbidden.
	at org.apache.maven.plugin.DefaultBuildPluginManager.executeMojo(DefaultBuildPluginManager.java:82)
	at org.apache.maven.lifecycle.internal.MojoExecutor.execute(MojoExecutor.java:209)
	... 19 more
Caused by: org.apache.maven.plugin.PluginResolutionException: Plugin org.scala-tools:maven-scala-plugin:2.15.2 or one of its dependencies could not be resolved: The following artifacts could not be resolved: org.geotools:gt-shapefile:jar:8.0, org.geotools:gt-swing:jar:8.0: Could not transfer artifact org.geotools:gt-shapefile:jar:8.0 from/to jboss.releases (http://repository.jboss.org/maven2): Access denied to: http://repository.jboss.org/maven2/org/geotools/gt-shapefile/8.0/gt-shapefile-8.0.jar, ReasonPhrase:Forbidden.
	at org.apache.maven.plugin.internal.DefaultPluginDependenciesResolver.resolve(DefaultPluginDependenciesResolver.java:215)
	at org.apache.maven.plugin.internal.DefaultMavenPluginManager.createPluginRealm(DefaultMavenPluginManager.java:353)
	at org.apache.maven.plugin.internal.DefaultMavenPluginManager.setupPluginRealm(DefaultMavenPluginManager.java:321)
	at org.apache.maven.plugin.DefaultBuildPluginManager.getPluginRealm(DefaultBuildPluginManager.java:175)
	at org.apache.maven.plugin.DefaultBuildPluginManager.executeMojo(DefaultBuildPluginManager.java:78)
	... 20 more
Caused by: org.sonatype.aether.resolution.ArtifactResolutionException: The following artifacts could not be resolved: org.geotools:gt-shapefile:jar:8.0, org.geotools:gt-swing:jar:8.0: Could not transfer artifact org.geotools:gt-shapefile:jar:8.0 from/to jboss.releases (http://repository.jboss.org/maven2): Access denied to: http://repository.jboss.org/maven2/org/geotools/gt-shapefile/8.0/gt-shapefile-8.0.jar, ReasonPhrase:Forbidden.
	at org.sonatype.aether.impl.internal.DefaultArtifactResolver.resolve(DefaultArtifactResolver.java:538)
	at org.sonatype.aether.impl.internal.DefaultArtifactResolver.resolveArtifacts(DefaultArtifactResolver.java:216)
	at org.sonatype.aether.impl.internal.DefaultRepositorySystem.resolveDependencies(DefaultRepositorySystem.java:358)
	at org.apache.maven.plugin.internal.DefaultPluginDependenciesResolver.resolve(DefaultPluginDependenciesResolver.java:207)
	... 24 more
Caused by: org.sonatype.aether.transfer.ArtifactTransferException: Could not transfer artifact org.geotools:gt-shapefile:jar:8.0 from/to jboss.releases (http://repository.jboss.org/maven2): Access denied to: http://repository.jboss.org/maven2/org/geotools/gt-shapefile/8.0/gt-shapefile-8.0.jar, ReasonPhrase:Forbidden.
	at org.sonatype.aether.connector.wagon.WagonRepositoryConnector$4.wrap(WagonRepositoryConnector.java:951)
	at org.sonatype.aether.connector.wagon.WagonRepositoryConnector$4.wrap(WagonRepositoryConnector.java:941)
	at org.sonatype.aether.connector.wagon.WagonRepositoryConnector$GetTask.run(WagonRepositoryConnector.java:669)
	at org.sonatype.aether.util.concurrency.RunnableErrorForwarder$1.run(RunnableErrorForwarder.java:60)
	at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(ThreadPoolExecutor.java:886)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:908)
	at java.lang.Thread.run(Thread.java:662)
Caused by: org.apache.maven.wagon.authorization.AuthorizationException: Access denied to: http://repository.jboss.org/maven2/org/geotools/gt-shapefile/8.0/gt-shapefile-8.0.jar, ReasonPhrase:Forbidden.
	at org.apache.maven.wagon.shared.http4.AbstractHttpClientWagon.resourceExists(AbstractHttpClientWagon.java:620)
	at org.sonatype.aether.connector.wagon.WagonRepositoryConnector$GetTask.run(WagonRepositoryConnector.java:577)
	... 4 more
[ERROR] 
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/PluginResolutionException
