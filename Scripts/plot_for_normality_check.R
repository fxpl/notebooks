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