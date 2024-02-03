package zio.http

import play.twirl.api.HtmlFormat

object extensions {

  private val contentTypeHtml: Headers = Headers(Header.ContentType(MediaType.text.html).untyped)

  implicit final class RichResponseType(private val dummy: Response.type) extends AnyVal {
    def twirl(html: HtmlFormat.Appendable, status: Status = Status.Ok): Response =
      Response(
        body = Body.fromCharSequence(html.toString),
        headers = Headers(contentTypeHtml),
        status = status,
      )
  }
}
