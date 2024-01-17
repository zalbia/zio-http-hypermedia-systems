package com.github.zalbia.zhhs.domain

// Domain errors for the ContactService
sealed trait ContactServiceError extends Product with Serializable
object ContactServiceError {
  sealed trait SaveContactError   extends ContactServiceError
  sealed trait UpdateContactError extends ContactServiceError

  final case class EmailAlreadyExistsError(email: String)   extends SaveContactError with UpdateContactError
  case object MissingEmailError                             extends SaveContactError with UpdateContactError
  final case class ContactIdDoesNotExist(contactId: String) extends UpdateContactError
}
