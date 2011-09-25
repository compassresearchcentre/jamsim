# JAMSIM Example Model analysis and output code
# 
# Author: Oliver Mannion
###############################################################################

#colours used for each gender in charts
genderColours <- c(F="chocolate1", M="cornflowerblue")

baseUpdated <- function () {
	#eg: baseUpdated()
	#called when changes have been made to the base file
	cat ("Executing",deparse(sys.call()),"\n")
	baseCharts()
}

baseCharts <- function () {
	#baseCharts()
	baseChart("sex", "weightBase", col=genderColours)
	baseChart("sex", "weightScenario", col=genderColours)
}

baseChart <- function(varname, wgtsname = "weightBase", xAxisNames = NULL, col=NULL) {
	#produce histogram for year 1 / unchanging variables
	#baseChart("sex", "weightBase", c("Female","Male"))
	name <- paste(dict[[varname]], ifelse(wgtsname == "weightBase", "", " (scenario)"), sep="")
	if (name != "") {
		props <- propWtdtable(a(people[[varname]])[,1], people[[wgtsname]])
		if (!is.null(xAxisNames)) {
			names(props) <- xAxisNames
		}
		
		activateJavaGD(name, "Basefile")
		barplot(props, xlab=varname, ylab="Proportion", main=name, col=col)
	}
}

expandPeople <- function () {
	#create a new dataframe that has the lists expanded
	#into individual variables
	#eg: str(expandPeople())
	with(people,
	data.frame(
	age, alive, sex, weightScenario,
	totalEarnings,
	currentDisabilityState,
	disabilityState=a(disabilityState),
	earningsToDate=a(earningsToDate)
	))
}

beginRun <- function () {
	cat ("Executing",deparse(sys.call()),"\n")
}

endOfRun <- function() {
	#eg: endOfRun()
	cat ("Executing",deparse(sys.call()),"\n")
	assign(".runNumber", .runNumber+1, envir = .GlobalEnv)
	assign(".runName", paste("Run",.runNumber), envir = .GlobalEnv)
	
}

beginSim <- function() {
	#eg: beginSim()
	cat ("Executing",deparse(sys.call()),"\n")
	assign(".runNumber", 0, envir = .GlobalEnv)
}

endOfSim <- function() {
	#eg: endOfSim(children)
	cat ("Executing",deparse(sys.call()),"\n")
	calcAccumulatedEarnings()
	addOutputs()
}

addOutputs <- function() {
	addOutputNode(tblAgents(), "Number of agents and people")
	addOutputNode(tblFemaleAgents(), "Number of females")
	addOutputNode(tblSeverelyDisabled(), "Population severely disabled")
	addOutputNode(tblAverageAgeAtDeath(), "Population average age at death")
	addOutputNode(tblGender(), "Population by gender")
	addOutputNode(xt.etd, "Accumulated earnings (base)", "Base")
	addOutputNode(tblAgeAtDeath(), "Population by age at death (scenario)", "Scenario")
	addOutputNode(tblCrossTabAgeAtDeathByGender(), "Population by age at death and gender (scenario)", "Scenario")
	addOutputNode(tblEarningsSummary(), "Earnings summary (scenario)",  "Scenario")
	addOutputNode(xt.etdw, "Accumulated earnings (scenario)",  "Scenario")
	chartCrossTabAgeAtDeathByGender()
	chartAccumulatedEarningsByGender()
	chartAgeSexPyramid()
}

tblAgents <- function() {
	#tblAgents()
	c(
		"Agents" = dim(people)[1],
		"Scaled population size (base)" = sum(people$weightBase),
		"Scaled population size (scenario)" = sum(people$weightScenario)
	)
}

tblFemaleAgents <- function() {
	#tblFemaleAgents()
	females <- subset(people, sex=="F")
	c(
		"Female agents" = dim(females)[1],
		"Female scaled population size (base)" = sum(females$weightBase),
		"Female scaled population size (scenario)" = sum(females$weightScenario)
	)
}

tblSeverelyDisabled <- function() {
	#tblSeverelyDisabled()
	disabled <- subset(people, currentDisabilityState == 4)
	round(c(
		"Severely disabled population size (base)" = sum(disabled$weightBase),
		"Severely disabled population size (scenario)" = sum(disabled$weightScenario)
	))
}

tblAverageAgeAtDeath <- function() {
	c(
		"Population average age at death (base)" = sum(people$age * people$weightBase) / sum(people$weightBase),
		"Population average age at death (scenario)" = sum(people$age * people$weightScenario) / sum(people$weightScenario)
	)
}

tblGender <- function() {
	#tblGender()
    base <- aggregate(people$weightBase, by = list(people[["sex"]]), FUN = sum)
	scenario <- aggregate(people$weightScenario, by = list(people[["sex"]]), FUN = sum)

	cbind(
		"Sex" = base[1], 
		"Base pop." = round(base$x), "Base %" = prop.table(base$x) * 100,
		"Scenario pop." = round(scenario$x), "Scenario %" = prop.table(scenario$x) * 100
	)
}

tblAgeAtDeath <- function() {
	# tblByAgeAtDeath()
	
	# create factor variable from age at the breaks specified
	ageBreaks <- c(0,20,30,40,50,60,70,80,max(people$age)+1)
	agePartitioned <- cut(people$age, breaks=ageBreaks, right=FALSE)
  
	result <- aggregate(people$weightScenario, by = list(agePartitioned), FUN = sum)
	result$x <- round(result$x)
 	dimnames(result)[[2]] = c("Age group", "Sum")
	result
}

