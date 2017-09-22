package com.tubugs.tools;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tubugs.tools.utils.FileUtil;
import com.tubugs.tools.utils.HttpUtil;
import com.tubugs.tools.utils.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by xuzhang on 2017/9/22.
 */
@Component
public class GrabAmap {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private ExecutorService threadPool = Executors.newFixedThreadPool(8);

    //结果保存路径
    @Value("${tubugs.amap.save.path}")
    private String savePath;
    //结果文本
    private StringBuilder saveContent = new StringBuilder();

    //接口地址
    @Value("${tubugs.amap.url}")
    private String amapUrl;
    //接口请求每秒最大并发数（个人用户50，企业用户200）
    private int maxQPS = 50;

    //所有坐标点
    private String[] points;
    //完成任务数
    private int completeTaskNum = 0;
    //所有任务数
    private int totalTaskNum = 0;

    public void start() throws InterruptedException {
        //1、读取文件中的坐标点
        String txt = FileUtil.readText(ResourceUtil.getAbsolutePath("input.txt"), "UTF-8");
        points = txt.split("\\r?\\n");
        for (String fromPoints : points) {
            for (String toPoints : points) {
                totalTaskNum++;
            }
        }
        //2、获取两点之间的行车路径规划（使用高德地图api接口）
        int taskNum = 0;
        for (String fromPoints : points) {
            for (String toPoints : points) {
                if (!fromPoints.equals(toPoints)) {
                    //控制qps，不要超过调用限制
                    taskNum++;
                    if (taskNum * 2 % maxQPS == 0) {
                        Thread.sleep(1000);
                    }
                    //使用线程池进行请求，提高并发量
                    threadPool.execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    String[] from = fromPoints.split("\\t");
                                    String[] to = toPoints.split("\\t");
                                    grab(from[0], from[1], from[2], to[0], to[1], to[2]);
                                }
                            }
                    );
                }
            }
        }

    }

    private void grab(String from_id, String from_lon, String from_lat, String to_id, String to_lon, String to_lat) {
        String jsonString = HttpUtil.doGet(String.format(amapUrl, from_lon + "," + from_lat, to_lon + "," + to_lat));
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        boolean requestSuccess = jsonObject.getString("info").equals("OK");
        if (!requestSuccess) {
            logger.error("request error");
            return;
        }
        JSONArray steps = jsonObject.getJSONObject("route").getJSONArray("paths").getJSONObject(0).getJSONArray("steps");
        String[] points = steps.stream().map((step) -> ((JSONObject) step).getString("polyline")).collect(Collectors.joining(";")).split(";");
        String result = Arrays.stream(points).distinct().collect(Collectors.joining(";"));
        add(String.format("%s\t%s\t%s", from_id, to_id, result));
    }

    private synchronized void add(String line) {
        //更新任务进度
        completeTaskNum++;
        logger.info(completeTaskNum + "/" + totalTaskNum);

        //生成文本并保存
        if (!StringUtils.isEmpty(saveContent.toString())) {
            saveContent.append("\r\n");
        }
        saveContent.append(line);
        if (completeTaskNum == totalTaskNum) {
            try {
                FileUtil.save(savePath, saveContent.toString());
            } catch (IOException e) {
                logger.info("写结果文件失败");
            }
            System.exit(0);
        }
    }
}
