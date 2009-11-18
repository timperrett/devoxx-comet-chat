package eu.getintheloop.example.comet

import _root_.scala.xml.{Elem,NodeSeq,Text}
import _root_.net.liftweb.http.{ListenerManager,CometActor,CometListenee,SHtml}
import _root_.net.liftweb.http.js.JsCmds._
import _root_.net.liftweb.common._
import _root_.net.liftweb.actor._

// message container that we use to notify listeners
case class Messages(msgs : List[String])

// global chat server
object ChatServer extends LiftActor with ListenerManager { 
  // Global list of messages 
  private var msgs: List[String] = Nil 
  // The message that we'll send to all subscribers 
  protected def createUpdate = Messages(msgs) 
  // Process messages that we receive 
  override def highPriority = { 
    case s: String if s.length > 0 => 
      msgs ::= s 
      updateListeners() 
  } 
}

// the per-session chat 'client'
class Chat extends CometActor with CometListenee { 
  private var msgs: List[String] = Nil 
  def render = 
  <div> 
    <ul>{msgs.reverse.map(m => <li>{m}</li>)}</ul> 
    {SHtml.ajaxText("", s => {ChatServer ! s; Noop})} 
  </div> 
  protected def registerWith = ChatServer 
  override def highPriority = { 
    case Messages(m) => msgs = m ; reRender(false) 
  } 
}