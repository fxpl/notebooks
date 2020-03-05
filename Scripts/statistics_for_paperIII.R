rm(list=ls())
setwd("~/github/fxpl/notebooks")
source("../mk/PaperII/Performance/plot_for_normality_check.R")
outputDir <- "Output"

# FUNCTIONS
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

# NOTEBOOK DATA
codeCells <- read.csv("Output/code_cells.csv", header=TRUE, stringsAsFactors=FALSE)
nonEmptySnippets <- read.csv("Output/snippetsPerFileNE.csv", header=TRUE, stringsAsFactors=FALSE)
sizes <- read.csv("Output/notebook_sizes.csv", header=TRUE)
loc <- read.csv("Output/loc.csv", header=TRUE, stringsAsFactors=FALSE)
languages <- read.csv("Output/languages.csv", header=TRUE, stringsAsFactors=FALSE)
snippetOccurrencesA <- read.csv("Output/filesPerSnippetA.csv", header=FALSE, stringsAsFactors=FALSE)
snippetOccurrencesNE <- read.csv("Output/filesPerSnippetNE.csv", header=FALSE, stringsAsFactors=FALSE)
notebookOccurencesA <- read.csv("Output/nb_clone_distrA.csv", header=FALSE)
notebookOccurencesNE <- read.csv("Output/nb_clone_distrNE.csv", header=FALSE)
cloneFreq <- read.csv("Output/cloneFrequency.csv", header=TRUE, stringsAsFactors=FALSE)
connections <-read.csv("Output/connections.csv", header=TRUE, stringsAsFactors=FALSE)
nbData = merge(languages, cloneFreq, by="file")
nbData = merge(codeCells, nbData, by="file")
nbData = merge(nonEmptySnippets, nbData, by="file")

# SIZES
cells <- codeCells[,"code.cells"]
bytes <- sizes[,"bytes"]
locTotal <- loc[,"total.LOC"]
locTotalReduced <- locTotal[locTotal<max(locTotal)]
locNonBlank <- loc[,"non.blank.LOC"]
locNonBlankReduced <- locNonBlank[locNonBlank<max(locNonBlank)]

# Descriptive statistics
print("Cells:")
printMeanAndPercentiles(cells)
print("Bytes:")
printMeanAndPercentiles(bytes)
print("Non-empty LOC:")
printMeanAndPercentiles(locNonBlank)
print("Total LOC:")
printMeanAndPercentiles(locTotal)

# Histograms (with log scale on y axis)
# All 4 metrics decay too fast: an ordinary histogram looks like 1 single bar
logHist(cells, specifier="code_cells")
logHist(bytes, specifier="bytes")
#logHist(locTotal, specifier="Lines of code")
logHist(locTotalReduced, specifier="loc")
#logHist(locNonBlank, specifier="Non-blank lines of code")
logHist(locNonBlankReduced, specifier="loc_non-blank")


# LANGUAGES
lang_percentages <- c(95.3487, 0.8214, 0.7881, 0.1896, 0.6656, 2.1867)
labels <- c("Python", "Julia", "R", "Scala", "other", "unknown")
colors <- c("blue", "green", "yellow", "purple", "brown", "gray")
exportAsEPS({
  pie(lang_percentages, labels=NA,  col=colors)
  legend(x="topright", legend=labels, fill=colors)
}, "languages")


# CLONE FREQUENCY
# Snippet occurences distribution
logHist(snippetOccurrencesA[,2], specifier="snippetOccurencesA", objects="Snippets")
logHist(snippetOccurrencesNE[,2], specifier="snippetOccurencesNE", objects="Snippets")
# Notebook clone occurrences distribution
logHist(notebookOccurencesA[,1], specifier="notebookOccurencesA")
logHist(notebookOccurencesNE[,1], specifier="notebookOccurencesNE")

# Plots and descriptive statistics
frequencies <- nbData[,"clone.frequency"]
print("Clone frequency")
printMeanAndPercentiles(frequencies)
histogram(frequencies, "clone_frequencyA") # Non-parametric test needed (Spearman!?) Liknande distribution för alla språk -> Kurskal-Wallis lämpligt för att jämföra medianer
frequencies <- nbData[,"non.empty.clone.frequency"]
print("Clone frequency, empty snippets excluded")
printMeanAndPercentiles(frequencies)
histogram(frequencies, "clone_frequencyNE")

# Correlation with size (=number of code cells)
exportAsEPS(plot(clone.frequency~code.cells, data=nbData, xlab="Number of code cells", ylab="Clone frequency"),
            "cells_frequencyA")
print("Correlation with size (all clones):")
cor.test(nbData$code.cells, nbData$clone.frequency, alternative="two.sided", method="spearman")

exportAsEPS(plot(non.empty.clone.frequency~non.empty.snippets, data=nbData, xlab="Number of code cells", ylab="Clone frequency"),
            "cells_frequencyNE")
print("Correlation with size (non-empty clones):")
cor.test(nbData$non.empty.snippets, nbData$non.empty.clone.frequency, alternative="two.sided", method="spearman")

# Association with language
exportAsEPS(boxplot(clone.frequency~language, data=nbData), "lang_frequencyA")
nbDataKnownLang <- nbData[nbData$language!=" UNKNOWN",]
checkLM(nbDataKnownLang$clone.frequency, as.factor(nbDataKnownLang$language))
print("Correlation with language (all clones):")
kruskalWallisWithPost(nbDataKnownLang$clone.frequency, as.factor(nbDataKnownLang$language))

exportAsEPS(boxplot(non.empty.clone.frequency~language, data=nbData), "lang_frequencyNE")
checkLM(nbDataKnownLang$non.empty.clone.frequency, as.factor(nbDataKnownLang$language))
print("Correlation with language (non-empty clones):")
kruskalWallisWithPost(nbDataKnownLang$non.empty.clone.frequency, as.factor(nbDataKnownLang$language))


# CONNECTIONS
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
wilcox.test(intraNE, meanInterNE, alternative="two.sided", paired=TRUE)
maxVal <- max(max(intraNE), max(meanInterNE))
exportAsEPS({
  plot(meanInterNE, intraNE, xlab="Mean inter repro connections", ylab="Intra repro connections")
  lines(c(0,maxVal), c(0,maxVal), col="red")
}, "meanInter_intraNE")

totalInterNE <- connections[,"non.empty.connections"] - intraNE
wilcox.test(intraNE, totalInterNE, alternative="two.sided", paired=TRUE)
maxVal <- max(max(intraNE), max(totalInterNE))
exportAsEPS({
  plot(totalInterNE, intraNE, xlab="Total inter repro connections", ylab="Intra repro connections")
  lines(c(0,maxVal), c(0,maxVal), col="red")
}, "totalInter_intraNE")
