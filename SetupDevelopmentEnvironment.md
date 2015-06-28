# Prerequisites #
  * Follow the instructions [here to setup R](SetupR.md)
  * Make sure you have Java installed. To check your version visit http://java.com/en/download/installed.jsp

# Install Eclipse #
  * Go to http://www.eclipse.org/downloads/ and download "Eclipse IDE for Java Developers". This is a .zip file.
  * Extract the .zip file to a folder on your computer
  * Run eclipse.exe to start
  * Eclipse will ask you where you want to create your workspace. A workspace is a folder that holds all your projects. You can select the default or specify your own.
  * When eclipse starts for the first time it will present you with the Welcome page. Click the arrow to go to the workbench.

# Install plugins: Ivy #

  * Start Eclipse and go to the menu item Help – Install New Software.
  * Click Add..
  * In location type http://www.apache.org/dist/ant/ivyde/updatesite
  * Click OK
  * Check ‘Apache Ivy library’ and ‘Apache IvyDE Eclipse plugin’.
  * Click Next and follow the prompts.
  * Click OK to the security warning.
  * Restart Eclipse when prompted to do so.

  * Go to the menu item Window – Preferences
  * Select Ivy in the left hand tree.
  * Check the radio button for ‘Nothing' under 'On Eclipse startup'
  * Select Ivy - Classpath in the left hand tree.
  * Check the box for ‘Resolve dependencies in workspace’

# Install plugins: Findbugs #

  * Start Eclipse and go to the menu item Help – Install New Software.
  * Click Add..
  * In location type http://findbugs.cs.umd.edu/eclipse
  * Click OK
  * Check ‘Findbugs Feature’
  * Click Next and follow the prompts.
  * Click OK to the security warning.
  * Restart Eclipse when prompted to do so.

# Install plugins: PMD #

  * Start Eclipse and go to the menu item Help – Install New Software.
  * Click Add..
  * In location type http://pmd.sourceforge.net/eclipse
  * Click OK
  * Check the latest version of PMD
  * Click Next and follow the prompts.
  * Click OK to the security warning.
  * Restart Eclipse when prompted to do so.

# Setup PMD #
  * Download http://jamsim.googlecode.com/git/eclipse_settings/PMD_Rules_Configuration_oman.xml to your workspace directory
  * Start Eclipse and go to the menu item Window – Preferences
  * Select PMD in the left hand tree.
  * Click `Clear all’
  * Click ‘Import rule set…’
  * Select the file you just downloaded.
  * Check ‘Import by copy’
  * Click OK
  * Click OK
  * If asked ‘Do you want to do a full rebuild?’ click Yes.

# Install plugins: Checkstyle #
  * Start Eclipse and go to the menu item Help – Install New Software.
  * Click Add..
  * In location type http://eclipse-cs.sf.net/update/
  * Click OK
  * Check ‘Eclipse Checkstyle Plug-in’
  * Click Next and follow the prompts.
  * Click OK to the security warning.
  * Restart Eclipse when prompted to do so.

# Setup Checkstyle #
  * Download http://jamsim.googlecode.com/git/eclipse_settings/Checkstyle_oman.xml to your workspace directory
  * Start Eclipse and go to the menu item Window – Preferences
  * Select Checkstyle in the left hand tree.
  * Click New and complete the following:
    * Type: External Configuration File
    * Name: Checkstyle oman
    * Location: select the location to which you save Checkstyle\_oman.xml
  * Select `Checkstyle oman’ and click Set as Default
  * Click OK. If asked to Rebuild, click Yes.

# Install plugins: Open Extern #
  * Start Eclipse and go to the menu item Help – Install New Software.
  * Click Add..
  * In location type http://openextern.googlecode.com/svn/trunk/openextern_update/
  * Click OK
  * Check ‘Open Extern’
  * Click Next and follow the prompts.
  * Click OK to the security warning.
  * Restart Eclipse when prompted to do so.

# Download the JAMSIM source code #
  * In Eclipse, right click in the Project or Package Explorer and select Import
  * Under Git, select Projects from Git and click Next
  * If you receive a “Check Home Directory” dialog box, select “Do not show again” and click OK.
  * Click Clone
  * In URI specify https://code.google.com/p/jamsim/
  * Click Next twice
  * In the destination directory, click Browse and navigate to your workspace directory and click Save.
  * Click Finish. This will create a local jamsim repository.
  * Click Next twice. Click Finish.
  * To download all the JAMSIM dependencies, right click on `ivy.xml [*]` and select Resolve.

# Create a JEM launch configuration #
JAMSIM comes with some JEM launch configurations for standard R installs on Windows XP machines. If you are on Windows 7, other systems, or if you have installed R and/or R packages into alternative locations, you will need to create your own launch configuration as follows:
  * In Eclipse, open the Run Configurations window
  * Right click Java Application and select New
  * On the Main tab, for Main class, specify `org.jamsim.example.RunJEM`
  * On the Arguments tab, under VM arguments, specify the following, adapted to the location of jri.dll on your installation:
    * `-Djava.library.path="C:\Program Files\R\R-2.14.1\library\rJava\jri\x64"`
  * On the Environment tab add the following environment variables, adapted to your installation
    * `PATH=${env_var:PATH};C:\Program Files\R\R-2.14.1\bin\x64`
      * Path to R.dll
    * `R_HOME=C:\Program Files\R\R-2.14.1`
      * Path to R directory
    * `R_LIBS=%USERPROFILE%\Documents\R\win-library\2.14`
      * On Windows 7/Linux/Mac OS X, R packages are typically installed into a user specific package directory. In these cases, the path to the package directory needs to be specified.