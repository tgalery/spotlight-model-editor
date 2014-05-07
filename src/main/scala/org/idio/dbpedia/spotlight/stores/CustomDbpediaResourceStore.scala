package org.idio.dbpedia.spotlight.stores

/**
 * Created by dav009 on 23/12/2013.
 */

import org.dbpedia.spotlight.db.memory.{ MemoryStore, MemoryResourceStore }
import java.io.{ File, FileInputStream }
import org.dbpedia.spotlight.exceptions.DBpediaResourceNotFoundException

class CustomDbpediaResourceStore(val pathtoFolder: String) {

  var resStore: MemoryResourceStore = MemoryStore.loadResourceStore(new FileInputStream(new File(pathtoFolder, "res.mem")))

  /*
  * Creates the specified DbpediaResource in the internal Arrays
  * */
  private def addDbpediaURI(uri: String, support: Int, types: Array[String]) {
    //URI i.e: Click-through_rate
    //Types: ??
    this.resStore.supportForID = Array concat (resStore.supportForID, Array(support))
    this.resStore.uriForID = Array concat (resStore.uriForID, Array(uri))

    var dbpediaTypesForResource: Array[Array[java.lang.Short]] = this.getTypesIds(types)

    this.resStore.typesForID = Array concat (resStore.typesForID, dbpediaTypesForResource)
  }

  /*
  * Checks if a given dpbediaID(URI) exists in the resource store
  * if it doesnt it creates it and returns its id
  * if it exists it returns its id
  * */
  def getAddDbpediaResource(uri: String, support: Int, types: Array[String]): Int = {
    try {
      val resourceID = this.resStore.getResourceByName(uri).id
      println("\tfound dbpedia resource for:" + uri + "--" + resourceID)
      return resourceID
    } catch {

      case e: DBpediaResourceNotFoundException => println("creating dbpedia Resource...")
    }
    val resourceID = this.addDbpediaResource(uri, support, types)
    println("\tcreated dbpedia Resource for: " + uri + "--" + resourceID)
    return resourceID
  }

  /*
  * Adds a new Dbpedia Resource to teh store, and rebuilds the indexes.
  * It does NOT check previous existance
  * */
  def addDbpediaResource(uri: String, support: Int, types: Array[String]): Int = {

    // add the dbpedia URI to the arrays
    this.addDbpediaURI(uri, support, types)

    //update internal indexes
    this.resStore.createReverseLookup()

    return this.resStore.getResourceByName(uri).id
  }

  def printAllSupportValues() {
    this.resStore.idFromURI.keySet().toArray.foreach { uri =>
      val id = this.resStore.idFromURI.get(uri)
      val support = this.resStore.supportForID(id)
      println(uri + "\t" + support)
    }
  }

  /*
  * Given a list of string of types it return a list wth types ids:
  * i.e: [dbpdia:person, dbpedia:location] => [100, 392]..
  * */
  def getTypesIds(dbpediaTypes: Array[String]): Array[Array[java.lang.Short]] = {
    var dbpediaTypesForResource2 = new Array[Array[java.lang.Short]](1)
    var dbpediaTypesForResource: Array[java.lang.Short] = new Array[java.lang.Short](dbpediaTypes.length)

    for (i <- 0 to dbpediaTypes.length - 1) {
      var currentType: String = dbpediaTypes(i)
      if (!currentType.equals("")) {
        dbpediaTypesForResource(i) = resStore.ontologyTypeStore.getOntologyTypeByName(currentType).id
      }
    }

    dbpediaTypesForResource2(0) = dbpediaTypesForResource
    return dbpediaTypesForResource2
  }

}
