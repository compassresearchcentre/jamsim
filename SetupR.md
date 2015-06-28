# Set up R #

JAMSIM has been tested extensively on R using all versions since 2.11.

  * Download and install [R](http://www.r-project.org/).
  * Open R and execute the following to install required packages:
`install.packages(c("rJava","JavaGD","hash","abind","Hmisc","epicalc"))`

2012-11-22: Currently GUI applications of JAMSIM involving graphical devices will not show these when using versions of the JavaGD R library beyond 0.55.