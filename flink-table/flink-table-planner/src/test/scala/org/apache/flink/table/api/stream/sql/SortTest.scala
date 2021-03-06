/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.api.stream.sql

import org.apache.flink.api.scala._
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala._
import org.apache.flink.table.utils.TableTestUtil._
import org.apache.flink.table.utils.{StreamTableTestUtil, TableTestBase}
import org.junit.Test

class SortTest extends TableTestBase {

  private val streamUtil: StreamTableTestUtil = streamTestUtil()
  private val table = streamUtil.addTable[(Int, String, Long)]("MyTable", 'a, 'b, 'c,
      'proctime.proctime, 'rowtime.rowtime)
  
  @Test
  def testSortProcessingTime(): Unit = {

    val sqlQuery = "SELECT a FROM MyTable ORDER BY proctime, c"

    val expected =
      unaryNode(
        "DataStreamCalc",
        unaryNode("DataStreamSort",
          streamTableNode(table),
          term("orderBy", "proctime ASC", "c ASC")),
        term("select", "a", "PROCTIME(proctime) AS proctime", "c"))

    streamUtil.verifySql(sqlQuery, expected)
  }

  @Test
  def testSortRowTime(): Unit = {

    val sqlQuery = "SELECT a FROM MyTable ORDER BY rowtime, c"
      
    val expected =
      unaryNode("DataStreamSort",
        unaryNode(
          "DataStreamCalc",
          streamTableNode(table),
          term("select", "a", "rowtime", "c")),
        term("orderBy", "rowtime ASC, c ASC"))

    streamUtil.verifySql(sqlQuery, expected)
  }
}
