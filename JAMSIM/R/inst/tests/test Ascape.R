context("Ascape.r tests")



test_that("storeGraphClosure stores function in graphs list in global environment", {
	if (exists("graphs")) rm(graphs, envir=.GlobalEnv)
	
	expect_that(storeGraphFunction(function() { plot(rnorm(1:100)) }, 'plot'), equals("graphs[['plot']]()"))
	
	graph_func <- graphs$"plot"

	expect_that(graph_func, is_a("function"))
})

