package com.yoppworks.ossum.riddl.parser

sealed trait AST

/** Abstract Syntax Tree
  * This object defines the model for processing IDDL and producing a
  * raw AST from it. This raw AST has no referential integrity, it just
  * results from applying the parsing rules to the input. The RawAST models
  * produced from parsing are syntactically correct but have no semantic
  * validation. The Transformation passes convert RawAST model to AST model
  * which is referentially and semantically consistent (or the user gets an
  * error).
  */
object AST {

  sealed trait Type extends AST

  sealed trait Def extends AST {
    def index: Int
    def name: String
  }

  sealed trait Ref extends AST {
    def name: String
  }

  case object String extends Type
  case object Boolean extends Type
  case object Number extends Type
  case object Id extends Type
  case object Date extends Type
  case object Time extends Type
  case object TimeStamp extends Type
  case object URL extends Type

  case class Enumeration(of: Seq[String]) extends Type
  case class Alternation(of: Seq[Type]) extends Type
  case class Aggregation(of: Map[String, Type]) extends Type

  case class Optional(element: Type) extends Type
  case class ZeroOrMore(of: Type) extends Type
  case class OneOrMore(of: Type) extends Type

  case class TypeRef(name: String) extends Ref with Type
  case class TypeDef(index: Int, name: String, typ: Type) extends Def

  case class ChannelRef(name: String) extends Ref
  case class ChannelDef(
    index: Int,
    name: String,
    commands: Seq[String] = Seq.empty[String],
    queries: Seq[String] = Seq.empty[String],
    events: Seq[String] = Seq.empty[String],
    results: Seq[String] = Seq.empty[String]
  ) extends Def

  sealed trait MessageRef extends Ref
  sealed trait MessageDef extends Def

  case class CommandRef(name: String) extends MessageRef
  case class CommandDef(
    index: Int,
    name: String,
    typ: Type,
    events: EventRefs
  ) extends MessageDef

  case class EventRef(name: String) extends MessageRef
  type EventRefs = Seq[EventRef]
  case class EventDef(index: Int, name: String, typ: Type) extends MessageDef

  case class QueryRef(name: String) extends MessageRef
  case class QueryDef(
    index: Int,
    name: String,
    typ: Type,
    result: ResultRef
  ) extends MessageDef

  case class ResultRef(name: String) extends MessageRef
  case class ResultDef(index: Int, name: String, typ: Type) extends MessageDef

  sealed trait EntityOption
  case object EntityAggregate extends EntityOption
  case object EntityPersistent extends EntityOption
  case object EntityConsistent extends EntityOption
  case object EntityAvailable extends EntityOption
  case object EntityDevice extends EntityOption

  case class EntityRef(name: String) extends Ref

  case class Given(fact: String)
  case class When(situation: String)
  case class Then(result: String)
  case class Background(givens: Seq[Given])
  case class Example(
    description: String,
    givens: Seq[Given],
    whens: Seq[When],
    thens: Seq[Then]
  )
  case class Feature(
    name: String,
    description: String,
    background: Option[Background],
    examples: Seq[Example]
  )

  case class Invariant(name: String, expression: String)

  /** Definition of an Entity
    *
    * @param options The options for the entity
    * @param index The index location in the input
    * @param name The name of the entity
    * @param typ The type of the entity's value
    * @param consumes A reference to the channel from which the entity consumes
    * @param produces A reference to the channel to which the entity produces
    */
  case class EntityDef(
    index: Int,
    options: Seq[EntityOption] = Seq.empty[EntityOption],
    name: String,
    typ: Type,
    consumes: Option[ChannelRef] = None,
    produces: Option[ChannelRef] = None,
    features: Seq[Feature] = Seq.empty[Feature],
    invariants: Seq[Invariant] = Seq.empty[Invariant]
  ) extends Def

  trait TranslationRule {
    def name: String
    def channel: String
  }

  case class MessageTranslationRule(
    name: String,
    channel: String,
    input: String,
    output: String,
    rule: String
  )

