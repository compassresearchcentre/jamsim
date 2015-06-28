# Input functionality #

<p align='center'><a href='http://jamsim.googlecode.com/svn/wiki/screenshot/Ascape_simulation_inputs_big.png'><img src='http://jamsim.googlecode.com/svn/wiki/screenshot/Ascape_simulation_inputs_small.png' /></a></p>

  1. Simulation controls
> > Buttons that control stopping, starting, stepping, and pausing of the simulation.
  1. Weighting input file
> > This CSV/XLS file controls the adjustment of the base file to test different scenarios, eg: what if 20% of basefile children have standard of living level 1 (sol1)? The simulation can be run and the results observed.
  1. Parameter input file
> > Variables that control the operation of the statistical predictive models can be specified in an CSV/XLS input file and displayed.
  1. R editor
> > R code that produces output results and analysis can be displayed, edited and executed in a syntax highlighted editor.

# Output functionality #

<p align='center'><a href='http://jamsim.googlecode.com/svn/wiki/screenshot/Ascape_simulation_outputs_big.png'><img src='http://jamsim.googlecode.com/svn/wiki/screenshot/Ascape_simulation_outputs_small.png' /></a></p>

  1. Navigator
> > All simulation objects appear in the Navigator including the base file and it’s individual units (children), parameter sets loaded from external files (ie: datasets), output tables from individual runs and combined output tables from all runs, dataframes of the base file and output tables generated in R, and graphs of simulated variables.
  1. Graphs
> > A range of graphs can be produced from base file and simulated variables. Graphs can be Java based, in which case JFreeChart is used, or R based, in which case the complete range of R graphics functionality is available.
  1. Output tables
> > Statistical analysis, performed in Java or R, can be displayed for base and simulated variables in tables and presented to the user for display and export.
  1. R console
> > The base file, and variables simulated in Java, are exposed to R as part of the simulation output dataframe. The full range of R functionality, including any external R packages, can be used to analyse the dataframe via this interactive R console.