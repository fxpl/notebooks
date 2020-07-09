source("functions_paperIII.R")
setwd("..")
outputDir <- "OutputSCC"

cloneFreq <- read.csv(paste(outputDir, "/cloneFrequency.csv", sep=""), header=TRUE, stringsAsFactors=FALSE)
connections <-read.csv(paste(outputDir, "/connections.csv", sep=""), header=TRUE, stringsAsFactors=FALSE)
loc <- read.csv(paste(outputDir, "/clones_loc.csv", sep=""), header=TRUE)

# CLONES
# Clone sizes (LOC) distribution
print("LOC in clone instances")
printMeanAndPercentiles(as.integer(loc[,1]))
logHist(loc[,1], specifier="cloneSizesNE", objects="Clones")

# Clone frequency
frequencies <- cloneFreq[,"non.empty.clone.frequency"]
print("Clone frequency")
printMeanAndPercentiles(frequencies)
histogram(frequencies, "clone_frequencyNE")

# Correlation with size (=number of code cells)
cloneFreq$non.empty.snippets <- cloneFreq$unique + cloneFreq$clones - cloneFreq$empty
exportAsEPS(plot(non.empty.clone.frequency~non.empty.snippets, data=cloneFreq, xlab="Number of code cells", ylab="Clone frequency"),
            "cells_frequencyNE")
print("Correlation with size (non-empty clones):")
cor.test(cloneFreq$non.empty.snippets, cloneFreq$non.empty.clone.frequency, alternative="two.sided", method="spearman")


# CONNECTIONS
connection_analysis(connections)
