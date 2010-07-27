.pep <- function(expr) {
	## Parse then evaluate a character vector (expr), returning AND printing the
	## result if the result is visible (ie: the REP parts of the REPL).
	## Modelled on the source() function.
	## Syntax errors will be produced by the parse function.
	## Evaluation errors will be produced by the eval.with.vis function.
	## Warnings are explicitly trapped and printed, because JRI doesn't
	## output them to the console when options(warn = 0) for expressions 
	## evaluated via calls to parseAndEval. 
	## Errors are not suppressed by JRI and output straight to the console.
	## NB: warnings are printed at the end of the evaluation of all lines
	## in the expr, as opposed to after each line as it's executed.
	## NB: because warnings are muffled, last.warning won't contain any
	## warnings generated by expr

	localWarnings <- list()
	
	#wrap in try so if it errors this function will continue to execute 
	#and print any warnings
	result <- try(withCallingHandlers(
		.Internal(eval.with.vis(parse(text=expr), .GlobalEnv, baseenv())),
		
			## trap and record warning in localWarnings list
			warning = function(w) {
				## because w$call can be NULL, we must wrap in list()
				## as per R FAQ 7.1
				localWarnings[length(localWarnings)+1] <<- list(w$call)
				names(localWarnings)[length(localWarnings)] <<- w$message
				
 				## don't print warning to console
				invokeRestart("muffleWarning")  
			}
			))
			
	if (!is(result,"try-error") && result$visible) {
		## print result if visible
		show(result$value)
	}
	
	if (length(localWarnings) > 0) {
		## explicitly print warnings if they exist
		show(structure(localWarnings, class = "warnings"))
	}
	
	## return value
	if (!is(result,"try-error")) {
		result$value
	}
}


.getObjects <- function (showFunctions = FALSE) 
{
	#.getObjects()
	#get a list of all the objects in the 
	#global environment, their class and their info
	#if showFunctions == TRUE returns functions as well
	objs <- ls(".GlobalEnv")
	klass <- sapply(objs, function(X) { class(get(X)) })
	if (!showFunctions) klass <- klass[klass != "function"]
	
	result <- NULL
	result$names <- names(klass)
	result$class <- klass
	result$info <- sapply(result$names, function(X) { .getInfo(get(X)) })
	
	names(result$class) <- NULL
	names(result$info) <- NULL
    result
}

.getParts <- function (o) 
{
	#.getParts(o)
	#get a list containing the names, class, and info
	#about the parts of an object
	#if the object has no parts, returns NULL
    result <- NULL
    if (class(o) == "matrix" || (class(o) == "table" && length(dim(o)) == 2)) {
    	#matrix (ie: 2d array) and 2d tables
    	result$names = .getPartNames(o[1,], "[,", "]")
    	result$class <- apply(o, 2, class)
    	result$info <- apply(o, 2, .getInfo)
    } else if (mode(o) == "list") {
    	#lists and dataframes
    	result$names = .getPartNames(o, "[[", "]]")
        result$class <- sapply(o, class)
    	result$info <- sapply(o, .getInfo)
    }

	names(result$class) <- NULL
	names(result$info) <- NULL
    result
}

.getPartNames <- function (o, left, right) {
	resultNames <- c()
	if (!is.null(o) && is.null(names(o))) {
		resultNames <- paste(left, c(1:length(o)), right, sep="") 
	}  else {
		resultNames <- names(o)
	}
	resultNames
}

.getInfo <- function (o) {
	#.getInfo(obj)
	#returns information about an object, or empty chr ""
	#if there is no information
	result <- c("")

	if (class(o) == "data.frame") {
		result  <- paste(class(o), " ", dim(o)[1], " obs. ", dim(o)[2], " vars", sep="")
	} else if (class(o) == "matrix") {
		result <- paste(class(o), " ", dim(o)[1], " x ", dim(o)[2], sep="")
	} else if (class(o) == "table") {
		if (length(dim(o)) == 2)
			result <- paste(class(o), " ", dim(o)[1], " x ", dim(o)[2], sep="")
		else if (length(dim(o)) == 1)
			result <- paste(class(o), " 1 x ", dim(o), sep="")
		else
			result <- paste(class(o), " ",length(o), sep="")
	} else if (class(o) == "list") {
		result <- paste(class(o), " ",length(o), sep="")
	} else {
		result <- paste(class(o), " ",length(o), sep="")
	}
	
	result
}