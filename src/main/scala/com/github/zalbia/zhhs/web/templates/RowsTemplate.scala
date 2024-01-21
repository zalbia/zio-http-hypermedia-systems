package com.github.zalbia.zhhs.web.templates

import com.github.zalbia.zhhs.domain.Contact
import ExtraAttributes.*
import com.github.zalbia.zhhs.Settings
import zio.http.template.*

object RowsTemplate {

  def apply(contacts: List[Contact], page: Int): List[Dom] =
    contactRows(contacts) ++ navigation(contacts, page)

  private def contactRows(contacts: List[Contact]) =
    contacts.map { contact =>
      val firstname = contact.firstname.getOrElse("N/A")
      val lastname  = contact.lastname.getOrElse("N/A")
      val phone     = contact.phone.getOrElse("N/A")
      tr(
        td(
          input(
            typeAttr       := "checkbox",
            nameAttr       := "selected_contact_ids",
            valueAttr      := contact.id,
            xAttr("model") := "selected",
          )
        ),
        td(firstname),
        td(lastname),
        td(phone),
        td(contact.email),
        td(
          div(
            dataAttr("overflow-menu")(""),
            button(
              typeAttr             := "button",
              ariaAttr("haspopup") := "menu",
              ariaAttr("controls") := s"contact-menu-${contact.id}",
              "Options",
            ),
            div(
              roleAttr             := "menu",
              hiddenAttr           := "hidden",
              id                   := s"contact-menu-${contact.id}",
              a(roleAttr          := "menu-item", hrefAttr := s"/contacts/${contact.id}/edit", "Edit"),
              a(roleAttr          := "menu-item", hrefAttr := s"/contacts/${contact.id}", "View"),
              a(
                roleAttr          := "menu-item",
                hrefAttr          := "#",
                hxAttr("delete")  := s"/contacts/${contact.id}",
                hxAttr("confirm") := "Are you sure you want to delete this contact?",
                hxAttr("swap")    := "outerHTML swap:1s",
                hxAttr("target")  := "closest tr",
                "Delete",
              ),
            ),
          )
        ),
      )
    }

  def navigation(contacts: List[Contact], page: Int): Option[Dom] =
    Option.when(contacts.length == Settings.pageSize) {
      tr(
        td(
          colSpanAttr := "5",
          styleAttr   := List("text-align" -> "center"),
          button(
            hxAttr("target") := "closest tr",
            hxAttr("swap")   := "outerHTML",
            hxAttr("select") := "tbody > tr",
            hxAttr("get")    := s"/contacts?page=${page + 1}",
            "Load More",
          ),
        )
      )
    }
}
