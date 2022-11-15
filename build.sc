import mill._
import mill.scalalib._
import mill.define.{TaskModule, Command}
import mill.scalalib.publish._
import mill.scalalib.scalafmt._
import mill.scalalib.TestModule.Utest
import coursier.maven.MavenRepository
import $file.dependencies.chisel3.build
import $file.dependencies.firrtl.build
import $file.dependencies.treadle.build
import $file.dependencies.chiseltest.build
import $file.dependencies.arithmetic.common
import $file.dependencies.tilelink.common
import $file.common

object v {
  val scala = "2.13.6"
  val utest = ivy"com.lihaoyi::utest:latest.integration"
  val mainargs = ivy"com.lihaoyi::mainargs:0.3.0"
  // for arithmetic
  val upickle = ivy"com.lihaoyi::upickle:latest.integration"
  val osLib = ivy"com.lihaoyi::os-lib:latest.integration"
  val bc = ivy"org.bouncycastle:bcprov-jdk15to18:latest.integration"
  val spire = ivy"org.typelevel::spire:latest.integration"
  val evilplot = ivy"io.github.cibotech::evilplot:latest.integration"
}

object myfirrtl extends dependencies.firrtl.build.firrtlCrossModule(v.scala) {
  override def millSourcePath = os.pwd / "dependencies" / "firrtl"

  override val checkSystemAntlr4Version = false
  override val checkSystemProtocVersion = false
  override val protocVersion = os.proc("protoc", "--version").call().out.text.dropRight(1).split(' ').last
  override val antlr4Version = os.proc("antlr4").call().out.text.split('\n').head.split(' ').last
}

object mytreadle extends dependencies.treadle.build.treadleCrossModule(v.scala) {
  override def millSourcePath = os.pwd / "dependencies" / "treadle"

  def firrtlModule: Option[PublishModule] = Some(myfirrtl)
}

object mychisel3 extends dependencies.chisel3.build.chisel3CrossModule(v.scala) {
  override def millSourcePath = os.pwd / "dependencies" / "chisel3"

  def firrtlModule: Option[PublishModule] = Some(myfirrtl)

  def treadleModule: Option[PublishModule] = Some(mytreadle)

  def chiseltestModule: Option[PublishModule] = Some(mychiseltest)
}

object mychiseltest extends dependencies.chiseltest.build.chiseltestCrossModule(v.scala) {
  override def millSourcePath = os.pwd / "dependencies" / "chiseltest"

  def chisel3Module: Option[PublishModule] = Some(mychisel3)

  def treadleModule: Option[PublishModule] = Some(mytreadle)
}

object myarithmetic extends dependencies.arithmetic.common.ArithmeticModule {
  override def millSourcePath = os.pwd / "dependencies" / "arithmetic" / "arithmetic"

  def scalaVersion = T {
    v.scala
  }

  def chisel3Module: Option[PublishModule] = Some(mychisel3)

  def chisel3PluginJar = T {
    Some(mychisel3.plugin.jar())
  }

  def chiseltestModule = Some(mychiseltest)

  def upickle: T[Dep] = v.upickle

  def osLib: T[Dep] = v.osLib

  def spire: T[Dep] = v.spire

  def evilplot: T[Dep] = v.evilplot

  def bc: T[Dep] = v.bc

  def utest: T[Dep] = v.utest
}

object mytilelink extends dependencies.tilelink.common.TileLinkModule {
  override def millSourcePath = os.pwd / "dependencies" / "tilelink" / "tilelink"

  def scalaVersion = T {
    v.scala
  }

  def chisel3Module: Option[PublishModule] = Some(mychisel3)

  def chisel3PluginJar = T {
    Some(mychisel3.plugin.jar())
  }
}

object vector extends common.VectorModule with ScalafmtModule {
  m =>
  def millSourcePath = os.pwd / "v"

  def scalaVersion = T {
    v.scala
  }

  def chisel3Module = Some(mychisel3)

  def chisel3PluginJar = T {
    Some(mychisel3.plugin.jar())
  }

  def chiseltestModule = Some(mychiseltest)

  def arithmeticModule = Some(myarithmetic)

  def tilelinkModule = Some(mytilelink)

  def utest: T[Dep] = v.utest
}

object tests extends Module {
  object elaborate extends ScalaModule {
    override def scalacPluginClasspath = T {
      Agg(mychisel3.plugin.jar())
    }

