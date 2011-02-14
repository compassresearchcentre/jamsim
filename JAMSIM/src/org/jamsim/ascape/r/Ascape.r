#  tell JavaGD() what class to use for drawing
cat("Setting JavaGD class\n")
Sys.setenv('JAVAGD_CLASS_NAME'='org/jamsim/ascape/r/AscapeGD')

cat("Creating function a\n")

a <- function (mylist) {
	## alias for toArray
	toArray(mylist)
}

cat("Creating function activateJavaGD\n")

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

cat("Creating function addRowPercents\n")

addRowPercents <- function (counts) {
	#adds row percentages to a set of counts
	#eg: addRowPercents(yearlyFreq(children$sol, "sol"))
	pcents <- prop.table(counts,1) * 100
	
	# add (%) to the end of column headings
	dimnames(pcents)[[2]] <- sapply(dimnames(pcents)[[2]], paste, "(%)")
	
	combined <- cbind(counts, pcents)
	names(dimnames(combined)) <- names(dimnames(counts))
	combined
}

cat("Creating function addOutputNode\n")

addOutputNode <- function(x, subFolderName, name = dictLookup(x)) {
	#add an Output Tables node to the navigator under the subfolder
	#specified
	#eg: addOutputNode(arrZMean(freqSingle), "Frequencies", "single")
	#eg: addOutputNode(arrZMean(freqSingle), "Frequencies")

	rdp <- .jnew("org/jamsim/ascape/output/REXPDatasetProvider", name, toJava(x))
	.jcall(.scape, "V", "addOutputNode", rdp, subFolderName)
}

cat("Creating function aMean\n")

aMean <- function (arr) {
	#eg: aMean(ymmsmoke)
	#calculate mean of numeric array
	meanOfRuns(arr, rowNameInFirstCol = FALSE) 
}

cat("Creating function arrZadd\n")

library(abind)
arrZAdd <- function(m, arrName) {
	#eg: arrZAdd(matrix(1:12,3,4), "arr3d")
	# m <- matrix(1:12,3,4)
	# dimnames(m) <- list(letters=c("A","B","C"), numbers=c("1st","2nd","3rd","4th"))
	# arrZAdd(m, "arr3d")
	#add the 2d matrix m to the 3d array specified by the string arrName
	#in the next z dimension slot
	
	if (length(dim(m)) != 2) {
		firstParamName <- as.character(sys.call())[2]
		stop(gettextf("'%s' must have 2 dimensions\n",firstParamName))
	}
	
	#create new NULL variable if arrName doesn't exist
	if (!exists(arrName)) {
		assign(arrName, NULL, envir = .GlobalEnv)
	}
	
	#get current value of arrName
	arr <- eval(parse(text=arrName))
	
	#bind m to the 3rd dimension of arr
	arr <- abind(arr,m,along=3)
	
	#add back names of dimension because they get lost in abind
	names(dimnames(arr)) <- names(dimnames(m))
	
	#keep meta attribute
	attr(arr, "meta") <- attr(m, "meta")	

	#save to arrName
	assign(arrName, arr, envir = .GlobalEnv)
}

cat("Creating function arrZMean\n")

arrZMean <- function (arrZ) {
	#eg: arrZMean(freqSingle)
	#mean across Z dimension of 3d array 
	result <- apply(arrZ,c(1,2),mean)
	
	#keep meta attribute
	attr(result, "meta") <- attr(arrZ, "meta")
	
	result
	
}

cat("Creating function ascapeStart\n")

ascapeStart <- function() {
	# eg: ascapeStart()
	# remove all graphics devices and the device-name hash
	graphics.off()
	#assign(".deviceHash", hash(), envir = .GlobalEnv)
	
	# remove all objects, except functions
	cat("Removing all existing objects\n")
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

cat("Creating function cAdd\n")

cAdd <- function(vec, arrName, colName) {
	#eg: cAdd(c(1,2,3,4,5), "arr2d", "col1")
	# vec <- c(1,2,3)
	# names(vec) <- c("a","b","c")
	# cAdd(vec, "arr2d", "col1")
	#add the vector to the 2d array specified by the string arrName
	#in the next column slot
	
	#create new NULL variable if arrName doesn't exist
	if (!exists(arrName)) {
		assign(arrName, NULL, envir = .GlobalEnv)
	}
	
	#get current value of arrName
	arr <- eval(parse(text=arrName))
	
 	#bind vec to the arr with column name = colName
	arr <- cbind(arr, `colnames<-`(cbind(vec), colName))
	
	#keep meta attribute
	attr(arr, "meta") <- attr(vec, "meta")	
	
	#save to arrName
	assign(arrName, arr, envir = .GlobalEnv)
}

cat("Creating function dictLookup\n")

dictLookup <- function(x) {
	#lookup description of variable
	#eg: dictLookup(c(1,2))
	#eg: dictLookup(freqSingle)
	#eg: dictLookup("single")
	#eg: dictLookup("don't exist")

	name <- c()
	grouping <- c()
	set <- c()
	weighting <- c()
	meta <- attr(x, "meta")
	
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
		firstParamName <- as.character(sys.call())[2]
		stop(gettextf("cannot determine varname from %s", firstParamName))
	}
	
	desc <- dict[[name]]
	
	if (is.null(desc)) {
		stop(gettextf("variable named '%s' does not exist in data dictionary", name))
		name <- dname
	}
	
	weightdesc <- ifelse(weighting == "weight", " weighted", "")
	paste(desc, grouping, weightdesc, set, sep="")
}

cat("Creating function err\n")

err <- function (values) {
	## calc the 95% error from the t Distribution
	## see http://www.cyclismo.org/tutorial/R/confidence.html
	qt(0.975,df=length(values)-1)*sd(values)/sqrt(length(values))
}

cat("Creating function freq\n")

freq <- function(variable, varname) {
	# frequency table with percent
	# v = variable
	#
	# eg: freq(a(children$msmoke)[,1], "msmoke")
	# eg: freq(a(children$msmoke)[,2], "msmoke")
	tbl <- as.data.frame( table(variable, dnn = varname), responseName = "Frequency")
	tbl$Percent <- prop.table(tbl$Frequency) * 100
	tbl$"Cumulative Percent" <- cumsum (tbl$Percent) 
	tbl
}

cat("Creating function global\n")

global <- function (varname, x) {
	#save x into global variable, ie: top frame, not just this function
	assign(varname, x, envir = .GlobalEnv)	
}

cat("Creating function meanOfRuns\n")

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

cat("Creating function toArray\n")

toArray <- function (mylist) {
	## convert list of vectors to an array
	t(array(unlist(mylist), dim=c(length(mylist[[1]]),length(mylist))))
}

cat("Creating function trim\n")

trim <- function (string) 
{
    gsub("^\\s+|\\s+$", "", string)
}