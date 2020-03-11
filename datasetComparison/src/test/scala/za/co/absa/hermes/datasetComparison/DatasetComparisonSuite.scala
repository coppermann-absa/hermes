/*
 * Copyright 2019 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.hermes.datasetComparison

import org.apache.spark.sql.Column
import org.scalatest.FunSuite
import za.co.absa.hermes.datasetComparison.cliUtils.{CliOptions, DataframeOptions}
import za.co.absa.hermes.datasetComparison.config.ManualConfig
import za.co.absa.hermes.utils.SparkTestBase

class DatasetComparisonSuite extends FunSuite with SparkTestBase {
  test("Test a positive comparison") {
    val cliOptions = new CliOptions(
      DataframeOptions("csv", Map("delimiter" -> ","), getClass.getResource("/dataSample2.csv").toString),
      DataframeOptions("csv", Map("delimiter" -> ","), getClass.getResource("/dataSample1.csv").toString),
      "path/to/nowhere",
      None,
      "--bogus raw-options"
    )
    val manualConfig = new ManualConfig(
      "errCol",
      "tmp",
      "comparisonUniqueId",
      "actual",
      "expected",
      true
    )
    val expectedResult = ComparisonResult(
      10, 10, 0, 0, 10,
      List(
        new Column("_c0"),
        new Column("_c1"),
        new Column("_c2"),
        new Column("_c3"),
        new Column("_c4"),
        new Column("_c5")
      ),
      None, 0,
      "--bogus raw-options"
    )

    val cmpResult = new DatasetComparison(cliOptions, manualConfig).compare

    assert(expectedResult == cmpResult)
  }

  test("Compare datasets with duplicates disallowed") {
    val cliOptions = new CliOptions(
      DataframeOptions("csv", Map("delimiter" -> ",", "header" -> "true"), getClass.getResource("/dataSample1.csv").toString),
      DataframeOptions("csv", Map("delimiter" -> ",", "header" -> "true"), getClass.getResource("/dataSample6.csv").toString),
      "path/to/nowhere",
      Some(Set("id", "first_name")),
      "--bogus raw-options"
    )
    val manualConfig = new ManualConfig(
      "errCol",
      "tmp",
      "comparisonUniqueId",
      "actual",
      "expected",
      true
    )

    val result = new DatasetComparison(cliOptions, manualConfig).compare
    assert(9 == result.refRowCount)
    assert(10 == result.newRowCount)
    assert(0 == result.refDuplicateCount)
    assert(1 == result.newDuplicateCount)
    assert(7 == result.passedCount)
    assert(2 == result.diffCount)
    assert("--bogus raw-options" ==  result.passedOptions)
  }
}