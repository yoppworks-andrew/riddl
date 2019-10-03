package com.yoppworks.ossum.riddl.parser

import cats.Monoid
import com.yoppworks.ossum.riddl.parser.AST._

/** Traversal Module */
object Traversal {

  trait DefTraveler[P, D <: Def] {
    def traverse: P
    protected def open(): P
    protected def close(): P
    protected def visitExplanation(exp: Option[Explanation]): Unit
  }

  trait DomainTraveler[P] extends DefTraveler[P, DomainDef] {
    def domain: DomainDef
    final def traverse: P = {
      open()
      domain.types.foreach(visitType(_).traverse)
      domain.channels.foreach(visitChannel(_).traverse)
      domain.interactions.foreach(visitInteraction(_).traverse)
      domain.contexts.foreach(visitContext(_).traverse)
      visitExplanation(domain.explanation)
      close()
    }

    def visitType(typ: TypeDef): TypeTraveler[P]
    def visitChannel(chan: ChannelDef): ChannelTraveler[P]
    def visitInteraction(i: InteractionDef): InteractionTraveler[P]
    def visitContext(context: ContextDef): ContextTraveler[P]
  }

  trait TypeTraveler[P] extends DefTraveler[P, TypeDef] {
    def typ: TypeDef

    def traverse: P = {
      open()
      visitType(typ.typ)
      close()
    }
    def visitType(ty: Type): Unit
  }

  trait ChannelTraveler[P] extends DefTraveler[P, ChannelDef] {
    def channel: ChannelDef

    def traverse: P = {
      open()
      channel.commands.foreach(visitCommand)
      channel.events.foreach(visitEvent)
      channel.queries.foreach(visitQuery)
      channel.results.foreach(visitResult)
      close()
    }
    def visitCommand(command: CommandRef): Unit
    def visitEvent(event: EventRef): Unit
    def visitQuery(query: QueryRef): Unit
    def visitResult(result: ResultRef): Unit
  }

  trait ContextTraveler[P] extends DefTraveler[P, ContextDef] {
    def context: ContextDef
    final def traverse: P = {
      open()
      context.types.foreach(visitType(_).traverse)
      context.commands.foreach(visitCommand)
      context.events.foreach(visitEvent)
      context.queries.foreach(visitQuery)
      context.results.foreach(visitResult)
      context.adaptors.foreach(visitAdaptor(_).traverse)
      context.interactions.foreach(visitInteraction(_).traverse)
      context.entities.foreach(visitEntity(_).traverse)
      close()
    }
    def visitType(ty: TypeDef): TypeTraveler[P]
    def visitCommand(command: CommandDef): Unit
    def visitEvent(event: EventDef): Unit
    def visitQuery(query: QueryDef): Unit
    def visitResult(result: ResultDef): Unit
    def visitAdaptor(a: AdaptorDef): AdaptorTraveler[P]
    def visitInteraction(i: InteractionDef): InteractionTraveler[P]
    def visitEntity(e: EntityDef): EntityTraveler[P]
  }

  trait AdaptorTraveler[P] extends DefTraveler[P, AdaptorDef] {
    def adaptor: AdaptorDef
    final def traverse: P = {
      open()
      close()
    }
  }

  trait EntityTraveler[P] extends DefTraveler[P, EntityDef] {
    def entity: EntityDef
    final def traverse: P = {
      open()
      entity.consumes.foreach(visitConsumer)
      entity.produces.foreach(visitProducer)
      entity.invariants.foreach(visitInvariant)
      entity.features.foreach(visitFeature)
      visitExplanation(entity.explanation)
      close()
    }
    def visitProducer(p: ChannelRef): Unit
    def visitConsumer(c: ChannelRef): Unit
    def visitInvariant(i: InvariantDef): Unit
    def visitFeature(f: FeatureDef): FeatureTraveler[P]
  }

  trait FeatureTraveler[P] extends DefTraveler[P, FeatureDef] {
    def feature: FeatureDef
    final def traverse: P = {
      open()
      feature.examples.foreach(visitExample)
      close()
    }
    def visitExample(example: Example): Unit
  }

  trait InteractionTraveler[P] extends DefTraveler[P, InteractionDef] {
    def interaction: InteractionDef
    final def traverse: P = {
      open()
      interaction.roles.foreach(visitRole)
      interaction.actions.foreach(visitAction)
      close()
    }
    def visitRole(role: RoleDef): Unit
    def visitAction(action: ActionDef): Unit
  }

  def traverse[T <: Monoid[_]](
    domains: Domains
  )(f: DomainDef ⇒ DomainTraveler[T]): Seq[T] = {
    domains.map { domain ⇒
      f(domain).traverse
    }
  }

  def traverse[T <: Monoid[_]](
    context: ContextDef
  )(f: ContextDef => ContextTraveler[T]): T = {
    f(context).traverse
  }

  def traverse[D <: Def, T <: Monoid[_]](
    ast: AST
  )(f: AST ⇒ DefTraveler[T, D]): T = {
    f(ast).traverse
  }
}
