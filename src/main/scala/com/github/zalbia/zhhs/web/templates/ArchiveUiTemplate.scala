package com.github.zalbia.zhhs.web.templates

import com.github.zalbia.zhhs.domain.Archiver
import com.github.zalbia.zhhs.web.templates.ExtraAttributes.*
import zio.http.template.*

object ArchiveUiTemplate {
  def apply(archiverState: Archiver.State): Html =
    div(
      idAttr           := "archive-ui",
      hxAttr("target") := "this",
      hxAttr("swap")   := "outerHTML",
      button(
        hxAttr("post") := "/contacts/archive",
        "Download Contact Archive",
      ),
    )
}
