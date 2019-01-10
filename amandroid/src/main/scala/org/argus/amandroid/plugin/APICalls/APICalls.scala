/*
 * Copyright (c) 2019. Fengguo Wei and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0
 * which accompanies this distribution, and is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Detailed contributors are listed in the CONTRIBUTOR.md
 */

package org.argus.amandroid.plugin.APICalls

import org.argus.jawa.core.{Global, JawaMethod}
import org.argus.jawa.core.ast.CallStatement
import org.argus.jawa.flow.dfa.InterProceduralDataFlowGraph

class APICalls {
  def getProgrammarDefinedMethods(global: Global, idfgOpt: Option[InterProceduralDataFlowGraph]): Unit = {

    println("ALL application classes")
    println(global.getApplicationClasses)

    println("ALL User library classes")
    println(global.getUserLibraryClasses)

    global.getApplicationClassCodes foreach { case (typ, f) =>
      println("Following are programmer defined functions in  Class "+ typ.toString())
      global.getClazz(typ) match {

      case Some(c)=>
        c.getDeclaredMethods.foreach { x: JawaMethod =>
          x.getBody.resolvedBody.locations.foreach { line =>
            line.statement match {
              case cs: CallStatement => println(cs)
              case _ =>
            }

          }
        }/*
        case Some(c)=>
          c.getDeclaredMethods.foreach { x: JawaMethod => println(x.toString())  }*/

          case None =>
      }
    }





  }
}