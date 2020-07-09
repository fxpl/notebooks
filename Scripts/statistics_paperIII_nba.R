source("functions_paperIII.R")
setwd("..")
outputDir <- "OutputNBA"

# NOTEBOOK DATA
codeCells <- read.csv(paste(outputDir, "/code_cells.csv", sep=""), header=TRUE, stringsAsFactors=FALSE)
sizes <- read.csv(paste(outputDir, "/notebook_sizes.csv", sep=""), header=TRUE)
loc <- read.csv(paste(outputDir, "/loc.csv", sep=""), header=TRUE, stringsAsFactors=FALSE)
languages <- read.csv(paste(outputDir, "/languages.csv", sep=""), header=TRUE, stringsAsFactors=FALSE)
snippetOccurrencesA <- read.csv(paste(outputDir, "/filesPerSnippetA.csv", sep=""), header=TRUE, stringsAsFactors=FALSE)
snippetOccurrencesNE <- read.csv(paste(outputDir, "/filesPerSnippetNE.csv", sep=""), header=TRUE, stringsAsFactors=FALSE)
notebookOccurencesA <- read.csv(paste(outputDir, "/nb_clone_distrA.csv", sep="") , header=FALSE)
notebookOccurencesNE <- read.csv(paste(outputDir, "/nb_clone_distrNE.csv", sep=""), header=FALSE)
cloneFreq <- read.csv(paste(outputDir, "/cloneFrequency.csv", sep=""), header=TRUE, stringsAsFactors=FALSE)
connections <-read.csv(paste(outputDir, "/connections.csv", sep=""), header=TRUE, stringsAsFactors=FALSE)
nbData <- merge(languages, cloneFreq, by="file")
nbData <- merge(codeCells, nbData, by="file")
nbData$non.empty.snippets <- nbData$clones + nbData$unique - nbData$empty

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


# CLONES
# Snippet occurences distribution
logHist(snippetOccurrencesA$count, specifier="snippetOccurencesA", objects="Snippets")
logHist(snippetOccurrencesNE$count, specifier="snippetOccurencesNE", objects="Snippets")
# Notebook clone occurrences distribution
logHist(notebookOccurencesA[,1], specifier="notebookOccurencesA")
logHist(notebookOccurencesNE[,1], specifier="notebookOccurencesNE")

# Clone sizes (LOC) distribution
cloneGroupsA <- snippetOccurrencesA[snippetOccurrencesA$count>1,]
print("LOC in clone groups, with empty snippet")
printMeanAndPercentiles(cloneGroupsA$LOC)
cloneSizesA <- do.call(c, apply(cloneGroupsA, 1, repeatSecondColumn))
print("LOC in clone instances, with empty snippet")
printMeanAndPercentiles(as.integer(cloneSizesA))
logHist(cloneGroupsA$LOC, specifier="cloneGroupSizesA", objects="Clone groups")
logHist(as.integer(cloneSizesA), specifier="cloneSizesA", objects="Clones")
cloneGroupsNE <- snippetOccurrencesNE[snippetOccurrencesNE$count>1,]
print("LOC in clone groups, without empty snippet")
printMeanAndPercentiles(cloneGroupsNE[,2])
cloneSizesNE <- do.call(c, apply(cloneGroupsNE, 1, repeatSecondColumn))
print("LOC in clone instances, without empty snippet")
printMeanAndPercentiles(as.integer(cloneSizesNE))
logHist(cloneGroupsNE[,2], specifier="cloneGroupSizesNE", objects="Clone groups")
logHist(as.integer(cloneSizesNE), specifier="cloneSizesNE", objects="Clones")

# Clone frequency
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
nbDataKnownLang <- nbData[nbData$language!=" UNKNOWN",]
exportAsEPS(boxplot(clone.frequency~language, data=nbDataKnownLang), "lang_frequencyA")
checkLM(nbDataKnownLang$clone.frequency, as.factor(nbDataKnownLang$language))
print("Correlation with language (all clones):")
kruskalWallisWithPost(nbDataKnownLang$clone.frequency, as.factor(nbDataKnownLang$language))

exportAsEPS(boxplot(non.empty.clone.frequency~language, data=nbDataKnownLang), "lang_frequencyNE")
checkLM(nbDataKnownLang$non.empty.clone.frequency, as.factor(nbDataKnownLang$language))
print("Correlation with language (non-empty clones):")
kruskalWallisWithPost(nbDataKnownLang$non.empty.clone.frequency, as.factor(nbDataKnownLang$language))


# CONNECTIONS
connection_analysis(connections)
