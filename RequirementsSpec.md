# Functional requirements #
  * base file input
    * ability to create individual “agents” from multiple data sets which may require some data manipulation and merging
  * parameter input
    * ability to load multiple tables of parameters from CSV/XLS files, which can be modified in the user interface to test different scenarios and forecast forward
  * scheduling mechanism
    * very simple discrete time based - state changes occur at every tick (year)
  * simulation techniques
    * stochastic equations (require random number generation)
    * ability to clone “agents”
  * output
    * aggregate: charts and tables of aggregate information per year eg: freq distributions, mean, stddev, i.e. descriptive statistics and perhaps more; facility to weight and reweight the sample to obtain representativeness and for scenario testing
    * individual: the complete dataset (ie: state of each individual) after each year; ability to track an individual history (state changes over time)
    * NB: aggregate and individual yearly outputs will be used for
      * internal verification,
      * external validation/alignment/calibration to external benchmarks
      * more sophisticated analysis (eg: via SAS) that can’t be performed in the simulation software
  * user interface
    * a GUI for end users to control input and output, and run scenarios. Should be able to do multiple runs (with same parameters but different random seeds) to obtain a robust mean estimate. It should be intuitive and “easy” for an end user to use.

# Other requirements #
  * performance
    * external end users should be able to perform multiple runs of a multi-year simulation on a desktop PC in a reasonable amount of time (X mins)