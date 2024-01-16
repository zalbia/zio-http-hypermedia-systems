package com.github.zalbia.zhhs.domain

import com.github.zalbia.zhhs.Settings
import com.github.zalbia.zhhs.domain.SaveContactError.{EmailAlreadyExistsError, MissingEmailError}
import com.github.zalbia.zhhs.web.templates.ContactFormData
import zio.*
trait ContactService {
  def all: UIO[List[Contact]]

  def delete(id: String): IO[ContactIdDoesNotExist, Unit]

  def save(contact: ContactFormData): IO[SaveContactError, Unit]

  def search(query: Option[String], page: Int): UIO[List[Contact]]
}

object ContactService {
  private val contacts = List(
    Contact("2", Some("Carson"), Some("Gross"), Some("123-456-7890"), "carson@example.comz"),
    Contact("3", Some(""), Some(""), Some(""), "joe@example2.com"),
    Contact("5", Some("Joe"), Some("Blow"), Some("123-456-7890"), "joe@example.com"),
    Contact("6", Some("Joe"), Some("Blow"), Some("123-456-7890"), "joe1@example.com"),
    Contact("7", Some("Joe"), Some("Blow"), Some("123-456-7890"), "joe2@example.com"),
    Contact("8", Some("Joe"), Some("Blow"), Some("123-456-7890"), "joe3@example.com"),
    Contact("9", Some("Joe"), Some("Blow"), Some("123-456-7890"), "joe4@example.com"),
    Contact("10", Some("Joe"), Some("Blow"), Some("123-456-7890"), "joe5@example.com"),
    Contact("11", Some("Joe"), Some("Blow"), Some("123-456-7890"), "joe6@example.com"),
    Contact("12", Some("Joe"), Some("Blow"), Some("123-456-7890"), "joe7@example.com"),
    Contact("13", Some("Joe"), Some("Blow"), Some("123-456-7890"), "joe8@example.com"),
    Contact("14", Some("Joe"), Some("Blow"), Some("123-456-7890"), "joe9@example.com"),
    Contact("15", Some("Joe"), Some("Blow"), Some("123-456-7890"), "joe10@example.com"),
    Contact("16", Some("Joe"), Some("Blow"), Some("123-456-7890"), "joe11@example.com"),
    Contact("17", Some("Joe"), Some("Blow"), Some("123-456-7890"), "joe12@example.com"),
    Contact("18", None, None, None, "restexample1@example.com"),
    Contact("19", None, None, None, "restexample2@example.com"),
  )

  def live: ULayer[ContactService] =
    ZLayer.fromZIO(Ref.make(contacts).map { contactsRef =>
      new ContactService {
        override def all: UIO[List[Contact]] =
          contactsRef.get.map(_.take(Settings.pageSize))

        override def delete(id: String): IO[ContactIdDoesNotExist, Unit] =
          if (contacts.exists(_.id == id))
            contactsRef.update(_.filterNot(_.id == id))
          else
            ZIO.fail(ContactIdDoesNotExist(id))

        override def save(contact: ContactFormData): IO[SaveContactError, Unit] =
          for {
            email     <- validateEmail(contact)
            id        <- nextId
            newContact = Contact(id, contact.firstname, contact.lastname, contact.phone, email)
            _         <- contactsRef.update(_ :+ newContact)
          } yield ()

        private def validateEmail(form: ContactFormData) =
          form.email match {
            case Some(email) =>
              if (contacts.exists(contact => email == contact.email))
                ZIO.fail(EmailAlreadyExistsError(email))
              else
                ZIO.succeed(email)
            case None        =>
              ZIO.fail(MissingEmailError)
          }

        /** Generates IDs by getting the max ID number + 1 */
        private def nextId: UIO[String] =
          contactsRef.get.map(_.map(_.id.toInt).max + 1).map(_.toString)

        override def search(query: Option[String], page: Int): UIO[List[Contact]] = {
          val pageStart = (page - 1) * Settings.pageSize
          val pageEnd   = pageStart + Settings.pageSize
          ZIO.succeed(
            query
              .map { query =>
                contacts
                  .filter { c =>
                    c.firstname.exists(_.toLowerCase.contains(query.toLowerCase)) ||
                    c.lastname.exists(_.toLowerCase.contains(query.toLowerCase)) ||
                    c.phone.exists(_.toLowerCase.contains(query.toLowerCase)) ||
                    c.email.toLowerCase.contains(query.toLowerCase)
                  }
              }
              .getOrElse(contacts)
              .slice(pageStart, pageEnd)
          )
        }
      }
    })
}
