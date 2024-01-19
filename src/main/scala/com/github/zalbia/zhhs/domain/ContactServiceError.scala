package com.github.zalbia.zhhs.domain

// Domain errors for the ContactService
object ContactServiceError {
  sealed trait SaveContactError   extends Product with Serializable
  sealed trait UpdateContactError extends Product with Serializable
  sealed trait DeleteContactError extends Product with Serializable

  final case class EmailAlreadyExistsError(email: String)   extends SaveContactError with UpdateContactError
  case object MissingEmailError                             extends SaveContactError with UpdateContactError
  final case class ContactIdDoesNotExist(contactId: String) extends UpdateContactError with DeleteContactError
}
