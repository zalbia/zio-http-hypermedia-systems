package com.github.zalbia.zhhs

import zio.Duration
import zio.http.{Cookie, Response}

package object web {
  implicit final class CookieResponseOps(private val response: Response) extends AnyVal {
    def addExpiringFlashMessage(
      message: String,
      maxAge: Option[Duration] = Some(Settings.flashMessageMaxAge),
    ): Response =
      response.addCookie(
        Cookie.Response(
          name = "zio-http-flash",
          content = message,
          maxAge = maxAge,
        )
      )
  }
}
