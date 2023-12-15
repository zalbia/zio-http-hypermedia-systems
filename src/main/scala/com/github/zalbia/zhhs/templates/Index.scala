package com.github.zalbia.zhhs.templates

import ExtraAttributes.*
import ExtraElements.*
import com.github.zalbia.zhhs.domain.Contact
import zio.http.template.*
object Index {

  private def searchForm(query: Option[String]): Html =
    form(
      actionAttr := "/contacts",
      methodAttr := "get",
      classAttr  := List("tool-bar"),
      label(forAttr         := "search", "Search Term"),
      input(
        idAttr              := "search",
        typeAttr            := "search",
        nameAttr            := "q",
        valueAttr           := query.getOrElse(""),
        hxAttr("get")       := "/contacts",
        hxAttr("trigger")   := "search, keyup delay:200ms changed",
        hxAttr("target")    := "tbody",
        hxAttr("push-url")  := "true",
        hxAttr("indicator") := "#spinner",
      ),
      img(
        styleAttr           := List("height" -> "20px"),
        idAttr              := "spinner",
        classAttr           := List("htmx-indicator"),
        srcAttr             := "/static/img/spinning-circles.svg",
      ),
      input(typeAttr        := "submit", valueAttr := "Search"),
    )

  private def contactsForm(contacts: List[Contact]): Html =
    form(
      xAttr("data") := "{ selected: [] }",
      template(
        xAttr("if")       := "selected.length > 0",
        div(
          classAttr := List("box", "info", "tool-bar", "fixed", "top"),
          slot(xAttr("text")         := "selected.length"),
          "contacts selected",
          button(
            typeAttr                 := "button",
            classAttr                := List("bad", "bg", "color", "border"),
            `@click`                 := "confirm(`Delete ${selected.length} contacts?`) && htmx.ajax('DELETE', '/contacts', { source: $root, target: document.body })",
            "Delete",
          ),
          hr(ariaAttr("orientation") := "vertical"),
          button(typeAttr            := "button", `@click` := "selected = []", "Cancel"),
        ),
      ),
      table(
        tHead(
          tr(
            th(),
            th("First"),
            th("Last"),
            th("Phone"),
            th("Email"),
            th(),
          )
        ),
        tBody(Rows(contacts)),
      ),
      button(
        hxAttr("delete")  := "/contacts",
        hxAttr("confirm") := "Are you sure you want to delete these contacts?",
        hxAttr("target")  := "body",
        "Delete Selected Contacts",
      ),
    )

  def addContact: Html =
    p(
      a(hrefAttr          := "/contacts/new"),
      span(
        hxAttr("get")     := "/contacts/count",
        hxAttr("trigger") := "revealed",
        img(
          idAttr    := "spinner",
          styleAttr := List("height" -> "20px"),
          classAttr := List("htmx-indicator"),
          srcAttr   := "/static/img/spinning-circles.svg",
        ),
      ),
    )

  def apply(query: Option[String] = None, contacts: List[Contact] = List.empty, flashedMessages: List[String] = List.empty): Html =
    Layout(
      searchForm(query) ++
        contactsForm(contacts) ++
        addContact,
      flashedMessages,
    )
}
