# Build environment setup #

## JDK setup ##

  * Ant uses javac from the JDK, so download and install Java SE JDK 6 from [here](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
  * For Ant to locate javac, set the JAVA\_HOME environment variable to the location of the installed JDK, eg: JAVA\_HOME=C:\Program Files\Java\jdk1.6.0\_31
  * To set up the JDK for use in Eclipse do the following:
    * In Eclipse, go to the menu item Windows - Preferences
    * In the left hand tree select Java - Installed JREs
    * Click Add
    * Select Standard VM, click Next
    * Click Directory... and browse to the location of the installed JDK, eg: C:\Program Files\Java\jdk1.6.0\_31
    * Click Finish
    * Click the newly added JDK to make it the default.
    * Click OK.
    * Go back to the menu item Windows - Preferences
    * In the left hand tree select Java - Installed JREs - Execution Environment
    * In the left hand pane select JavaSE-1.6
    * In the right hand pane select the jdk and click OK

## Launch4j setup ##

Launch4j is used to make java launchers that will start JEM in a Windows environment. The launchers are created as part of the ant "package" target.

  * Download Launch4j from http://launch4j.sourceforge.net/
  * Install Launch4j into C:\Program Files\Launch4j
    * B: Launch4j must be installed to C:\Program Files\Launch4j because the build.xml in JAMSIM is hardwired to look for launch4j in this location

## Build test ##

Test your build environment in Eclipse by:
  * Right-clicking on build.xml, select Run As - Ant Build...
  * On the Targets tab, make sure the _package_ target is selected and click Run

# Ant targets #

  * _package_: Compile and build JARs for binaries, source and javadocs
  * _publish_: Publish artifacts to local ivy repository
  * _publish-local-maven-repo_: Publish artifacts to local maven repo directory

# Common build errors #

### Cannot run program "javadoc.exe": CreateProcess error=2, The system cannot find the file specified ###
This occurs when running Ant using the JRE rather than JDK.
On the Ant build file launch configuration, under the JRE tab, make sure the Runtime JRE specified is a JDK. Typically this means selecting an Execution environment or Separate JRE that is a JDK