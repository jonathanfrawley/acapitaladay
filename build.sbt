lazy val server = (project in file("server")).settings(commonSettings).settings(
  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  pipelineStages := Seq(digest, gzip),
  // triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
  libraryDependencies ++= Seq(
    "com.vmunier" %% "scalajs-scripts" % "1.1.1",
    guice,
    specs2 % Test
  ),
  // Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
  EclipseKeys.preTasks := Seq(compile in Compile)
).enablePlugins(PlayScala).
  dependsOn(sharedJvm)

lazy val client = (project in file("client")).settings(commonSettings).settings(
  scalaJSUseMainModuleInitializer := true,
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.4",
    "io.github.cquiroz" %%% "scala-java-time" % "2.0.0-M12",
    "be.doeraene" %%% "scalajs-jquery" % "0.9.1",
    "com.thoughtworks.binding" %%% "dom" % "latest.release",
    "com.thoughtworks.binding" %%% "binding" % "latest.release",
    "com.thoughtworks.binding" %%% "futurebinding" % "latest.release",
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
  )
).enablePlugins(ScalaJSPlugin, ScalaJSWeb).dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).settings(commonSettings)
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js


lazy val scripts = (project in file("scripts")).settings(
  libraryDependencies ++= Seq(
    "net.ruippeixotog" %% "scala-scraper" % "2.1.0",
    "com.typesafe.play" %% "play-json" % "2.6.6"
))

lazy val commonSettings = Seq(
  scalaVersion := "2.12.4",
  organization := "com.9lines"
)

// loads the server project at sbt startup
onLoad in Global := (onLoad in Global).value andThen {s: State => "project server" :: s}


