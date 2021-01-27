#!/bin/bash

################################################################################
# Convert the giant eps plots to smaller jpg files.
################################################################################

convert -density 300 ../Output/cells_frequencyA.eps ../Output/cells_frequencyA.jpg
convert -density 300 ../Output/cells_frequencyNE.eps ../Output/cells_frequencyNE.jpg
convert -density 300 ../Output/meanInter_intraNE.eps ../Output/meanInter_intraNE.jpg
convert -density 300 ../Output/totalInter_intraNE.eps ../Output/totalInter_intraNE.jpg
