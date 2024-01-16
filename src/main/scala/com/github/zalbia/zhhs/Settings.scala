package com.github.zalbia.zhhs

import zio.durationInt

object Settings {
  val flashMessageMaxAge: zio.Duration = 10.seconds

  val pageSize = 10
}
