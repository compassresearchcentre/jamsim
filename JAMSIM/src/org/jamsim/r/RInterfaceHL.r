.pep <- function(expr) {
	## Parse then evaluate a character vector (expr), returning AND printing the
	## result if the result is visible (ie: the REP parts of the REPL).
	## Modelled on the source function.
	## Syntax errors will be produced by the parse function.
	## Evaluation errors will be produced by the eval.with.vis function.
	## Warnings are explicitly trapped and printed, because JRI doesn't
	## output them to the console when options(warn = 0) for expressions 
	## evaluated via calls to parseAndEval. 
	## Errors are not suppressed by JRI and output straight to the console.

	localWarnings <- list()
	
	result <- withCallingHandlers(
		.Internal(eval.with.vis(parse(text=expr), .GlobalEnv, baseenv())),
		
			warning = function(w) {
				## trap warnings in localWarnings list				
				localWarnings[[length(localWarnings)+1]] <<- w$call
				names(localWarnings)[length(localWarnings)] <<- w$message
				
 				## don't print warning to console
				invokeRestart("muffleWarning")  
				
			})
	
	if (result$visible) {
		## print result if visible
		show(result$value)
	}
	
	if (length(localWarnings) > 0) {
		## explicitly print warnings if they exist
		show(structure(localWarnings, class = "warnings"))
	}
	
	## return value
	result$value
}