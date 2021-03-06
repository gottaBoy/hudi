/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hudi.execution.bulkinsert;

import org.apache.hudi.client.WriteStatus;
import org.apache.hudi.common.model.HoodieRecord;
import org.apache.hudi.common.model.HoodieRecordPayload;
import org.apache.hudi.config.HoodieWriteConfig;
import org.apache.hudi.execution.LazyInsertIterable;
import org.apache.hudi.table.HoodieTable;

import org.apache.spark.api.java.function.Function2;

import java.util.Iterator;
import java.util.List;

/**
 * Map function that handles a stream of HoodieRecords.
 */
public class BulkInsertMapFunction<T extends HoodieRecordPayload>
    implements Function2<Integer, Iterator<HoodieRecord<T>>, Iterator<List<WriteStatus>>> {

  private String instantTime;
  private boolean areRecordsSorted;
  private HoodieWriteConfig config;
  private HoodieTable<T> hoodieTable;
  private List<String> fileIDPrefixes;

  public BulkInsertMapFunction(String instantTime, boolean areRecordsSorted,
                               HoodieWriteConfig config, HoodieTable<T> hoodieTable,
                               List<String> fileIDPrefixes) {
    this.instantTime = instantTime;
    this.areRecordsSorted = areRecordsSorted;
    this.config = config;
    this.hoodieTable = hoodieTable;
    this.fileIDPrefixes = fileIDPrefixes;
  }

  @Override
  public Iterator<List<WriteStatus>> call(Integer partition, Iterator<HoodieRecord<T>> recordItr) {
    return new LazyInsertIterable<>(recordItr, areRecordsSorted, config, instantTime, hoodieTable,
        fileIDPrefixes.get(partition), hoodieTable.getSparkTaskContextSupplier());
  }
}