    override def scalacOptions = T {
      super.scalacOptions() ++ Some(mychisel3.plugin.jar()).map(path => s"-Xplugin:${path.path}") ++ Seq("-Ymacro-annotations")
    }

    override def scalaVersion = v.scala

    override def moduleDeps = Seq(vector)

    override def ivyDeps = T {
      Seq(
        v.mainargs
      )
    }

    def elaborate = T {
      mill.modules.Jvm.runSubprocess(
        finalMainClass(),
        runClasspath().map(_.path),
        Seq.empty,
        Map.empty,
        Seq(
          "--dir", T.dest.toString,
        ),
        workingDir = T.dest
      )
      PathRef(T.dest)
    }

    def chiselAnno = T {
      os.walk(elaborate().path).collectFirst { case p if p.last.endsWith("anno.json") => p }.map(PathRef(_)).get
    }

    def chirrtl = T {
      os.walk(elaborate().path).collectFirst { case p if p.last.endsWith("fir") => p }.map(PathRef(_)).get
    }

    def topName = T {
      chirrtl().path.last.split('.').head
    }

  }

  object mfccompile extends Module {

    def compile = T {
      os.proc("firtool",
        elaborate.chirrtl().path,
        s"--annotation-file=${elaborate.chiselAnno().path}",
        "-disable-infer-rw",
        "-dedup",
        "-O=debug",
        "--split-verilog",
        "--preserve-values=named",
        "--output-annotation-file=mfc.anno.json",
        s"-o=${T.dest}"
      ).call(T.dest)
      PathRef(T.dest)
    }

    def rtls = T {
      os.read(compile().path / "filelist.f").split("\n").map(str =>
        try {
          os.Path(str)
        } catch {
          case e: IllegalArgumentException if e.getMessage.contains("is not an absolute path") =>
            compile().path / str.stripPrefix("./")
        }
      ).filter(p => p.ext == "v" || p.ext == "sv").map(PathRef(_)).toSeq
    }

    def annotations = T {
      os.walk(compile().path).filter(p => p.last.endsWith("mfc.anno.json")).map(PathRef(_))
    }
  }

  object emulator extends Module {

    object spike extends Module {
      override def millSourcePath = os.pwd / "dependencies" / "riscv-isa-sim"

      // ask make to cache file.
      def compile = T.persistent {
        os.proc(millSourcePath / "configure", "--prefix", "/usr", "--without-boost", "--without-boost-asio", "--without-boost-regex", "--enable-commitlog").call(
          T.ctx.dest, Map(
            "CC" -> "clang",
            "CXX" -> "clang++",
            "AR" -> "llvm-ar",
            "RANLIB" -> "llvm-ranlib",
            "LD" -> "lld",
          )
        )
        os.proc("make", "-j", Runtime.getRuntime().availableProcessors()).call(T.ctx.dest)
        PathRef(T.ctx.dest)
      }
    }

    def csrcDir = T.source {
      PathRef(millSourcePath / "src")
    }

    def allCHeaderFiles = T.sources {
      os.walk(csrcDir().path).filter(_.ext == "h").map(PathRef(_))
    }

    def allCSourceFiles = T.sources {
      Seq(
        "main.cc",
        "vbridge.cc",
        "rtl_event.cc",
        "spike_event.cc",
        "vbridge_impl.cc",
      ).map(f => PathRef(csrcDir().path / f))
    }

    def verilatorConfig = T {
      val traceConfigPath = T.dest / "verilator.vlt"
      os.write(
        traceConfigPath,
        "`verilator_config\n" +
          ujson.read(mfccompile.annotations().collectFirst(f => os.read(f.path)).get).arr.flatMap {
            case anno if anno("class").str == "chisel3.experimental.Trace$TraceAnnotation" =>
              Some(anno("target").str)
            case _ => None
          }.toSet.map { t: String =>
            val s = t.split('|').last.split("/").last
            val M = s.split(">").head.split(":").last
            val S = s.split(">").last
            s"""//$t\npublic_flat_rd -module "$M" -var "$S""""
          }.mkString("\n")
      )
      PathRef(traceConfigPath)
    }

    def verilatorArgs = T {
      Seq(
        // format: off
        "--x-initial unique",
        "--output-split 100000",
        "--max-num-width 1048576",
        "--vpi"
        // format: on
      )
    }

    def topName = T {
      "V"
    }

    def verilatorThreads = T {
      8
    }

