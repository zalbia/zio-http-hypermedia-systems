package com.github.zalbia.zhhs.web.templates

import com.github.zalbia.zhhs.domain.Archiver
import com.github.zalbia.zhhs.domain.Archiver.Status
import com.github.zalbia.zhhs.web.templates.ExtraAttributes.*
import zio.http.template.*
import zio.http.template.Html.fromDomElement

object ArchiveUiTemplate {
  def apply(archiverState: Archiver.State): Html =
    div(
      idAttr           := "archive-ui",
      hxAttr("target") := "this",
      hxAttr("swap")   := "outerHTML",
      archiverState.status match {
        case Status.Waiting  =>
          button(
            hxAttr("post") := "/contacts/archive",
            "Download Contact Archive",
          )
        case Status.Running  =>
          div(
            hxAttr("get")     := "/contacts/archive",
            hxAttr("trigger") := "load delay:500ms",
            "Creating Archive...",
            div(
              classAttr := List("progress"),
              div(
                idAttr    := "archive-progress",
                classAttr := List("progress-bar"),
                styleAttr := List(
                  "width" -> (archiverState.progress * 100).toString
                ),
              ),
            ),
          )
        case Status.Complete =>
          a(
            hxAttr("boost") := "false",
            hrefAttr        := "/contacts/archive/file",
            _Attr           := "on load click() me",
            "Archive Downloading! Click here if the download does not start.",
          ) ++
            button(hxAttr("delete") := "/contacts/archive", "Clear Download")
      },
    )
}
