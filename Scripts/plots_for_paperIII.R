setwd("~/github/fxpl/notebooks")
rm(list=ls())

# TODO: Städa

library(dunn.test)  # TODO: Är det här biblioteket jag vill använda?

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
# objects: the type of object that the data concerns; the label on the y axis
###############################################################################
logHist <- function(data, specifier="", objects="Notebooks") {
  histData <- hist(data, plot=FALSE)
  lineWidth <- 2
  lineEnd <- 2
  
  setEPS()
  postscript(paste(outputDir, "/log_hist_", specifier, ".eps", sep=""))
  plot(histData$mids, histData$count, log='y',
       type='h', lwd=lineWidth, lend=lineEnd,
       xlab="", ylab=objects
  )
  dev.off()
}

histogram <- function(data, specifier="") {
  setEPS()
  postscript(paste(outputDir, "/", specifier, ".eps", sep=""))
  hist(data, main="", xlab="", ylab="Notebooks")
  dev.off() 
}

# NOTEBOOK DATA
codeCells <- read.csv("Output/code_cells.csv", header=TRUE, stringsAsFactors=FALSE)
sizes <- read.csv("Output/notebook_sizes.csv", header=TRUE)
loc <- read.csv("Output/loc.csv", header=TRUE, stringsAsFactors=FALSE)
languages <- read.csv("Output/languages.csv", header=TRUE, stringsAsFactors=FALSE)
snippetOccurrencesA <- read.csv("Output/filesPerSnippetA.csv", header=FALSE, stringsAsFactors=FALSE)
snippetOccurrencesNE <- read.csv("Output/filesPerSnippetNE.csv", header=FALSE, stringsAsFactors=FALSE)
cloneFreq <- read.csv("Output/cloneFrequency.csv", header=TRUE, stringsAsFactors=FALSE)
connections <-read.csv("Output/connections.csv", header=TRUE, stringsAsFactors=FALSE)
nbData = merge(languages, cloneFreq, by="file")
nbData = merge(codeCells, nbData, by="file")

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
postscript(paste(outputDir, "/languages.eps", sep=""))
pie(lang_percentages, labels=NA,  col=colors)
legend(x="topright", legend=labels, fill=colors)
dev.off()


# CLONE FREQUENCY
# Snippet occurences distribution
logHist(snippetOccurrencesA[,2], specifier="snippetOccurencesA", objects="Snippets")
logHist(snippetOccurrencesNE[,2], specifier="snippetOccurencesNE", objects="Snippets")

# Clone frequencies
frequencies <- nbData[,"clone.frequency"]
print("Clone frequency")
printMeanAndPercentiles(frequencies)
histogram(frequencies, "clone_frequencyA") # Non-parametric test needed (Spearman!?) Liknande distribution för alla språk -> Kurskal-Wallis lämpligt för att jämföra medianer
frequencies <- nbData[,"non.empty.clone.frequency"]
print("Clone frequency, empty snippets excluded")
printMeanAndPercentiles(frequencies)
histogram(frequencies, "clone_frequencyNE")

# Correlation with size (=number of code cells)
postscript(paste(outputDir, "/cells_frequencyA.eps", sep=""))
plot(clone.frequency~code.cells, data=nbData, xlab="Number of code cells", ylab="Clone frequency")
dev.off()
print("Correlation with size (all clones):")
cor.test(nbData$code.cells, nbData$clone.frequency, alternative="two.sided", method="spearman")
postscript(paste(outputDir, "/cells_frequencyNE.eps", sep=""))
plot(non.empty.clone.frequency~code.cells, data=nbData, xlab="Number of code cells", ylab="Clone frequency")
dev.off()
print("Correlation with size (non-empty clones):")
cor.test(nbData$code.cells, nbData$non.empty.clone.frequency, alternative="two.sided", method="spearman")

# Association with language
nbDataKnownLang <- nbData[nbData$language!=" UNKNOWN",]
postscript(paste(outputDir, "/lang_frequencyA.eps", sep=""))
boxplot(clone.frequency~language, data=nbData)
dev.off()
langMdl <- lm(clone.frequency~language, data=nbDataKnownLang)
hist(resid(langMdl))  # Non-parametric test needed (Kruskal-Wallis 1-way ANOVA!? --rapportera median)
plot(langMdl$residuals~langMdl$fitted.values)
kruskal.test(clone.frequency~as.factor(language), data=nbDataKnownLang)
# Post hoc, since kruskal test indicated significant difference
dunn.test(nbDataKnownLang$clone.frequency, g=as.factor(nbDataKnownLang$language), alpha=0.001, method="bonferroni")

postscript(paste(outputDir, "/lang_frequencyNE.eps", sep=""))
boxplot(non.empty.clone.frequency~language, data=nbData)
dev.off()
langMdl <- lm(non.empty.clone.frequency~language, data=nbDataKnownLang)
hist(resid(langMdl))  # Non-parametric test needed (Kruskal-Wallis 1-way ANOVA!? --rapportera median)
plot(langMdl$residuals~langMdl$fitted.values)
kruskal.test(non.empty.clone.frequency~as.factor(language), data=nbDataKnownLang)
# Post hoc, since kruskal test indicated significant difference
dunn.test(nbDataKnownLang$non.empty.clone.frequency, g=as.factor(nbDataKnownLang$language), alpha=0.001, method="bonferroni")


# CONNECTIONS
#intra <- connections[,"intra.repro.connections"] ## Overflowar!
#meanInter <- connections[,"mean.inter.repro.connections"]
intraNE <- connections[,"non.empty.intra.repro.connections"]
meanInterNE <- connections[,"mean.non.empty.inter.repro.connections"]
#wilcox.test(intra, meanInter, alternative="two.sided", paired=TRUE)
wilcox.test(intraNE, meanInterNE, alternative="two.sided", paired=TRUE)

postscript(paste(outputDir, "/inter_intra_ne.eps", sep=""))
plot(meanInterNE, intraNE, xlab="Inter repro connections", ylab="Intra repro connections")
maxVal <- max(max(intraNE), max(meanInterNE))
lines(c(0,maxVal), c(0,maxVal), col="gray")
dev.off()

#postscript(paste(outputDir, "/inter_intra.eps", sep=""))
#plot(meanInter, intra, xlab="Inter repro connections", ylab="Intra repro connections")
#maxVal <- max(max(intra), max(meanInter))
#lines(c(0,maxVal), c(0,maxVal), col="gray")
#dev.off()
