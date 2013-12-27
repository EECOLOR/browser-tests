package org.qirx.browserTests.runner

import sbt.testing
import org.qirx.browserTests.Arguments
import com.gargoylesoftware.htmlunit.BrowserVersion

case class Task(
  browserVersions: Seq[BrowserVersion],
  testPage: String,
  runCode: RunCodeFunction)(val taskDef: testing.TaskDef) extends testing.Task {

  val tags = Array.empty[String]
  private val testName = taskDef.fullyQualifiedName
  private val events = new Events(taskDef)

  def execute(eventHandler: testing.EventHandler, loggers: Array[testing.Logger]): Array[testing.Task] = {

    val eventProxy = new EventProxy(eventHandler, loggers, events)

    for (browserVersion <- browserVersions)
      runCode(eventProxy, browserVersion) { (host, port, browser) =>
        eventProxy.log.info(s"Running test '$testName' in '$browserVersion' with $testPage")
        val url = s"http://$host:$port/$testPage?test=$testName"
        browser.get(url)
      }

    Array.empty[testing.Task]
  }
}