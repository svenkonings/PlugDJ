sbtVersion := "0.13.13"
scalaVersion := "2.11.8"

resolvers += Resolver.jcenterRepo

enablePlugins(AndroidApp)
android.useSupportVectors

buildToolsVersion := Some("25.0.2")
minSdkVersion := "16"
platformTarget := "android-25"

name := "PlugDJ"
applicationId := "dj.plug.plugdj"
version := "0.2"
versionCode := Some(2)

useProguard := true
proguardOptions ++= io.Source.fromFile("proguard-project.txt").getLines.toSeq
shrinkResources := true

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

libraryDependencies ++= Seq(
  "com.android.support" % "appcompat-v7" % "25.1.1",
  "com.google.android.exoplayer" % "exoplayer" % "r2.2.0",
  "com.neovisionaries" % "nv-websocket-client" % "1.31" withSources() withJavadoc(),
  "com.squareup.picasso" % "picasso" % "2.5.2" withSources() withJavadoc())
