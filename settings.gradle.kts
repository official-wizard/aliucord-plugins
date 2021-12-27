// include(":MyFirstPlugin")

include(":BetterMarkdown")
project(":BetterMarkdown").projectDir = File("./BetterMarkdown")

include(":GreenText")
project(":GreenText").projectDir = File("./GreenText")

rootProject.name = "aliucord-plugins"
