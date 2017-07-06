library(ggplot2)

data <- read.csv("solver_execution_time.log", header=TRUE)
data$n <- as.numeric(data$n)
data$k <- as.numeric(data$k)
data$t <- as.numeric(data$t)

ggplot(subset(data), aes(n+k, t, group=sat)) +
  geom_point(aes(colour=sat))
