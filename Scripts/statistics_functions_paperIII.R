###############################################################################
# Print the mean value and the following percentiles:
# - 0:th (min value)
# - 10:th
# - 25:th
# - 50:th (median)
# - 75:th
# - 90:th
# - 100:th (max value)
# for data
# Parameters:
# data: Data to print statistics for
###############################################################################
printMeanAndPercentiles <- function(data) {
  print(paste("Mean: ", mean(data), sep=""))
  print("Percentiles:")
  print(quantile(data, probs=c(0, 0.1, 0.25, 0.5, 0.75, 0.9, 1)))
}

###############################################################################
# Create a plot using plotFunction and save it as outputDir/plotName.eps
# Parameters:
# plotFunction: Function to be used for plotting
# plotFunction: Name of output file (excl. directory and ".eps")
###############################################################################
exportAsEPS <- function(plotFunction, plotName) {
  setEPS()
  postscript(paste(outputDir, "/", plotName, ".eps", sep=""))
  plotFunction
  dev.off()
}

###############################################################################
# Plot a histogram over data with logarithmic scale on the y axis. Save it as
# Output/log_hist_<specifier>.eps.
# Parameters:
# data: data to be plotted
# specifier: a specifier for the data, to be included in the plot file name
# objects: the type of object that the data concerns; the label on the y axis
###############################################################################
logHist <- function(data, specifier="", objects="Notebooks") {
  histData <- hist(data, plot=FALSE)
  lineWidth <- 2
  lineEnd <- 2
  
  exportAsEPS(plot(histData$mids, histData$count, log='y',
                   type='h', lwd=lineWidth, lend=lineEnd,
                   xlab="", ylab=objects),
              paste("log_hist_", specifier, sep=""))
}

###############################################################################
# Plot a histogram over data. Save it as Output/hist_<specifier>.eps.
# Parameters:
# data: data to be plotted
# specifier: a specifier for the data, to be included in the plot file name
###############################################################################
histogram <- function(data, specifier="") {
  exportAsEPS(hist(data, main="", xlab="", ylab="Notebooks"),
              paste("hist_", specifier, sep=""))
}

###############################################################################
# Plot a histogram and a qq plot for sample, side by side.
# Resets mfrow to 1,1 after plotting
###############################################################################
plot_for_normality_check <- function(sample) {
  readline(prompt="Press enter to see normality check plot")
  par(mfrow=c(1,2))
  hist(sample)
  qqnorm(sample)
  qqline(sample)
  par(mfrow=c(1,1))
}

###############################################################################
# Check the assumptions of a linear model (e.g. ANOVA)
# Parameters:
# y: dependent variable
# x: independent variable
###############################################################################
checkLM <- function(y, x) {
  mdl <- lm(y~x)
  plot_for_normality_check(mdl$resid)
  #plot(mdl$residuals~mdl$fitted.values)
}

###############################################################################
# Perform a Kruskal Wallis test with post hoc analysis (pairwise Wilcoxon rank
# sum test)
# Parameters:
# y: dependent variable
# x: independent variable, should be a factor
###############################################################################
kruskalWallisWithPost <- function(y, x) {
  print(kruskal.test(y, x))
  # Post hoc analysis, relevant if Kruskal Wallis test indicates a significant difference
  pAdjustMethod="hochberg"
  pairwise.wilcox.test(y, x, p.adjust.method=pAdjustMethod, paired=FALSE, alternative="two.sided")
}

###############################################################################
# Present mean and percentiles for non.empty connections between notebooks.
# Perform Wilcoxon signed rank tests to find out if there is a significant
# difference between number of intra repro connections and the mean number of
# connections to other repros and between the number of intra repro connections
# and the total number of inter repro connections. After each Wilcoxon test,
# plot the two statistics against each other.
# Parameter:
# connections: Data frame containing info about connections (same format as output from Java programs)
###############################################################################
connection_analysis <- function(connections) {
  # Connections for the empty snippet is skipped, since the number of intra
  # connections overflowed
  connectionsNE <- connections[,"non.empty.connections"]
  connectionsNormalizedNE <- connections[,"non.empty.connections.normalized"]
  print("Non-empty connections:")
  printMeanAndPercentiles(connectionsNE)
  print("Normalized number of non-empty connections:")
  printMeanAndPercentiles(connectionsNormalizedNE)
  logHist(connectionsNE, specifier="connectionsNE")
  logHist(connectionsNormalizedNE, specifier="connectionsNormalizedNE")
  
  intraNE <- connections[,"non.empty.intra.repro.connections"]
  meanInterNE <- connections[,"mean.non.empty.inter.repro.connections"]
  print(wilcox.test(intraNE, meanInterNE, alternative="two.sided", paired=TRUE))
  maxVal <- max(max(intraNE), max(meanInterNE))
  exportAsEPS({
    plot(meanInterNE, intraNE, xlab="Mean inter repro connections", ylab="Intra repro connections")
    lines(c(0,maxVal), c(0,maxVal), col="red")
  }, "meanInter_intraNE")
  
  totalInterNE <- connections[,"non.empty.connections"] - intraNE
  print(wilcox.test(intraNE, totalInterNE, alternative="two.sided", paired=TRUE))
  maxVal <- max(max(intraNE), max(totalInterNE))
  exportAsEPS({
    plot(totalInterNE, intraNE, xlab="Total inter repro connections", ylab="Intra repro connections")
    lines(c(0,maxVal), c(0,maxVal), col="red")
  }, "totalInter_intraNE")
}

###############################################################################
# Return a vector consisting of the values in the second column of the
# argument, each repeated the number of times specified in the third column of
# the same row.
###############################################################################
repeatSecondColumn <- function(data) {
  return(rep(data[2], data[3]))
}
