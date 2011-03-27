# Common functions for analysing data in microsimulation models.
# 
# Author: Oliver Mannion
###############################################################################

cat("Common.r: Creating common functions\n")

a <- function (mylist) {
	## alias for toArray
	toArray(mylist)
}

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

aMean <- function (arr) {
	#calculate mean of numeric array
	#eg: aMean(ymmsmoke)
	meanOfRuns(arr, rowNameInFirstCol = FALSE) 
}

library(abind)
arrZAdd <- function(m, arrName) {
	#add the 2d matrix m to the 3d array specified by the string arrName
	#in the next z dimension slot
	#eg: arrZAdd(matrix(1:12,3,4), "arr3d")
	# m <- matrix(1:12,3,4)
	# dimnames(m) <- list(letters=c("A","B","C"), numbers=c("1st","2nd","3rd","4th"))
	# arrZAdd(m, "arr3d")
	
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

arrZMean <- function (arrZ) {
	#mean across Z dimension of 3d array 
	#eg: arrZMean(freqSingle)
	result <- apply(arrZ,c(1,2),mean)
	
	#keep meta attribute
	attr(result, "meta") <- attr(arrZ, "meta")
	
	result
}


cAdd <- function(vec, arrName, colName) {
	#add the vector to the 2d array specified by the string arrName
	#in the next column slot
	#eg: cAdd(c(1,2,3,4,5), "arr2d", "col1")
	# vec <- c(1,2,3)
	# names(vec) <- c("a","b","c")
	# cAdd(vec, "arr2d", "col1")
	
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

global <- function (varname, x) {
	#save x into global variable, ie: top frame, not just this function
	assign(varname, x, envir = .GlobalEnv)	
}

propWtdtable <- function (variable, wgts) {
	#return proportions of variable weighted
	#eg: propWtdtable(people$sex, people$weightEqual)
	prop.table(wtdtable(variable, wgts))
}

toArray <- function (mylist) {
	## convert list of vectors to an array
	t(array(unlist(mylist), dim=c(length(mylist[[1]]),length(mylist))))
}

trim <- function (string) {
	#remove leading and trailing spaces from a string
	gsub("^\\s+|\\s+$", "", string)
}

library("Hmisc")
wtdtable <- function (variable, wgts) {
	#eg: wtdtable(people$sex, people$weight)
	#eg: wtdtable(a(children$single)[,1], people$weightEqual)
	tbl <- wtd.table(variable, weights=wgts)$sum.of.weights
	
	# wtd.table does not count NAs so we have to do it here
	NAs <- sum(wgts[is.na(variable)])
	if (NAs > 0) {
		#attach NA column to table
		expandedTbl <- as.array(c(tbl,NAs))
		names(expandedTbl) <- c(names(tbl), NA)
		tbl <- expandedTbl
	}
	
	#NB: cast to table because wtd.table doesn't do this
	#but table does and we want wtdtable to act like table
	#so when it is passed to the data.frame command it 
	#works properly
	as.table(tbl)
}


