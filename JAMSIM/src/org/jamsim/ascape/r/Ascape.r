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

dictLookup <- function(x) {
	#lookup description of variable x in the dictionary
	#first determines the name of variable x, then does the lookup
	#eg: dictLookup(c(1,2))
	#eg: dictLookup(freqSingle)
	#eg: dictLookup("single")
	#eg: dictLookup("don't exist")

	name <- c()
	grouping <- c()
	set <- c()
	weighting <- c()
	meta <- attr(x, "meta")
	
	#get the variable name
	if (!is.null(meta)) {
		#use the meta attribute
		name <- meta["varname"]
		if (!is.na(meta["grouping"])) grouping <- paste(" by ", meta["grouping"], sep="")
		if (!is.na(meta["set"])) set <- paste(" (", meta["set"], ")", sep="")
		if (!is.na(meta["weighting"])) weighting <- meta["weighting"]
		
	} else if (class(x) %in% c("matrix", "array", "table")) {
		#get name from names of dimensions
		namesdim <- names(dimnames(x))
		name <- paste(namesdim[1], namesdim[2],sep="")
	
	} else if (class(x) == "character") {
		#get name from first position of char vector
		name <- x[1]
	
	} else {
		#fail
		firstParamName <- as.character(sys.call())[2]
		stop(gettextf("cannot determine varname from %s", firstParamName))
	}
	
	#lookup name in dictionary
	desc <- dict[[name]]
	
	if (is.null(desc)) {
		stop(gettextf("variable named '%s' does not exist in data dictionary", name))
		name <- dname
	}
	
	#add grouping, weighting, and set descriptions (if any)
	weightdesc <- ifelse(weighting == "weight", " weighted", "")
	paste(desc, grouping, weightdesc, set, sep="")
}

err <- function (values) {
	## calc the 95% error from the t Distribution
	## see http://www.cyclismo.org/tutorial/R/confidence.html
	## used by meanOfRuns
	qt(0.975,df=length(values)-1)*sd(values)/sqrt(length(values))
}

meanOfRuns <- function (multiRunResults, rowNameInFirstCol = TRUE) {
	##meanOfRuns (called by OutputDataset.scapeClosing)
	#
	#input: a dataframe/array where the first variable is the row name and subsequent 
	#variables are run values for each row, eg:
	#
	#   Category        Run 1        Run 2
	#1         1 0.0039392527 4.189704e-03
	#2         2 0.0052892006 5.554406e-03
	#3         3 0.0500477200 4.921984e-02
	#4         4 0.0061327012 6.273054e-03
	#
	#output: the original values plus the additional variables: 
	#Mean, Err, Left, Right
	
	
	#remove first column from dataframe
	values <- c()
	if (rowNameInFirstCol) {
		values <- multiRunResults[-1]
	} else {
		values <- multiRunResults
	}
	                                   
	#calculate mean of each row
	meanRuns <- apply(values,1,mean)
	
	#calculate error of each row
	errRuns <- apply(values,1,err)
	
	#calculate left CI
	leftRuns <- meanRuns - errRuns
	
	#calculate right CI
	rightRuns <- meanRuns + errRuns
	
	#return dataframe with mean, error, and confidence intervals
	if (rowNameInFirstCol) {
		result <- cbind(multiRunResults[1], Mean = meanRuns, Err = errRuns, 
		Left = leftRuns, Right = rightRuns, multiRunResults[-1])
	} else {
		result <- cbind(Mean = meanRuns, Err = errRuns, 
		Left = leftRuns, Right = rightRuns, multiRunResults)
	}
	
	#keep meta attribute
	attr(result, "meta") <- attr(multiRunResults, "meta")
	
	result
	
}