package com.github.zalbia.zhhs.web.templates

import zio.http.template.Element.PartialElement

object ExtraElements {

  final def allCaps: PartialElement = PartialElement("all-caps")

  final def slot: PartialElement = PartialElement("slot")

  final def subTitle: PartialElement = PartialElement("sub-title")
}
