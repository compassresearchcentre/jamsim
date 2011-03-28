Installation and Usage
----------------------

JAMSIM has only been tested extensively on R v2.11.1 and v2.12.2

    * Download and install R.
    * Open R and execute the following to install required packages: 

install.packages(c("rJava","JavaGD","hash","abind","Hmisc"))

    * Download and extract the jamsim binary + dependencies
    * Execute JEM.exe (Windows only - for other operating systems, see below)

(NB: JEM.exe works by looking up the location of R from the registry key HKEY_LOCAL_MACHINE\SOFTWARE\R-core\R\InstallPath. However if the option "Save version number in registry" was not checked during installation of R then JEM.exe will not find R. Instead see the next section on how to manually run JEM.)

Manually running JEM
--------------------

To manually run JEM without JEM.exe, because you are not on a Windows platform, or because you want to monitor console output, you need to

    * First make sure R.dll is on the path, eg: 

    PATH=%PATH%;C:\Program Files\R\R-2.11.1\bin

        or 

    PATH=%PATH%;C:\Program Files\R\R-2.12.2\bin\i386 

    or if running via an Eclipse run configuration set the path like this: PATH = ${env_var:PATH};C:\Program Files\R\R-2.11.1\bin 

    * Make sure -Djava.library.path is specified in the arguments supplied to the JVM and points to the directory containing jri.dll , eg: 

    java -jar jamsim.jar -Djava.library.path="C:\Program Files\R\R-2.11.\library\rJava\jri" 

(NB: If java.library.path has not been specified, then the loader will try and find jri.dll in the current directory.)

Failing to do this will result in the error "Cannot load JRI native library" 