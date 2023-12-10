package com.github.zalbia.zhhs.templates

import zio.http.template.Element.PartialElement

object ExtraElements {
  final def allCaps: PartialElement = PartialElement("all-caps")

  final def subTitle: PartialElement = PartialElement("sub-title")
}
