ff <- function(x) gg(x)
gg <- function(y) sys.status()
str(ff(1))

source(textConnection('1:2 + 1:3'))

#warning
1:2 + 1:3
#muted warning
Hmisc::all.is.numeric(c("F","M"), "vector")
writeLines(captureAll(Parse('1:2 + 1:3')))
writeLines(captureAll(Parse('aa')))
aa

	.pep <- function(expr) {cap(expr)}

writeLines(captureAll(Parse('message("5")')))

ain <- function (x, what = c("test", "vector"), extras = c(".", "NA")) 
{
    what <- match.arg(what)
    old <- options(warn = -1)
    on.exit(options(old))
    x <- sub("[[:space:]]+$", "", x)
    x <- sub("^[[:space:]]+", "", x)
    xs <- x[x %nin% c("", extras)]
    isnum <- !any(is.na(as.numeric(xs)))
    if (what == "test") 
        isnum
    else if (isnum) 
        as.numeric(x)
    else x
}


a <- 1
b <- 2

	name <- "gender"
	activateJavaGD(name, "Basefile")
	varname <- "sex"
	wgtsname <- "weight"
	col <- genderColours
	props <- propWtdtable(a(people[[varname]])[,1], people[[wgtsname]])
	barplot(props, xlab=varname, ylab="Proportion", main=name, col=col)

	
	
	.pep <- function(expr) {
		#wrap in try so if it errors this function will continue to execute 
		#and print any warnings
		result <- source(textConnection(expr)) 
				
		if (!is(result,"try-error") && result$visible) {
			## print result if visible
			show(result$value)
		}
		
		## return value
		if (!is(result,"try-error")) {
			result$value
		}
	}

	
	
wt <-	function (x, weights = NULL, type = c("list", "table"), normwt = FALSE, 
    na.rm = TRUE) 
{
    type <- match.arg(type)
    if (!length(weights)) 
        weights <- rep(1, length(x))
    isdate <- testDateTime(x)
    ax <- attributes(x)
    ax$names <- NULL
    x <- if (is.character(x)) 
        as.category(x)
    else oldUnclass(x)
    lev <- levels(x)
    if (na.rm) {
        s <- !is.na(x + weights)
        x <- x[s, drop = FALSE]
        weights <- weights[s]
    }
    n <- length(x)
    if (normwt) 
        weights <- weights * length(x)/sum(weights)
    i <- order(x)
    x <- x[i]
    weights <- weights[i]
    if (any(diff(x) == 0)) {
        weights <- tapply(weights, x, sum)
        if (length(lev)) {
            levused <- lev[sort(unique(x))]
            if ((length(weights) > length(levused)) && any(is.na(weights))) 
                weights <- weights[!is.na(weights)]
            if (length(weights) != length(levused)) 
                stop("program logic error")
            names(weights) <- levused
        }
        if (!length(names(weights))) 
            stop("program logic error")
        if (type == "table") 
            return(weights)
        x <- all.is.numeric(names(weights), "vector")
        if (isdate) 
            attributes(x) <- c(attributes(x), ax)
        names(weights) <- NULL
        return(list(x = x, sum.of.weights = weights))
    }
    xx <- x
    if (isdate) 
        attributes(xx) <- c(attributes(xx), ax)
    if (type == "list") 
        list(x = if (length(lev)) lev[x] else xx, sum.of.weights = weights)
    else {
        names(weights) <- if (length(lev)) 
            lev[x]
        else xx
        weights
    }
}
	