# Setup of the Ascape R environment, and functions called from Ascape 
# java code, or that call Ascape java code
# 
# Author: Oliver Mannion
#######################################################################

cat("Ascape.r: Setting up R environment\n")

# set max number of lines printed to the R console during common evaluation 
options(max.print=256)

# tell JavaGD() what class to use for drawing
Sys.setenv('JAVAGD_CLASS_NAME'='org/jamsim/ascape/r/AscapeGD')

cat("Ascape.r: Creating Ascape functions\n")

library(hash)
activateJavaGD <- function(name, subFolderName = "", selectNode = FALSE, ...) {
	# activate, or create if it doesn't exist, a JavaGD device by name
	# eg: activateJavaGD("hadmtot")
	# eg: activateJavaGD("gender", "base file")
	# requires: library(hash)
	
	# if .deviceHash doesn't exist in global environment, create it
	if (!exists(".deviceHash")) {
		assign(".deviceHash", hash(), envir = .GlobalEnv)
	}
	
	hashname <- paste(name, subFolderName)
	
	# if no device in hash, create it and add to hash
	if (!has.key(hashname, .deviceHash)) {
		JavaGD(...)
		# add device nbr to hash
		.deviceHash[[hashname]] <- dev.cur()
		
		# set name on AscapeGD object
		ascapeGD <- .getJavaGDObject(dev.cur())
		.jcall(ascapeGD, "V", "setName", name)
		.jcall(ascapeGD, "V", "addToNavigator", subFolderName)
	}
	
	# get device number
	devNbr <- .deviceHash[[hashname]]
	
	# make active
	invisible(dev.set(which = devNbr))

	if (selectNode) {
		ascapeGD <- .getJavaGDObject(dev.cur())
		.jcall(ascapeGD, "V", "selectNode")
	}

} 

addOutputNodes <- function(xlist, subFolderName = .jnull("java/lang/String"), name = lapply(xlist, dictLookup)) {
	# add a list of results as nodes
	# eg: xlist <- runs.mean.mean$all.by.gender.base
	# eg: addOutputNodes(runs.mean.freq$base, "Frequencies")
	# eg: addOutputNodes(runs.mean.mean$all.by.gender.base, "Means - grouped by gender")
	mapply(addOutputNode, x=xlist, name=name, MoreArgs=list(subFolderName = subFolderName))
}

addOutputNode <- function(x, subFolderName = .jnull("java/lang/String"), name = dictLookup(x)) {
	#add an Output Tables node to the navigator under the subfolder
	#specified
	#eg: addOutputNode(arrZMean(freqSingle), "Frequencies", "single")
	#eg: addOutputNode(arrZMean(freqSingle), "Frequencies")

	rdp <- .jnew("org/jamsim/ascape/output/REXPDatasetProvider", name, toJava(x))
	.jcall(.scape, "V", "addOutputNode", rdp, subFolderName)
}

ascapeStart <- function() {
	# eg: ascapeStart()
	
	# remove all graphics devices and the device-name hash
	graphics.off()
	
	# remove all objects (including .deviceHash), except functions
	cat("ascapeStart: Removing all existing objects\n")
	rm(pos = ".GlobalEnv", list = lsNoFunc(all.names=TRUE))
	
	#not sure why, but for rJava 0.8+ we need this otherwise get
	#"rJava was called from a running JVM without .jinit()" when
	#we try the .jcall
	.jinit() 
	
	# assign scape
	scape <- .jcall("org/jamsim/ascape/r/ScapeRInterface",
			"Lorg/jamsim/ascape/MicroSimScape;","getLastMsScape")
	assign(".scape", scape, envir = .GlobalEnv)
}

meanOfRuns <- function (multiRunResults) {
	##meanOfRuns (called by OutputDataset.scapeClosing)
	#
	#input: a dataframe where the first variable is the row name 
	#and subsequent variables are run values for each row, eg:
	#
	#   Category        Run 1        Run 2
	#1         1 0.0039392527 4.189704e-03
	#2         2 0.0052892006 5.554406e-03
	#3         3 0.0500477200 4.921984e-02
	#4         4 0.0061327012 6.273054e-03
	#
	#output: the original values plus the additional variables: 
	#Mean, Err, Left, Right prepended to the start of each row
	#
	#eg: multiRunResults <- data.frame(cbind(c(5:9),ymo.gptotvis))
	#eg: meanOfRuns(multiRunResults)
	cbind(multiRunResults[1], prependRowMeanInfo(multiRunResults[-1]))
}