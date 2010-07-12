cat("Creating function toArray\n")

toArray <- function (mylist) {
	## convert list of vectors to an array
	t(array(unlist(mylist), dim=c(length(mylist[[1]]),length(mylist))))
}

cat("Creating function a\n")

a <- function (mylist) {
	## alias for toArray
	toArray(mylist)
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


cat("Creating function meanOfRuns\n")

meanOfRuns <- function (multiRunResults) {
##meanOfRuns (called by OutputDataset.scapeClosing)
#
#input: a dataframe where the first variable is the row name and subsequent 
#variables are run values for each row, eg:
#
#   Category        Run 1        Run 2
#1         1 0.0039392527 4.189704e-03
#2         2 0.0052892006 5.554406e-03
#3         3 0.0500477200 4.921984e-02
#4         4 0.0061327012 6.273054e-03
#
#output: the original dataset plus the additional variables: 
#Mean, Err, Left, Right


#remove first column from dataframe
values <- multiRunResults[-1]
                                   
#calculate mean of each row
meanRuns <- apply(values,1,mean)

#calculate error of each row
errRuns <- apply(values,1,err)

#calculate left CI
leftRuns <- meanRuns - errRuns

#calculate right CI
rightRuns <- meanRuns + errRuns

#return dataframe with mean, error, and confidence intervals
cbind(multiRunResults[1], Mean = meanRuns, Err = errRuns, 
Left = leftRuns, Right = rightRuns, multiRunResults[-1])
}

ascapeStart <- function() {
	# remove all graphics devices and the device-name hash
	# eg: ascapeStart()
	graphics.off()
	assign("deviceHash", hash(), envir = .GlobalEnv)
}

cat("Creating function activateJavaGD\n")
library(hash)
activateJavaGD <- function(name, ...) {
	# activate, or create if it doesn't exist, a JavaGD device by name
	# eg: activateJavaGD("gp")
	# eg: activateJavaGD("hospadm")
	# requires: library(hash)
	
	# if deviceHash doesn't exist in global environment, create it
	if (!exists("deviceHash")) {
		assign("deviceHash", hash(), envir = .GlobalEnv)
	}
	
	# if no device in hash, create it and add to hash
	if (!has.key(name, deviceHash)) {
		JavaGD(...)
		# add device nbr to hash
		deviceHash[[name]] <- dev.cur()
		
		# set name on AscapeGD object
		ascapeGD <- .getJavaGDObject(dev.cur())
		.jcall(ascapeGD, "V", "setName", name)
		.jcall(ascapeGD, "V", "addToNavigator")
	}
	
	# get device number
	devNbr <- deviceHash[[name]]
	
	# make active
	invisible(dev.set(which = devNbr))
} 

cat("Creating function trim\n")
trim <- function (string) 
{
    gsub("^\\s+|\\s+$", "", string)
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

.getDataObjects <- function (showFunctions = FALSE) 
{
	#.getDataObjects()
	#get a named vector of all the objects in the 
	#global environment and their class
	#if showFunctions == TRUE returns functions as well
	objs <- ls(".GlobalEnv")
	result <- sapply(objs,function(X) { class(get(X)) })
	
	if (showFunctions)
		result
	else 
		result[result != "function"]
}
