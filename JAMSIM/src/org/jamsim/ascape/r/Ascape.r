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
	# TODO: replace hash code with a list
	
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

#' Add a list of objects as nodes under the "Base Tables" node.
#' 
#' @param xlist
#'  list of objects
#' @param subFolderName
#'  name of sub folder to add node under, or leave unspecified (the default) 
#'  to add directly under "Base Tables".
#' @param xnames
#'  the tree node name used for each object
#' 
#' @examples
#'  
addBaseTableNodes <- function(xlist, xnames = lapply(xlist, dictLookup), subFolderName = .jnull("java/lang/String")) {
	invisible(mapply(addBaseTableNode, x=xlist, name=xnames, MoreArgs=list(subFolderName = subFolderName)))
}


#' Add a list of objects as nodes under the "Model Inputs" node.
#' 
#' @param xlist
#'  list of objects
#' @param subFolderName
#'  name of sub folder to add node under, or leave unspecified (the default) 
#'  to add directly under "Model Inputs".
#' @param xnames
#'  the tree node name used for each object 
addInputNodes <- function(xlist, xnames = lapply(xlist, dictLookup),  subFolderName = .jnull("java/lang/String")) {
	invisible(mapply(addInputNode, x=xlist, name=xnames, MoreArgs=list(subFolderName = subFolderName)))
}

#' Add a list of objects as nodes under the "Output Tables" node.
#' 
#' @param xlist
#'  list of objects
#' @param subFolderName
#'  name of sub folder to add node under, or leave unspecified (the default) 
#'  to add directly under "Output Tables".
#' @param xnames
#'  the tree node name used for each object
#' 
#' @examples
#'  
addOutputNodes <- function(xlist, xnames = lapply(xlist, dictLookup), subFolderName = .jnull("java/lang/String")) {
	invisible(mapply(addOutputNode, x=xlist, name=xnames, MoreArgs=list(subFolderName = subFolderName)))
}

#' Add an object as a node under the "Base Tables" node.
#' 
#' @param x
#'  object to add
#' @param subFolderName
#'  name of sub folder to add node under, or leave unspecified (the default) 
#'  to add directly under "Base Tables".
#' @param names
#'  the tree node name used for the object
#' 
#' @examples
#'  
addBaseTableNode <- function(x, name = dictLookup(x), subFolderName = .jnull("java/lang/String")) {
	rdp <- .jnew("org/jamsim/ascape/output/REXPDatasetProvider", name, toJava(x))
	.jcall(getScapeNode(), "V", "addBaseTableNode", rdp, subFolderName)
}


#' Add an object as a node under the "Model Inputs" node.
#' 
#' @param x
#'  object to add
#' @param subFolderName
#'  name of sub folder to add node under, or leave unspecified (the default) 
#'  to add directly under "Model Inputs".
#' @param names
#'  the tree node name used for the object
#' 
#' @examples
#'  
addInputNode <- function(x, name = dictLookup(x), subFolderName = .jnull("java/lang/String")) {
	rdp <- .jnew("org/jamsim/ascape/output/REXPDatasetProvider", name, toJava(x))
	.jcall(getScapeNode(), "V", "addInputNode", rdp, subFolderName)
}


#' Add an object as a node under the "Output Tables" node.
#' 
#' @param x
#'  object to add
#' @param subFolderName
#'  name of sub folder to add node under, or leave unspecified (the default) 
#'  to add directly under "Output Tables".
#' @param names
#'  the tree node name used for the object
#' 
#' @examples
#'  
addOutputNode <- function(x, name = dictLookup(x), subFolderName = .jnull("java/lang/String")) {
	rdp <- .jnew("org/jamsim/ascape/output/REXPDatasetProvider", name, toJava(x))
	.jcall(getScapeNode(), "V", "addOutputNode", rdp, subFolderName)
}

ascapeStart <- function() {
	# eg: ascapeStart()
	
	# remove all graphics devices and the device-name hash
	graphics.off()
	
	# remove all objects (including .deviceHash), except functions
	cat("ascapeStart: Removing all existing objects\n")
	objsToDel <- lsNoFunc(all.names=TRUE)
	
	#TODO: quick fix to prevent env.base from being deleted
	objsToDel <- objsToDel[which(objsToDel != "env.base")]
	
	rm(pos = ".GlobalEnv", list = objsToDel)
	
	#not sure why, but for rJava 0.8+ we need this otherwise get
	#"rJava was called from a running JVM without .jinit()" when
	#we try the .jcall
	.jinit() 
	
}

#' Gets the navigator scape node.
getScapeNode <- function() {
	if (!exists(".scapeNode")) {
		#' load cache 
		scapeNode <- .jcall("org/jamsim/ascape/r/ScapeRInterface",
				"Lorg/jamsim/ascape/navigator/MicroSimScapeNode;","getLastMsScapeNode")
		assign(".scapeNode", scapeNode, envir = .GlobalEnv)
	}
	.scapeNode
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