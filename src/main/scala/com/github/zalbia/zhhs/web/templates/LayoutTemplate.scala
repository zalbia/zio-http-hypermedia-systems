package com.github.zalbia.zhhs.web.templates

import ExtraAttributes.*
import ExtraElements.*
import zio.http.template.*

/** Master Layout */
object LayoutTemplate {
  def apply(blockContent: Html, flashMessage: Option[String]): Html =
    html(
      langAttr := "",
      head(
        title("Contact App"),
        link(relAttr("stylesheet"), hrefAttr("https://unpkg.com/missing.css@1.1.1")),
        link(relAttr("stylesheet"), hrefAttr("/static/site.css")),
        script(srcAttr   := "https://unpkg.com/htmx.org@1.9.9"),
        script(srcAttr   := "https://unpkg.com/hyperscript.org@0.9.12"),
        script(srcAttr   := "/static/js/rsjs-menu.js"),
        script(deferAttr := "defer", srcAttr := "https://unpkg.com/alpinejs@3/dist/cdn.min.js"),
      ),
      body(
        hxAttr("boost") := "true",
        main(
          header(
            h1(
              allCaps("contacts.app"),
              subTitle("A Demo Contacts Application"),
            )
          ),
          flashMessage.map(div(classAttr := List("flash"), _)),
          blockContent,
        ),
      ),
    )

  def noFlashedMessages(blockContent: Html): Html = apply(blockContent, flashMessage = None)
}
