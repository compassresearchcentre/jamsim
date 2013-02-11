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

# create settings list to hold:
# .ascape$objsToKeep - names of objects not to delete during ascapeStart()
if (!exists(".ascape")) .ascape <- list()

cat("Ascape.r: Creating Ascape functions\n")

library(hash)

#' Activate, or create if it doesn't exist, a JavaGD device by name.
#' 
#' @param name
#'  device name
#' 
#' @param path
#' 	a path to a sub folder node, eg: "Base/Means" which represents
#' 	the folder Means under the folder Base, or just "Base" which
#' 	will add to the folder Base, or .jnull("java/lang/String"), empty string,
#'  or unspecified to add directly under "Graphs".
#' 
#' @param selectNode
#'  if TRUE, will select the node for this chart in the Navigator
#' 
#' @examples
#' 
#' activateJavaGD("hadmtot")
#' activateJavaGD("gender", "base file")
activateJavaGD <- function(name, path = "", selectNode = FALSE, ...) {
	
	# if .deviceHash doesn't exist in global environment, create it
	if (!exists(".deviceHash")) {
		assign(".deviceHash", hash(), envir = .GlobalEnv)
	}
	
	hashname <- paste(name, path)
	
	# if no device in hash, create it and add to hash
	if (!has.key(hashname, .deviceHash)) {
		JavaGD(...)
		# add device nbr to hash
		.deviceHash[[hashname]] <- dev.cur()
		
		# set name on AscapeGD object
		ascapeGD <- JavaGD:::.getJavaGDObject(dev.cur())
		.jcall(ascapeGD, "V", "setName", name)
		.jcall(ascapeGD, "V", "addToNavigator", path)
	}
	
	# get device number
	devNbr <- .deviceHash[[hashname]]
	
	# make active
	invisible(dev.set(which = devNbr))
	
	if (selectNode) {
		ascapeGD <- JavaGD:::.getJavaGDObject(dev.cur())
		.jcall(ascapeGD, "V", "selectNode")
	}
	
} 

#' Add a list of objects as nodes under the specified parent node.
#' 
#' @param xlist
#'  list of objects
#' @param parentName
#'    name of parent to create node under. Created if it doesn't already exist.
#' @param path
#'  a path to a sub folder node, eg: "Base/Means" which represents
#'  the folder Means under the folder Base, or just "Base" which
#'  will add to the folder Base, or leave unspecified (the default) to add directly
#'  under parentName.
#' @param xnames
#'  the tree node name used for each object
#' 
#' @examples
#'  
addTableNodes <- function(xlist, xnames, parentName, path = .jnull("java/lang/String")) {
	invisible(mapply(addTableNode, x=xlist, name=xnames, MoreArgs=list(parentName = parentName, path = path)))
}


#' Add a list of objects as nodes under the "Model Inputs" node.
#' 
#' @param xlist
#'  list of objects
#' @param path
#'  a path to a sub folder node, eg: "Base/Means" which represents
#'  the folder Means under the folder Base, or just "Base" which
#'  will add to the folder Base, or leave unspecified (the default) to add directly
#'  under "Model Inputs".
#' @param xnames
#'  the tree node name used for each object 
addInputNodes <- function(xlist, xnames,  path = .jnull("java/lang/String")) {
	invisible(mapply(addInputNode, x=xlist, name=xnames, MoreArgs=list(path = path)))
}

#' Add a list of objects as nodes under the "Output Tables" node.
#' 
#' @param xlist
#'  list of objects
#' @param path
#'  a path to a sub folder node, eg: "Base/Means" which represents
#'  the folder Means under the folder Base, or just "Base" which
#'  will add to the folder Base, or leave unspecified (the default) to add directly
#'  under "Output Tables".
#' @param xnames
#'  the tree node name used for each object
#' 
#' @examples
#' xlist <- freqs
#' 
#' addOutputNodes(xlist, path = titleFrequencies)
addOutputNodes <- function(xlist, xnames, path = .jnull("java/lang/String")) {
	invisible(mapply(addOutputNode, x=xlist, name=xnames, MoreArgs=list(path = path)))
}

#' Add an object as a node under the "Base Tables" node.
#' 
#' @param x
#'  object to add
#' @param parentName
#'    name of parent to create node under. Created if it doesn't already exist.
#' @param path
#'  a path to a sub folder node, eg: "Base/Means" which represents
#'  the folder Means under the folder Base, or just "Base" which
#'  will add to the folder Base, or leave unspecified (the default) to add directly
#'  under parentName.
#' @param names
#'  the tree node name used for the object
#' 
#' @examples
#'  
addTableNode <- function(x, name, parentName, path = .jnull("java/lang/String")) {
	rdp <- .jnew("org/jamsim/ascape/output/REXPDatasetProvider", name, toJava(x))
	.jcall(getScapeNode(), "V", "addTableNode", rdp, parentName, path)
}


#' Add an object as a node under the "Model Inputs" node.
#' 
#' @param x
#'  object to add
#' @param path
#'  a path to a sub folder node, eg: "Base/Means" which represents
#'  the folder Means under the folder Base, or just "Base" which
#'  will add to the folder Base, or leave unspecified (the default) to add directly
#'  under "Model Inputs".
#' @param names
#'  the tree node name used for the object
#' 
#' @examples
#'  
addInputNode <- function(x, name, path = .jnull("java/lang/String")) {
	rdp <- .jnew("org/jamsim/ascape/output/REXPDatasetProvider", name, toJava(x))
	.jcall(getScapeNode(), "V", "addInputNode", rdp, path)
}


