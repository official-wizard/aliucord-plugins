// include(":MyFirstPlugin")

include(":BetterMarkdown")
project(":BetterMarkdown").projectDir = File("./plugins/BetterMarkdown")

include(":GreenText")
project(":GreenText").projectDir = File("./plugins/GreenText")

rootProject.name = "aliucord-plugins"
