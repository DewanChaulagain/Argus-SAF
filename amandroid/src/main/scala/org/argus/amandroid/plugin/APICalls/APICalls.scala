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

import java.io.{File, PrintWriter}

import org.argus.amandroid.alir.dataRecorder.DataCollector
import org.argus.amandroid.core.ApkGlobal
import org.argus.jawa.core.{Global, JawaMethod}
import org.argus.jawa.core.ast.CallStatement
import org.argus.jawa.core.elements.{JawaType, Signature}
import org.argus.jawa.core.java_signatures.ClassType
import org.argus.jawa.core.util._
import org.argus.jawa.flow.dfa.InterProceduralDataFlowGraph

import scala.collection.mutable.ListBuffer

class APICalls {
  def getProgrammarDefinedMethods(global: Global, appName: String,args: String, idfgOpt: Option[InterProceduralDataFlowGraph]): Unit = {

    //println("ALL application classes")    //println(global.getApplicationClasses)
    //println("ALL User library classes")    //println(global.getUserLibraryClasses)


    val mapForAllClass: MMap[String, MMap[Signature, ListBuffer[Signature]]] = mmapEmpty
    global.getApplicationClassCodes foreach { case (typ, f) =>
      global.getClazz(typ) match {
      case Some(c)=>

        val mapForEachClass: MMap[Signature, ListBuffer[Signature]] = mmapEmpty
        c.getDeclaredMethods.foreach { x: JawaMethod =>
          var callListInEachMethod = new ListBuffer[Signature]()
          x.getBody.resolvedBody.locations.foreach { line =>
            line.statement match {
              case cs: CallStatement =>
                /*println(cs.signature + "****"+cs.signature.getClassName+ "-isApplicationclass" +global.isApplicationClasses(cs.signature.classTyp)+" isSystem: "+global.isSystemLibraryClasses(cs.signature.classTyp)+ " isUser: "+global.isUserLibraryClasses(cs.signature.classTyp));*/
                  if(!global.isApplicationClasses(cs.signature.classTyp))
                    callListInEachMethod+= cs.signature

              case _ =>
            }
          }

          mapForEachClass.put(x.getSignature,callListInEachMethod)
        }
        //mapForEachClass is correct , completed verification
        //for( (ke,va) <- mapForEachClass){ if (ke.toString.contains("onStartCommand") )  println(va)}
        mapForAllClass.put(c.toString(),mapForEachClass)

      case None =>
      }

    }
    //println("Projectname: "+ global.projectName)
    writeMapToFile(global.projectName,appName,args, mapForAllClass)
    //mapForAllClass is correct , completed verification
    for( (ke,va) <- mapForAllClass){
      if (ke.toString.equals("com.example.shiva.smsstealer.Second") ){
        for( (k,v) <- va){
          if (k.toString.contains("onStartCommand"))
            println(v)
        }
      }
    }
  }


  private def writeMapToFile( projectName : String,appName: String,dest : String, mapForAllClass: MMap[String, MMap[Signature, ListBuffer[Signature]]]): Unit = {
      var appNameFinal =""
      if(appName.contains("apk")){
        appNameFinal = appName.substring(0,appName.length-4)
      }
      var destFinal = dest + appNameFinal + ".txt"


      val out = new PrintWriter(new File(destFinal))
      for( (classNameAsKey,mapForAClassAsvalue) <- mapForAllClass){
        if (classNameAsKey !=null  ){
          //out.print("{)
          for( (methodName,apiCallsAsList) <- mapForAClassAsvalue){
              if (methodName != null && apiCallsAsList.length >0)
              {
                for (item: Signature <- apiCallsAsList) {
		  if(item!=apiCallsAsList.last){
                    out.write(item.toString())
                    out.write(" ")
		   }
                   else
		    out.write(item.toString())
                }
                out.write("#")
 		out.write(" ")
              }

          }
          out.write("\n")
        }
      }
      out.close()
    //}
  }


}
