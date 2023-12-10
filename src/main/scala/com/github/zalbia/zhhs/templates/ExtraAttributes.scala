package com.github.zalbia.zhhs.templates

import zio.http.template.Attributes.PartialAttribute

object ExtraAttributes {
  final def htmxAttr(name: String): PartialAttribute[String] = PartialAttribute("hx-" + name)
}
