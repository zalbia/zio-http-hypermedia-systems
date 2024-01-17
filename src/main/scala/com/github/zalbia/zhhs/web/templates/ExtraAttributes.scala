package com.github.zalbia.zhhs.web.templates

import zio.http.template.Attributes.PartialAttribute

object ExtraAttributes {
  final def `@click`: PartialAttribute[String]               = PartialAttribute("@click")
  final def ariaAttr(name: String): PartialAttribute[String] = PartialAttribute("aria-" + name)

  final def hxAttr(name: String): PartialAttribute[String] = PartialAttribute("hx-" + name)

  final def roleAttr: PartialAttribute[String] = PartialAttribute("role")

  final def xAttr(name: String): PartialAttribute[String] = PartialAttribute("x-" + name)

  final def crossOriginAttr: PartialAttribute[String] = PartialAttribute("crossorigin")
}
