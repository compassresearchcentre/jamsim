### Installation and Usage ###
  * Follow the instructions [here to setup R](SetupR.md)
  * Download and extract the [jamsim binary + dependencies](http://code.google.com/p/jamsim/downloads/list)
  * Execute JEM.exe (Windows only - for other operating systems, see below)

_NB: JEM.exe works by looking up the location of R from the registry key `HKEY_LOCAL_MACHINE\SOFTWARE\R-core\R\InstallPath`. However if the option "Save version number in registry" was not checked during installation of R then JEM.exe will not find R. Instead see the next section on how to manually run JEM._

### Manually running JEM on Windows ###

To manually run JEM without JEM.exe because you want to monitor console output, you need to

  * First make sure `R.dll` is on the path, eg:
> `PATH=%PATH%;C:\Program Files\R\R-2.11.1\bin`
> > or

> `PATH=%PATH%;C:\Program Files\R\R-2.12.2\bin\i386`
> > or for 64-bit R

> `PATH=%PATH%;C:\Program Files\R\R-2.12.2\bin\x64`

> or if running via an Eclipse run configuration set the path like this:
> `PATH = ${env_var:PATH};C:\Program Files\R\R-2.11.1\bin`

  * Make sure -Djava.library.path is specified in the arguments supplied to the JVM and points to the directory containing `jri.dll` , eg, for 32-bit R:

> `java -Djava.library.path="C:\Program Files\R\R-2.11.1\library\rJava\jri\i386" -jar jamsim-VERSION.jar`

for 64-bit R:

> `java -Djava.library.path="C:\Program Files\R\R-2.11.1\library\rJava\jri\x64" -jar jamsim-VERSION.jar`

_NB: If java.library.path has not been specified, then the loader will try and find jri.dll in the current directory._

Failing to do this will result in the error "**Cannot load JRI native library**"

### Manually running JEM on Mac OS X ###

In the terminal:
  * Specify R\_HOME:
> `declare -x R_HOME="/Library/Frameworks/R.framework/Resources"`

> _(NB: Failing to so will produce the error "Unable to initialize R")_

  * Run JEM, eg:
> `java -Djava.library.path="/Library/Frameworks/R.framework/Resources/library/rJava/jri" -jar jamsim-VERSION.jar`

### Manually running JEM on Linux ###

_NB: This example is for Ubuntu. Package installation and path details may vary on other distributions._

Installation:

  * Install full JDK (needed to build rJava during its installation): `sudo apt-get install default-jdk`
  * Install R: `sudo apt-get install r-base`
  * Setup java config for R: `sudo R CMD javareconf`
  * Run R as superuser: `sudo R`
  * Install R packages: `install.packages(c("rJava","JavaGD","hash","abind","Hmisc"))`

Usage:
  * Specify R\_HOME:
> `declare -x R_HOME="/usr/lib/R"`

> _(NB: Failing to so will produce the error "Unable to initialize R")_

  * Run JEM, eg:
> `java -Djava.library.path=/usr/local/lib/R/site-library/rJava/jri -jar jamsim-VERSION.jar`