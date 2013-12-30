package org.qirx.browserTests.runner

import sbt.testing
import org.qirx.browserTests.Arguments
import com.gargoylesoftware.htmlunit.BrowserVersion

case class Task(testRunner:TestRunner)(val taskDef: testing.TaskDef) extends testing.Task {

  val tags = Array.empty[String]
  private val testName = taskDef.fullyQualifiedName
  private val events = new Events(taskDef)

  def execute(eventHandler: testing.EventHandler, loggers: Array[testing.Logger]): Array[testing.Task] = {

    val eventProxy = new EventProxy(eventHandler, loggers, events)

    testRunner.run(eventProxy, testName)

    Array.empty[testing.Task]
  }
}