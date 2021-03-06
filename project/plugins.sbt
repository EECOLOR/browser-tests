addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8")

libraryDependencies += "org.qirx" %% "sbt-release-custom-steps" % "0.2"

resolvers += "Rhinofly Internal Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-release-local"

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.3.0")