tblCrossTabAgeAtDeathByGender <- function() {
	# tblCrossTabAgeAtDeathByGender()

	# create factor variable from age at the breaks specified
	ageBreaks <- c(0,18,65,max(people$age)+1)
	people$ageBreak1865 <- cut(people$age, breaks=ageBreaks,right=FALSE)

 	# cross tabulate the sum of weight by ageBreak1865 and sex
	xt <- xtabs(weightScenario ~ ageBreak1865+sex, data=people)	
		
	# add row and columns totals
	xt <- addmargins(xt)

	names(dimnames(xt)) <- "Age group"
	
	# replace the column/row name Sum with Total
	dimnames(xt)[[1]] <- sub("Sum", "Total", dimnames(xt)[[1]])
	dimnames(xt)[[2]] <- sub("Sum", "Total", dimnames(xt)[[2]])
	
	# replace M with Male, and F with Female
	dimnames(xt)[[2]] <- sub("^M$", "Male", dimnames(xt)[[2]])
	dimnames(xt)[[2]] <- sub("^F$", "Female", dimnames(xt)[[2]])
	
	# returned rounded version
	round(xt)
}

tblEarningsSummary <- function() {
	# tblEarningsSummary()
	allEarnings <- sum(people$totalEarnings * people$weightScenario)
	
	result <- c(
		"Total population lifetime earnings(millions)" = allEarnings / 1000000,
		"Avg lifetime earnings" = allEarnings / sum(people$weightScenario),
		"Avg annual lifetime earnings" = allEarnings / sum(people$age * people$weightScenario)
	)

	round(result)
}

calcAccumulatedEarnings <- function() {
	assign("etdrows", earningsToDateInRows(), envir = .GlobalEnv)
	assign("xt.etd", tblAccumulatedEtdWeightEqual(), envir = .GlobalEnv)
	assign("xt.etdw", tblAccumulatedEtdWeighted(), envir = .GlobalEnv)
}

earningsToDateInRows <- function() {
	#return earningsToDate in a table of rows for each 
	#of the 100 years for each person
	#earningsToDateInRows()
	data.frame(
		age=rep(people$age, each=100),
		sex=rep(people$sex, each=100),
		weightScenario=rep(people$weightScenario, each=100),
		weightBase=rep(people$weightBase, each=100),
		year=seq(1,100),
		earningsToDate=unlist(people$earningsToDate)
	)
}

tblAccumulatedEtdWeighted <- function() {
	#earnings to date per year, by sex, weighted
	xtabs(earningsToDate*weightScenario ~ sex+year, data=etdrows)
}

tblAccumulatedEtdWeightEqual <- function() {
	#earnings to date per year, by sex, weight equal
	xtabs(earningsToDate*weightBase ~ sex+year, data=etdrows)
}

#load epicalc library but remove aggregate.numeric
#function because it produces warnings when used
library(epicalc)
rm(aggregate.numeric, pos=which(search() == "package:epicalc"))

chartAgeSexPyramid <- function() {
	#chartAgeSexPyramid()
	charttitle <- "Age-sex pyramid (frequency)"
	activateJavaGD(charttitle,"Outputs")
	
	pyramid(people$age, people$sex, binwidth=10, col.gender=genderColours, main=charttitle)
	title(charttitle)
}

chartCrossTabAgeAtDeathByGender <- function() {
	#chartCrossTabAgeAtDeathByGender()
	xt <- tblCrossTabAgeAtDeathByGender()
	title <- "Average earnings over life course by gender (scenario)"
	activateJavaGD(title,"Outputs")
	
	barplot(
	t(xt[1:3,1:2]/1000000),			#transpose, remove totals, show by millions 
	beside=TRUE, 					#bars beside each other, instead of stacked
	xlab="Age group", ylab="No. of people (millions)",		#axis labels
	legend.text=c("Female", "Male"), args.legend=list(x=5), #legend text & location
	las=1, 													#y axis label horiz
	col=genderColours,									#bar colours
	main=title)
	
	#draw line along x axis
	segments(0,0,7)
}

chartAccumulatedEarningsByGender <- function() {
	#chartAccumulatedEarningsByGender()
	years <- dimnames(xt.etd)$year  

	title <- "Accumulated earnings (base)"
	activateJavaGD(title, "Outputs")			
	linechartByGender(years, 
		xt.etd["F",]/1000000000, xt.etd["M",]/1000000000, 
		"Year", "Earnings (billions)", title)
	
	title <- "Accumulated earnings (scenario)"
	activateJavaGD(title, "Outputs")			
	linechartByGender(years, 
			xt.etdw["F",]/1000000000, xt.etdw["M",]/1000000000,
			"Year", "Earnings (billions)", title)
}

linechartByGender <- function(x, yFemale, yMale, xlab, ylab, title) {
	#draws on activate GD		
	ymin = min(yMale, yFemale)
	ymax = max(yMale, yFemale)
	
	plot(
		x, yFemale,
		type="l",
		ylim=c(ymin,ymax),
		xlab=xlab, ylab=ylab, main=title, 
		col=genderColours["F"]
	)
	lines(
		x, yMale, 
		col=genderColours["M"]
	)
	legend(0,ymax,c("Female", "Male"),lty=1,col=genderColours)
}
