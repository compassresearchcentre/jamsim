library(testthat)

JAMSIM_DIR <- "~/workspace/jamsim/JAMSIM/" 

ASCAPE.R <- paste(JAMSIM_DIR, "src/org/jamsim/ascape/r/Ascape.r", sep="")
TESTS_DIR <- paste(JAMSIM_DIR, "R/inst/tests/", sep="")

source(ASCAPE.R)

# runs all files starting with "test*" in the directory TEST_DIR
test_dir(TESTS_DIR)
