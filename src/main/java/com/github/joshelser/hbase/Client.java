package com.github.joshelser.hbase;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;
import org.apache.hadoop.hbase.util.Bytes;

public class Client {
  public static final TableName TABLE_NAME = TableName.valueOf("TEST");
  public static final byte[] FAM = Bytes.toBytes("f1");
  public static final byte[] QUAL = Bytes.toBytes("q");

  public static void main(String[] args) throws Exception {
    Configuration conf = HBaseConfiguration.create();
    try (Connection conn = ConnectionFactory.createConnection(conf); Admin admin = conn.getAdmin()) {

      if (admin.tableExists(TABLE_NAME)) {
        if (admin.isTableEnabled(TABLE_NAME)) {
          System.out.println("Disabling " + TABLE_NAME);
          admin.disableTable(TABLE_NAME);
        }
        System.out.println("Deleting " + TABLE_NAME);
        admin.deleteTable(TABLE_NAME);
      }

      System.out.println("Creating " + TABLE_NAME);
      admin.createTable(TableDescriptorBuilder.newBuilder(TABLE_NAME)
          .setColumnFamily(ColumnFamilyDescriptorBuilder.of(FAM)).build());

      try (Table table = conn.getTable(TABLE_NAME)) {
        System.out.println("Writing data to " + TABLE_NAME);
        List<Put> puts = IntStream.range(0, 100)
            .mapToObj(i -> new Put(Bytes.toBytes("row" + i)).addColumn(FAM, QUAL, Bytes.toBytes("value" + i)))
            .collect(Collectors.toList());

        table.put(puts);

        System.out.println("Reading from " + TABLE_NAME);
        System.out.println(table.get(new Get(Bytes.toBytes("row99"))));
      }
    }
  }
}
