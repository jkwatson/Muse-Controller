package com.sleazyweasel.applescriptifier

import com.google.gson.Gson
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util._
import java.util.logging.Level
import java.util.logging.Logger
import java.util

object PandoraBoyServlet {
  private final val logger: Logger = Logger.getLogger(classOf[PandoraBoyServlet].getName)
  private final val QUICK_MIX_STATION_CODE: String = "QuickMix"
}

class PandoraBoyServlet extends HttpServlet {
  protected override def doGet(req: HttpServletRequest, response: HttpServletResponse) {
    response.setHeader("Expires", "Sat, 6 May 1995 12:00:00 GMT")
    response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate")
    response.addHeader("Cache-Control", "post-check=0, pre-check=0")
    response.setHeader("Pragma", "no-cache")
    response.setContentType("application/json; charset=utf-8")
    response.setCharacterEncoding("UTF-8")
    val pathInfo: String = req.getPathInfo
    if (pathInfo.startsWith("/status")) {
      appendStatus(response)
    }
    else if (pathInfo.startsWith("/setStation")) {
      val station: String = req.getParameter("station")
      setStation(station)
      appendStatus(response)
    }
    else if (pathInfo.startsWith("/playpause")) {
      pandoraBoySupport.playPause()
      appendStatus(response)
    }
    else if (pathInfo.startsWith("/next")) {
      pandoraBoySupport.next()
      appendStatus(response)
    }
    else if (pathInfo.startsWith("/thumbsUp")) {
      pandoraBoySupport.thumbsUp()
      appendStatus(response)
    }
    else if (pathInfo.startsWith("/thumbsDown")) {
      pandoraBoySupport.thumbsDown()
      appendStatus(response)
    }
    else if (pathInfo.startsWith("/create")) {
      val stationName: String = req.getParameter("station")
      appleScriptTemplate.execute(Application.PANDORABOY, "create station \"" + stationName + "\"")
    }
    else if (pathInfo.startsWith("/reset")) {
      appleScriptTemplate.executeKeyStrokeWithCommandKey(Application.PANDORABOY, "r")
    }
    else {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND)
    }
  }

  private def setStation(station: String) {
    if (PandoraBoyServlet.QUICK_MIX_STATION_CODE == station) {
      appleScriptTemplate.execute(Application.PANDORABOY, "set current station to (quickmix station)")
    }
    else {
      appleScriptTemplate.execute(Application.PANDORABOY, "set current station to item 1 of (every station whose name is \"" + station + "\")")
    }
  }

  private def appendStatus(response: HttpServletResponse) {
    val status: util.Map[String, AnyRef] = new util.HashMap[String, AnyRef]
    var currentStation: AnyRef = null
    try {
      currentStation = appleScriptTemplate.execute(Application.PANDORABOY, "get name of current station")
      if ((currentStation.asInstanceOf[String]).contains(PandoraBoyServlet.QUICK_MIX_STATION_CODE)) {
        currentStation = PandoraBoyServlet.QUICK_MIX_STATION_CODE
      }
    }
    catch {
      case e: Exception => {
        PandoraBoyServlet.logger.log(Level.WARNING, "Exception caught.", e)

        currentStation = "Unable to get Station from PandoraBoy"
      }
    }
    status.put("currentStation", currentStation)
    var currentTrack: util.List[String] = null
    try {
      currentTrack = appleScriptTemplate.execute(Application.PANDORABOY, "get [name of current track, artist of current track]")
    }
    catch {
      case e: Exception => {
        PandoraBoyServlet.logger.log(Level.WARNING, "Exception caught.", e)

        currentTrack = util.Arrays.asList("", "")
      }
    }
    status.put("currentTrack", currentTrack)
    val stations: LinkedHashSet[String] = new LinkedHashSet[String](appleScriptTemplate.execute[util.List[String]](Application.PANDORABOY, "get name of every station"))
    stations.add(PandoraBoyServlet.QUICK_MIX_STATION_CODE)
    status.put("stations", stations)
    val uglyStuff: String = appleScriptTemplate.execute(Application.PANDORABOY, "get player state")
    status.put("status", if (uglyStuff.contains("play")) "playing" else "stopped")
    status.put("version", ControlServlet.CURRENT_VERSION)
    val json: String = new Gson().toJson(status)
    response.getWriter.append(json)
    response.setStatus(HttpServletResponse.SC_OK)
  }

  private final val appleScriptTemplate: AppleScriptTemplate = new AppleScriptTemplateFactory().getActiveTemplate
  private final val pandoraBoySupport: PandoraBoySupport = new PandoraBoySupport(appleScriptTemplate)
}