#' Add an object as a node under the "Output Tables" node.
#' 
#' @param x
#'  object to add
#' @param path
#'  a path to a sub folder node, eg: "Base/Means" which represents
#'  the folder Means under the folder Base, or just "Base" which
#'  will add to the folder Base, or leave unspecified (the default) to add directly
#'  under "Output Tables".
#' @param names
#'  the tree node name used for the object
#' 
#' @examples
#' x <- xlist[[1]]  
addOutputNode <- function(x, name, path = .jnull("java/lang/String")) {
	rdp <- .jnew("org/jamsim/ascape/output/REXPDatasetProvider", name, toJava(x))
	.jcall(getScapeNode(), "V", "addOutputNode", rdp, path)
}

#' Add a graphics device as a node under the  "Graphs" node.
#'  @param plotCmd
#' 	a command that will be evaluated to produce an R graphics device
#'  @param name
#'  the tree node name used for the object
#'  @param path
#'  a path to a sub folder node, eg: "Base/Means" which represents
#'  the folder Means under the folder Base, or just "Base" which
#'  will add to the folder Base, or leave unspecified (the default) to add directly
#'  under "Graphs".
addLazyJGDNode <- function(plotCmd, name, path = .jnull("java/lang/String")) {
	.jcall(getScapeNode(), "V", "addLazyJGDNode", plotCmd, name, path)
}

#' Add a data set as a node under a specified parent node.
#' 
#'  @param expr
#'  An expression to be evaluated to produce a dataset for
#'  display in the Ascape GUI
#'  @param name
#'  the tree node name used for the object
#'  @param parentName 
#'  the name of the parent folder in which the node is to be placed
#'  @param path
#'  a path to a sub folder node, eg: "Base/Means" which represents
#'  the folder Means under the folder Base, or just "Base" which
#'  will add to the folder Base, or leave unspecified (the default) to add directly
#'  under the specified parent node.
addLazyTableNode <- function(expr, name, parentName, path = .jnull("java/lang/String")) {
	.jcall(getScapeNode(), "V", "addLazyTableNode", expr, name, parentName, path)
}


#' The given parameter expr is a String containing a function to
#' create the table or graph required. Stores the expression for use 
#' in recreating the workspace when the workspace is loaded from a saved RData file
#' @param expr
#' 	an expression that is a call to another function to create the table or graph required (see example)
#' @examples
#'	expr <- "addLazyTableNode('tableBuilder('Base', 'means', 'msmoke1', '')', 'msmoke1 means', 'Lazy tables', '')" 
#' 	storeOnLoadExpression(expr)
storeOnLoadExpression <- function(expr){
	if (!exists("tab.expressions")) {
		tab.expressions <<- NULL
	}
	tab.expressions <<- c(tab.expressions, expr)
	#cat(expr, "\n")
}

#' Saves the workspace to an RData file
#' @param fileName
#' the name to give the file
#' @param path
#' the path in which to save the file 
saveWorkspace <- function(fileName, path) {
	filepath<-paste(path, fileName, ".RData", sep="")
	save.image(filepath)
	#cat(fileName, path, "\n")
}

#' Loads a saved workspace from an RData file
#' @param fileName
#' the name of the file to load
#' @param path
#' the path in which the file is found 
loadWorkspace <- function(fileName, path) {
	filepath<-paste(path, fileName, ".RData", sep="")
	load(filepath)
}

	
ascapeStart <- function() {
	# eg: ascapeStart()
	
	# remove all graphics devices and the device-name hash
	graphics.off()
	
	# remove all objects (including .deviceHash), except functions
	cat("ascapeStart: Removing all existing objects\n")
	objsToDel <- lsNoFunc(all.names=TRUE)
	
	# don't delete objects specified by name in the vector .ascape$objsToKeep
	objsToDel <- objsToDel[!(objsToDel %in% c(".ascape",.ascape$objsToKeep))]
	
	rm(pos = ".GlobalEnv", list = objsToDel)
	
	#not sure why, but for rJava 0.8+ we need this otherwise get
	#"rJava was called from a running JVM without .jinit()" when
	#we try the .jcall
	invisible(.jinit()) 	
}

#' Specify the name of an object to keep 
#' when ascape restarts.
#' 
#' @param objname
#'  name of object
ascapeKeepObject <- function(objname) {
	if (!exists(".ascape")) {
		.ascape <<- list()
	}
	
	# add to objsToKeep vector
	.ascape$objsToKeep <<- unique(c(.ascape$objsToKeep, objname))
}

#' Assigns value to a variable, but keep the variables' attributes.
#' Can assign to elements of a list.
#' 
#' @param dest_varname
#'  character vector of destination variable name that has
#'  attributes to keep
#' @param value
#'  value to be assigned to dest_varname
#' 
#' @examples
#' dest <- structure(1:5, myattr="test", class="myclass")
#' value <- 6:10 
#' dest_varname <- "dest"
#' assignKeepingAttributes(dest_varname, value); dest
#' 
#' dest_list <- list(); dest_list$one <- structure(1:10, myattr="test", class="myclass")
#' dest_varname <- "dest_list$one"; value <- 11:20
#' assignKeepingAttributes(dest_varname, value); dest_list
assignKeepingAttributes <- function(dest_varname, value) {
	dest_attributes <- attributes(eval(parse(text=dest_varname)))
	attributes(value) <- dest_attributes
	assign_expr <- paste(dest_varname, "<<- value")
	eval(parse(text=assign_expr))
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


lsNoFunc <- function (...) {
	#eg: lsNoFunc(all.names=TRUE)
	#displays objects as per ls() except for those that are functions
	#eg: rm(list = lsNoFunc(all.names=TRUE))
	#removes all objects (except functions)
	objs <- ls(".GlobalEnv", ...)
	klass <- sapply(objs, function(X) { class(get(X, pos=1)) })
	klass <- .filter(klass, include = "all", exclude = "function")
	names(klass)
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