    def CMakeListsString = T {
      // format: off
      s"""cmake_minimum_required(VERSION 3.20)
         |project(emulator)
         |find_package(args REQUIRED)
         |find_package(glog REQUIRED)
         |find_package(fmt REQUIRED)
         |
         |find_package(verilator)
         |set(CMAKE_CXX_STANDARD 17)
         |set(CMAKE_CXX_COMPILER_ID "clang")
         |set(CMAKE_C_COMPILER "clang")
         |set(CMAKE_CXX_COMPILER "clang++")
         |
         |find_package(Threads)
         |set(THREADS_PREFER_PTHREAD_FLAG ON)
         |add_executable(emulator
         |${allCSourceFiles().map(_.path).mkString("\n")}
         |)
         |target_include_directories(emulator PRIVATE ${(spike.millSourcePath / "riscv").toString})
         |target_include_directories(emulator PRIVATE ${(spike.millSourcePath / "fesvr").toString})
         |target_include_directories(emulator PRIVATE ${(spike.millSourcePath / "softfloat").toString})
         |target_include_directories(emulator PRIVATE ${spike.compile().path.toString})
         |
         |target_include_directories(emulator PUBLIC ${csrcDir().path.toString})
         |
         |target_link_directories(emulator PRIVATE ${spike.compile().path.toString})
         |target_link_libraries(emulator PUBLIC $${CMAKE_THREAD_LIBS_INIT})
         |target_link_libraries(emulator PUBLIC riscv fmt glog)  # note that libargs is header only, nothing to link
         |
         |verilate(emulator
         |  SOURCES
         |  ${mfccompile.rtls().map(_.path.toString).mkString("\n")}
         |  ${verilatorConfig().path.toString}
         |  TRACE_FST
         |  TOP_MODULE ${topName()}
         |  PREFIX V${topName()}
         |  OPT_FAST
         |  THREADS ${verilatorThreads()}
         |  VERILATOR_ARGS ${verilatorArgs().mkString(" ")}
         |)
         |""".stripMargin
      // format: on
    }

    def cmakefileLists = T {
      val path = T.dest / "CMakeLists.txt"
      os.write.over(path, CMakeListsString())
      PathRef(T.dest)
    }

    def buildDir = T {
      PathRef(T.dest)
    }

    def config = T {
      mill.modules.Jvm.runSubprocess(Seq("cmake", "-G", "Ninja", "-S", cmakefileLists().path, "-B", buildDir().path).map(_.toString), Map[String, String](), T.dest)
    }

    def elf = T {
      // either rtl or testbench change should trigger elf rebuild
      mfccompile.rtls()
      allCSourceFiles()
      allCHeaderFiles()
      config()
      mill.modules.Jvm.runSubprocess(Seq("ninja", "-C", buildDir().path).map(_.toString), Map[String, String](), buildDir().path)
      PathRef(buildDir().path / "emulator")
    }
  }

  object cases extends Module {

    trait Case extends Module {
      def name: T[String] = millSourcePath.last

      def sources = T.sources {
        millSourcePath
      }

      def allSourceFiles = T {
        Lib.findSourceFiles(sources(), Seq("S", "s", "c", "cpp")).map(PathRef(_))
      }

      def linkScript: T[PathRef] = T {
        os.write(T.ctx.dest / "linker.ld",
          s"""
             |SECTIONS
             |{
             |  . = 0x1000;
             |  .text.start : { *(.text.start) }
             |}
             |""".stripMargin)
        PathRef(T.ctx.dest / "linker.ld")
      }

      def compile: T[PathRef] = T {
        os.proc(Seq("clang-rv32", "-o", name() + ".elf", "-march=rv32gcv", s"-T${linkScript().path}") ++ allSourceFiles().map(_.path.toString)).call(T.ctx.dest)
        os.proc(Seq("llvm-objcopy", "-O", "binary", "--only-section=.text", name() + ".elf", name())).call(T.ctx.dest)
        PathRef(T.ctx.dest / name())
      }
    }

    object smoketest extends Case
  }

  trait Case extends TaskModule {
    override def defaultCommandName() = "run"

    def bin: cases.Case

    def run(args: String*) = T.command {
      val proc = os.proc(Seq(tests.emulator.elf().path.toString, "--bin", bin.compile().path.toString, "--wave", (T.dest / "wave").toString) ++ args)
      T.log.info(s"run test: ${bin.name} with:\n ${proc.command.map(_.value.mkString(" ")).mkString(" ")}")
      proc.call()
      PathRef(T.dest)
    }
  }

  object smoketest extends Case {
    def bin = cases.smoketest
  }
}
