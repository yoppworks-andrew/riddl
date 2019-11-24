package com.yoppworks.ossum.riddl.language

import fastparse._
import NoWhitespace._
import com.yoppworks.ossum.riddl.language.AST.LiteralString
import com.yoppworks.ossum.riddl.language.Terminals.Punctuation

/** Parser rules that should not collect white space */
trait NoWhiteSpaceParsers extends ParsingContext {

  def markdownLine[_: P]: P[LiteralString] = {
    P(
      location ~ Punctuation.verticalBar ~~
        CharsWhile(ch => ch != '\n' && ch != '\r').!.log ~~
        ("\n" | "\r").rep(1)
    ).map(tpl => (LiteralString.apply _).tupled(tpl))
  }

  def literalString[_: P]: P[LiteralString] = {
    P(
      location ~ Punctuation.quote ~~
        CharsWhile(_ != '"', 0).!.log ~~
        Punctuation.quote
    ).map { tpl =>
      (LiteralString.apply _).tupled(tpl)
    }
  }

}
