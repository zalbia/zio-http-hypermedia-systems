import sbt.*

object Libraries {

  val zioVersion       = "2.0.21"
  val slf4jVersion     = "2.0.9"
  val zioConfigVersion = "3.0.7"
  val zioHttpVersion   = "3.0.0-RC4" // "3.0.0-RC4+7-f9ffb0b6-SNAPSHOT"

  val zio        = "dev.zio" %% "zio"                % zioVersion
  val prelude    = "dev.zio" %% "zio-prelude"        % "1.0.0-RC22"
  val zioHttp    = "dev.zio" %% "zio-http"           % zioHttpVersion
  val zioLogging = "dev.zio" %% "zio-logging-slf4j2" % "2.1.16"

  val tests = Seq(
    "dev.zio" %% "zio-test"          % zioVersion,
    "dev.zio" %% "zio-test-sbt"      % zioVersion,
    "dev.zio" %% "zio-test-magnolia" % zioVersion,
    "dev.zio" %% "zio-mock"          % "1.0.0-RC12",
  )

  val loggingRuntime = Seq(
    "ch.qos.logback"       % "logback-classic"          % "1.4.14",
    "net.logstash.logback" % "logstash-logback-encoder" % "7.4",
    "org.slf4j"            % "jul-to-slf4j"             % slf4jVersion,
    "org.slf4j"            % "log4j-over-slf4j"         % slf4jVersion,
    "org.slf4j"            % "jcl-over-slf4j"           % slf4jVersion,
    "org.slf4j"            % "slf4j-api"                % slf4jVersion,
  )

}
