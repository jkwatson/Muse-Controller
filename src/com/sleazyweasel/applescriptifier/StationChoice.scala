package com.sleazyweasel.applescriptifier

import java.lang.Integer

case class StationChoice(key: Integer, stationName: String) {
  override def toString: String = {
    stationName
  }
}