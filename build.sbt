name := "system-notification-service"

version := "0.0.1"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.17",
  "com.typesafe.akka" %% "akka-remote" % "2.4.17",
  "org.reactivemongo" %% "reactivemongo" % "0.12.1",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "io.kamon" %% "kamon-akka-2.4" % "0.6.6",
  "io.kamon" %% "kamon-statsd" % "0.6.6",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.17" % "test",
  "org.mockito" % "mockito-core" % "2.2.9" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % "test"
)

parallelExecution in Test := false

assemblyJarName in assembly := s"system-notification-service-${version.value}.jar"

// Thanks to https://gist.github.com/colestanfield/fac042d3108b0c06e952 workaround for Kamon aop.xml merge strategy
val defaultMergeStrategy: String => sbtassembly.MergeStrategy = {
  case x if Assembly.isConfigFile(x) =>
    MergeStrategy.concat
  case PathList(ps @ _*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
    MergeStrategy.rename
  case PathList("META-INF", xs @ _*) =>
    (xs map {_.toLowerCase}) match {
      case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
        MergeStrategy.discard
      case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
        MergeStrategy.discard
      case "plexus" :: xs =>
        MergeStrategy.discard
      case "services" :: xs =>
        MergeStrategy.filterDistinctLines
      case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
        MergeStrategy.filterDistinctLines
      case _ => MergeStrategy.deduplicate
    }
  case _ => MergeStrategy.deduplicate
}

// Create a new MergeStrategy for aop.xml files
val aopMerge: sbtassembly.MergeStrategy = new sbtassembly.MergeStrategy {
  val name = "aopMerge"
  import scala.xml._
  import scala.xml.dtd._

  def apply(tempDir: File, path: String, files: Seq[File]): Either[String, Seq[(File, String)]] = {
    val dt = DocType("aspectj", PublicID("-//AspectJ//DTD//EN", "http://www.eclipse.org/aspectj/dtd/aspectj.dtd"), Nil)
    val file = MergeStrategy.createMergeTarget(tempDir, path)
    val xmls: Seq[Elem] = files.map(XML.loadFile)
    val aspectsChildren: Seq[Node] = xmls.flatMap(_ \\ "aspectj" \ "aspects" \ "_")
    val weaverChildren: Seq[Node] = xmls.flatMap(_ \\ "aspectj" \ "weaver" \ "_")
    val options: String = xmls.map(x => (x \\ "aspectj" \ "weaver" \ "@options").text).mkString(" ").trim
    val weaverAttr = if (options.isEmpty) Null else new UnprefixedAttribute("options", options, Null)
    val aspects = new Elem(null, "aspects", Null, TopScope, false, aspectsChildren: _*)
    val weaver = new Elem(null, "weaver", weaverAttr, TopScope, false, weaverChildren: _*)
    val aspectj = new Elem(null, "aspectj", Null, TopScope, false, aspects, weaver)
    XML.save(file.toString, aspectj, "UTF-8", xmlDecl = false, dt)
    IO.append(file, IO.Newline.getBytes(IO.defaultCharset))
    Right(Seq(file -> path))
  }
}

// Use defaultMergeStrategy with a case for aop.xml
// I like this better than the inline version mentioned in assembly's README
val customMergeStrategy: String => sbtassembly.MergeStrategy = {
  case PathList("META-INF", "aop.xml") =>
    aopMerge
  case s =>
    defaultMergeStrategy(s)
}

assemblyMergeStrategy  in assembly := customMergeStrategy