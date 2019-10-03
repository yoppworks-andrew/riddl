package com.yoppworks.ossum.riddl.parser

import com.yoppworks.ossum.riddl.parser.AST._

import scala.collection.immutable.ListMap

/** Unit Tests For TypesParserTest */
class TypesParserTest extends ParsingTest {

  "TypesParser" should {
    "allow renames of 8 literal types" in {
      val cases = Map[String, TypeDef](
        "type str = String" → TypeDef(
          33,
          Identifier("str"),
          PredefinedType(Identifier("String"))
        ),
        "type num = Number" → TypeDef(
          33,
          Identifier("num"),
          PredefinedType(Identifier("Number"))
        ),
        "type boo = Boolean" → TypeDef(
          33,
          Identifier("boo"),
          PredefinedType(Identifier("Boolean"))
        ),
        "type ident  = Id" -> TypeDef(
          33,
          Identifier("ident"),
          PredefinedType(Identifier("Id"))
        ),
        "type dat = Date" -> TypeDef(
          33,
          Identifier("dat"),
          PredefinedType(Identifier("Date"))
        ),
        "type tim = Time" -> TypeDef(
          33,
          Identifier("tim"),
          PredefinedType(Identifier("Time"))
        ),
        "type stamp = TimeStamp" -> TypeDef(
          33,
          Identifier("stamp"),
          PredefinedType(Identifier("TimeStamp"))
        ),
        "type url = URL" -> TypeDef(
          33,
          Identifier("url"),
          PredefinedType(Identifier("URL"))
        ),
        "type FirstName = String" -> TypeDef(
          33,
          Identifier("FirstName"),
          PredefinedType(Identifier("String"))
        )
      )
      checkDef[TypeDef](cases, _.contexts.head.types.head)
    }
    "allow enumerations" in {
      val cases: List[(String, TypeDef)] = List(
        "type enum = any [ Apple Pear Peach Persimmon ]" ->
          TypeDef(
            33,
            Identifier("enum"),
            Enumeration(
              List(
                Identifier("Apple"),
                Identifier("Pear"),
                Identifier("Peach"),
                Identifier("Persimmon")
              )
            )
          ),
        "type alt = choose enum or stamp or url " ->
          TypeDef(
            33,
            Identifier("alt"),
            Alternation(
              List(
                TypeRef(Identifier("enum")),
                TypeRef(Identifier("stamp")),
                TypeRef(Identifier("url"))
              )
            )
          ),
        """type agg = combine {
          |  key: Number,
          |  id: Id,
          |  time: TimeStamp
          |}
          |""".stripMargin ->
          TypeDef(
            33,
            Identifier("agg"),
            Aggregation(
              ListMap(
                Identifier("key") → Number,
                Identifier("id") → Id,
                Identifier("time") → TimeStamp
              )
            )
          ),
        "type oneOrMore = type agg+" ->
          TypeDef(
            33,
            Identifier("oneOrMore"),
            OneOrMore(Identifier("agg"))
          ),
        "type zeroOrMore = type agg*" ->
          TypeDef(
            33,
            Identifier("zeroOrMore"),
            ZeroOrMore(Identifier("agg"))
          ),
        "type optional = type agg?" ->
          TypeDef(
            33,
            Identifier("optional"),
            Optional(Identifier("agg"))
          )
      )
    }
  }
}
