package com.ai.cloud.skywalking.analysis.mapper;


import com.ai.cloud.skywalking.analysis.chainbuild.ChainBuildMapper;
import com.ai.cloud.skywalking.analysis.chainbuild.ChainBuildReducer;
import com.ai.cloud.skywalking.analysis.chainbuild.po.ChainInfo;
import com.ai.cloud.skywalking.analysis.config.ConfigInitializer;
import com.ai.cloud.skywalking.analysis.config.HBaseTableMetaData;
import com.ai.cloud.skywalking.protocol.Span;
import com.google.gson.Gson;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by astraea on 2016/1/15.
 */
public class CallChainMapperTest {

    //private static String ZK_QUORUM = "10.1.235.197,10.1.235.198,10.1.235.199";
    private static String ZK_QUORUM = "10.1.241.18,10.1.241.19,10.1.241.20";
    private static String ZK_CLIENT_PORT = "29181";
    private static String chain_Id = "1.0a2.1457436000002.860568c.21818.49.82";
//    private static String chain_Id = "1.0a2.1453429608422.2701d43.6468.56.1";

    private static Configuration configuration = null;
    private static Connection connection;

    public static void main(String[] args) throws Exception {
        ConfigInitializer.initialize();
        List<Span> spanList = selectByTraceId(chain_Id);
        ChainInfo chainInfo = ChainBuildMapper.spanToChainInfo(chain_Id, spanList);

        List<Text> chainInfos = new ArrayList<Text>();
        chainInfos.add(new Text(new Gson().toJson(chainInfo)));

        new ChainBuildReducer().doReduceAction(chainInfo.getCallEntrance(), chainInfos.iterator());
    }

    public static List<Span> selectByTraceId(String traceId) throws IOException {
        List<Span> entries = new ArrayList<Span>();
        Table table = connection.getTable(TableName.valueOf(HBaseTableMetaData.TABLE_CALL_CHAIN.TABLE_NAME));
        Get g = new Get(Bytes.toBytes(traceId));
        Result r = table.get(g);
        for (Cell cell : r.rawCells()) {
            if (cell.getValueArray().length > 0)
                entries.add(new Span(Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength())));
        }
        return entries;
    }

    public static List<ChainInfo> selectSpans() throws IOException, ParseException {
        List<ChainInfo> chainInfos = new ArrayList<ChainInfo>();
        Table table = connection.getTable(TableName.valueOf(HBaseTableMetaData.TABLE_CALL_CHAIN.TABLE_NAME));
        Scan scan = new Scan();
        scan.setTimeRange(new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss").parse("2016-01-10/11:55:48").getTime(), new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss").parse("2016-02-10/11:55:48").getTime());
        ResultScanner resultScanner = table.getScanner(scan);
        Iterator<Result> resultIterator = resultScanner.iterator();
        while (resultIterator.hasNext()) {
            Result result = resultIterator.next();
            List<Span> entries = new ArrayList<Span>();
            for (Cell cell : result.rawCells()) {
                if (cell.getValueArray().length > 0)
                    entries.add(new Span(Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength())));
            }
            ChainInfo chainInfo = null;
            try {
                chainInfo = ChainBuildMapper.spanToChainInfo(Bytes.toString(result.getRow()), entries);
            } catch (Exception e) {
                continue;
            }
            chainInfos.add(chainInfo);
        }

        return chainInfos;
    }


    public void initHBaseClient() throws IOException {
        if (configuration == null) {
            configuration = HBaseConfiguration.create();
            configuration.set("hbase.zookeeper.quorum", ZK_QUORUM);
            configuration.set("hbase.zookeeper.property.clientPort", ZK_CLIENT_PORT);
            connection = ConnectionFactory.createConnection(configuration);
        }

        ConfigInitializer.initialize();
    }
}