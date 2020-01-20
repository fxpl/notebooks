setwd("~/github/fxpl/notebooks")
rm(list=ls())

outputDir <- "Output"
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

# TODO: Uppdatera med senaste efter körningar vecka 3!
extCloneFreq <- read.csv("Output/extendedCloneFrequency.csv", header=TRUE)
codeCells <- read.csv("Output/code_cells.csv", header=TRUE)
loc <- read.csv("Output/loc.csv", header=TRUE)

# TODO: Uppdatera kolumnnamn i enlighet med nya headers i analyzern!
cells <- codeCells[,"code.cells"]
bytes <- extCloneFreq[,"blank"]   # Wrong names in temporary file... Should be "bytes", not "blank"
locTotal <- loc[,"total.LOC"]
max(locTotal)
locTotalReduced <- locTotal[locTotal<max(locTotal)]
locNonBlank <- loc[,"non.blank.LOC"]
max(locNonBlank)
locNonBlankReduced <- locNonBlank[locNonBlank<max(locNonBlank)]
# All 4 metrics decay to fast: an ordinary histogram looks like 1 single bar

# Histograms (with log scale on y axis)
logHist(cells, "code_cells")
logHist(bytes, "bytes")
#logHist(locTotal, "Lines of code")
logHist(locTotalReduced, "loc")
#logHist(locNonBlank, "Non-blank lines of code")
logHist(locNonBlankReduced, "loc_non-blank")
