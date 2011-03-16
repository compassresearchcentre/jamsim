# TODO: Add comment
# 
# Author: oman002
###############################################################################

propWtdtable <- function (variable, wgts) {
	#return proportions of variable weighted
	#eg: propWtdtable(people$sex, people$weightEqual)
	prop.table(wtdtable(variable, wgts))
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


