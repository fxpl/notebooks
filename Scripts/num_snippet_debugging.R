setwd("~/github/fxpl/notebooks")

codeCells <- read.csv("Output/code_cells.csv", header=TRUE, stringsAsFactors=FALSE)
snippets <-read.csv("Output/snippetsPerFile.csv", header=TRUE, stringsAsFactors=FALSE)

counts <- merge(codeCells, snippets, by="file")
diff <- counts[counts$code.cells!=counts$snippets,]
diff$snippets/diff$code.cells
