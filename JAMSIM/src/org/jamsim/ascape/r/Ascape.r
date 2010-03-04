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