  /** Definition of an Adaptor
    * Adaptors are defined in Contexts to convert messaging from one Context to
    * another. Adaptors translate incoming events from other Contexts into
    * commands or events that its owning context can understand. There should be
    * one Adaptor for each external Context
    *
    * @param index Location in the parsing input
    * @param name Name of the adaptor
    */
  case class AdaptorDef(
    index: Int,
    name: String,
    targetDomain: Option[DomainRef] = None,
    targetContext: ContextRef
    // Details TBD
  ) extends Def

  sealed trait ContextOption
  case object WrapperOption extends ContextOption
  case object FunctionOption extends ContextOption
  case object GatewayOption extends ContextOption

  case class ContextRef(name: String) extends Ref

  case class ContextDef(
    options: Seq[ContextOption] = Seq.empty[ContextOption],
    index: Int,
    name: String,
    types: Seq[TypeDef] = Seq.empty[TypeDef],
    commands: Seq[CommandDef] = Seq.empty[CommandDef],
    events: Seq[EventDef] = Seq.empty[EventDef],
    queries: Seq[QueryDef] = Seq.empty[QueryDef],
    results: Seq[ResultDef] = Seq.empty[ResultDef],
    entities: Seq[EntityDef] = Seq.empty[EntityDef],
    translators: Seq[AdaptorDef] = Seq.empty[AdaptorDef],
    interactions: Seq[InteractionDef] = Seq.empty[InteractionDef]
  ) extends Def

  /** Definition of an Interaction
    * Interactions define an exemplary interaction between the system being
    * designed and other actors. The basic ideas of an Interaction are much
    * like UML Sequence Diagram.
    *
    * @param index Where in the input the Scenario is defined
    * @param prefix The path identifier's prefix
    * @param name The name of the scenario
    */
  case class InteractionDef(
    index: Int,
    prefix: Seq[String],
    name: String,
    roles: Seq[RoleDef],
    actions: Seq[ActionDef]
  ) extends Def

  sealed trait RoleOption
  case object HumanOption extends RoleOption
  case object DeviceOption extends RoleOption

  case class RoleDef(
    options: Seq[RoleOption] = Seq.empty[RoleOption],
    index: Int,
    name: String,
    responsibilities: Seq[String] = Seq.empty[String],
    capacities: Seq[String] = Seq.empty[String]
  ) extends Def

  case class RoleRef(name: String) extends Ref

  sealed trait ActionDef extends Def

  type Actions = Seq[ActionDef]

  sealed trait MessageOption
  case object SynchOption extends MessageOption
  case object AsynchOption extends MessageOption
  case object ReplyOption extends MessageOption

  /** An Interaction based on entity messaging
    * @param options Options for the message
    * @param index Where the message is located in the input
    * @param name The displayable text that describes the interaction
    * @param sender A reference to the entity sending the message
    * @param receiver A reference to the entity receiving the message
    * @param message A reference to the kind of message sent & received
    */
  case class MessageActionDef(
    options: Seq[MessageOption] = Seq.empty[MessageOption],
    index: Int,
    name: String,
    sender: EntityRef,
    receiver: EntityRef,
    message: MessageRef
  ) extends ActionDef

  case class DirectiveActionDef(
    options: Seq[MessageOption] = Seq.empty[MessageOption],
    index: Int,
    name: String,
    role: RoleRef,
    entity: EntityRef,
    message: MessageRef
  ) extends ActionDef

  case class ProcessingActionDef(
    index: Int,
    name: String,
    entity: EntityRef,
    description: String
  ) extends ActionDef

  case class DeletionActionDef(
    index: Int,
    name: String,
    entity: EntityRef
  ) extends ActionDef

  case class CreationActionDef(
    index: Int,
    name: String,
    entity: EntityRef
  ) extends ActionDef

  case class DomainRef(name: String) extends Ref

  case class DomainDef(
    index: Int,
    prefix: Seq[String],
    name: String,
    channels: Seq[ChannelDef] = Seq.empty[ChannelDef],
    contexts: Seq[ContextDef] = Seq.empty[ContextDef],
    scenarios: Seq[InteractionDef] = Seq.empty[InteractionDef]
  ) extends Def

}
