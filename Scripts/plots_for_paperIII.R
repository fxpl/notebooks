setwd("~/github/fxpl/notebooks")
rm(list=ls())

outputDir <- "Output"

printMeanAndPercentiles <- function(x) {
  print(paste("Mean: ", mean(x), sep=""))
  print("Percentiles:")
  print(quantile(x, probs=c(0, 0.1, 0.25, 0.5, 0.75, 0.9, 1)))
}

###############################################################################
# Plot a histogram over data with logarithmic scale on the y axis. Save it as
# Output/log_hist_<specifier>.eps.
# Parameters:
# data: data to be plotted
# specifier: a specifier for the data, to be included in the plot file namn
###############################################################################
logHist <- function(data, specifier="") {
  histData <- hist(data, plot=FALSE)
  lineWidth <- 2
  lineEnd <- 2
  
  setEPS()
  postscript(paste(outputDir, "/log_hist_", specifier, ".eps", sep=""))
  plot(histData$mids, histData$count, log='y',
       type='h', lwd=lineWidth, lend=lineEnd,
       xlab="", ylab="Notebooks"
  )
  dev.off()
}

extCloneFreq <- read.csv("Output/extendedCloneFrequency.csv", header=TRUE)
codeCells <- read.csv("Output/code_cells.csv", header=TRUE)


# SIZES
cells <- codeCells[,"code.cells"]
bytes <- extCloneFreq[,"bytes"]
locTotal <- extCloneFreq[,"total.LOC"]
locTotalReduced <- locTotal[locTotal<max(locTotal)]
locNonBlank <- extCloneFreq[,"non.blank.LOC"]
locNonBlankReduced <- locNonBlank[locNonBlank<max(locNonBlank)]
# All 4 metrics decay too fast: an ordinary histogram looks like 1 single bar

# Statistics
print("Cells:")
printMeanAndPercentiles(cells)
print("Bytes:")
printMeanAndPercentiles(bytes)
print("Non-empty LOC:")
printMeanAndPercentiles(locNonBlank)
print("Total LOC:")
printMeanAndPercentiles(locTotal)

# Histograms (with log scale on y axis)
logHist(cells, "code_cells")
logHist(bytes, "bytes")
#logHist(locTotal, "Lines of code")
logHist(locTotalReduced, "loc")
#logHist(locNonBlank, "Non-blank lines of code")
logHist(locNonBlankReduced, "loc_non-blank")


# LANGUAGES
lang_percentages <- c(95.3537, 0.8215, 0.7885, 0.1896, 0.6663, 2.1804)
labels <- c("Python", "Julia", "R", "Scala", "other", "unknown")
colors <- c("blue", "green", "yellow", "purple", "brown", "gray")
setEPS()
postscript(paste(outputDir, "/languages.eps", sep=""))
pie(lang_percentages, labels=NA,  col=colors)
legend(x="topright", legend=labels, fill=colors)
dev.off()
