package com.github.zalbia.zhhs.domain

import com.github.zalbia.zhhs.Settings
import com.github.zalbia.zhhs.domain.ContactServiceError.*
import zio.*

trait ContactService {
  def all(page: Int): UIO[List[Contact]]

  def count: UIO[Int]

  def delete(id: String): IO[ContactIdDoesNotExist, Unit]

  def deleteAll(contactIds: Set[String]): UIO[Unit]

  def find(contactId: String): UIO[Option[Contact]]
  def save(newContact: NewContactDto): IO[SaveContactError, Unit]

  def search(query: Option[String], page: Int): UIO[List[Contact]]

  def update(updateContact: UpdateContactDto): IO[UpdateContactError, Unit]
}

object ContactService {
  def live: ULayer[ContactService] =
    ZLayer.fromZIO(Ref.make(preloadedContacts).map { contactsRef =>
      new ContactService {
        override def all(page: Int): UIO[List[Contact]] = {
          val pageStart = (page - 1) * Settings.pageSize
          val pageEnd   = pageStart + Settings.pageSize
          contactsRef.get.map(_.take(Settings.pageSize).slice(pageStart, pageEnd).toList)
        }

        override def count: UIO[Int] =
          contactsRef.get.map(_.length)

        override def delete(id: String): IO[ContactIdDoesNotExist, Unit] =
          contactsRef
            .modify { contacts =>
              if (contacts.exists(_.id == id))
                (true, contacts.filterNot(_.id == id))
              else
                (false, contacts)
            }
            .flatMap(contactIdExists => ZIO.unless(contactIdExists)(ZIO.fail(ContactIdDoesNotExist(id))))
            .unit

        override def deleteAll(deletedIds: Set[String]): UIO[Unit] =
          contactsRef.update(_.filterNot(contact => deletedIds(contact.id)))

        override def find(contactId: String): UIO[Option[Contact]] =
          contactsRef.get.map(_.find(_.id == contactId))

        override def save(newContactDto: NewContactDto): IO[SaveContactError, Unit] =
          contactsRef
            .modify { contacts =>
              newContactDto.email match {
                case None        =>
                  (Some(MissingEmailError), contacts)
                case Some(email) =>
                  val emailAlreadyExists = contacts.exists(contact => email == contact.email)
                  if (emailAlreadyExists)
                    (Some(EmailAlreadyExistsError(email)), contacts)
                  else {
                    // Generates IDs by getting the max ID number + 1. Unsafe use of toInt!!!
                    // We're getting away with it as the preloaded ID's are numbers by convention.
                    val nextId     = (contacts.map(_.id.toInt).max + 1).toString
                    val newContact = Contact(
                      id = nextId,
                      firstname = newContactDto.firstname,
                      lastname = newContactDto.lastname,
                      phone = newContactDto.phone,
                      email = email,
                    )
                    (None, contacts :+ newContact)
                  }
              }
            }
            .flatMap(ZIO.fromOption(_).flip)
            .unit

        override def update(update: UpdateContactDto): IO[UpdateContactError, Unit] =
          contactsRef
            .modify { contacts =>
              val oldContact = contacts.find(_.id == update.contactId)
              (oldContact, update.email) match {
                case (None, _)                              =>
                  (Some(ContactIdDoesNotExist(update.contactId)), contacts)
                case (_, None)                              =>
                  (Some(MissingEmailError), contacts)
                case (Some(oldContact), Some(updatedEmail)) =>
                  val emailAlreadyExists = contacts.exists(contact => updatedEmail == contact.email)
                  val emailChanged       = oldContact.email != updatedEmail
                  if (emailChanged && emailAlreadyExists)
                    (Some(EmailAlreadyExistsError(updatedEmail)), contacts)
                  else {
                    val updatedContact = Contact(
                      id = update.contactId,
                      firstname = update.firstname,
                      lastname = update.lastname,
                      phone = update.phone,
                      email = updatedEmail,
                    )

                    val updateIndex = contacts.indexWhere(_.id == updatedContact.id)
                    (None, contacts.updated(updateIndex, updatedContact))
                  }
              }
            }
            .flatMap(ZIO.fromOption(_).flip)
            .unit

        override def search(query: Option[String], page: Int): UIO[List[Contact]] = {
          val pageStart = (page - 1) * Settings.pageSize
          val pageEnd   = pageStart + Settings.pageSize
          contactsRef.get.map { contacts =>
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
              .toList
          }
        }
      }
    })

  private val preloadedContacts = Chunk(
    Contact("2", Some("Carson"), Some("Gross"), Some("123-456-7890"), "carson@example.comz"),
    Contact("3", None, None, None, "joe@example2.com"),
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
}
