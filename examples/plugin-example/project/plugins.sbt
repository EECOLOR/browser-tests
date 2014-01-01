addSbtPlugin("org.qirx" % "browser-tests-plugin" % "0.6-SNAPSHOT")

addSbtPlugin("org.qirx" % "sbt-webjar" % "0.3")

resolvers += Resolver.url("Rhinofly Internal Plugin Repository", url("http://maven-repository.rhinofly.net:8081/artifactory/libs-release-local"))(Resolver.ivyStylePatterns)
