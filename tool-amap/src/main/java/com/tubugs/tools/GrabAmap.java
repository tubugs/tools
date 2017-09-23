package com.tubugs.tools;

import com.tubugs.tools.utils.FileUtil;
import com.tubugs.tools.utils.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by xuzhang on 2017/9/22.
 */
@Component
public class GrabAmap {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private ExecutorService threadPool = Executors.newFixedThreadPool(8);

    //结果文本
    private StringBuilder saveContent = new StringBuilder();

    //接口地址
    @Value("${tubugs.amap.url}")
    private String amapUrl;
    //接口请求每秒最大并发数（个人用户50，企业用户200）
    private int maxQPS = 150;

    //任务执行结果
    List<Future<String>> futures = new ArrayList<>();

    public void start() throws InterruptedException, ExecutionException, IOException {
        //1、读取文件中的坐标点
        String txt = FileUtil.readText(ResourceUtil.getAbsolutePath("input.txt"), "UTF-8");
        String[] points = txt.split("\\r?\\n");

        //2、计算点的组合
        int totalTaskNum = 0;
        for (String fromPoints : points) {
            for (String toPoints : points) {
                if (!fromPoints.equals(toPoints)) {
                    totalTaskNum++;
                }
            }
        }

        //3、获取两点之间的行车路径规划（使用高德地图api接口）
        int taskNum = 0;
        for (String fromPoints : points) {
            for (String toPoints : points) {
                if (!fromPoints.equals(toPoints)) {
                    //控制并发，不要超过调用限制
                    taskNum++;
                    if (taskNum % maxQPS == 0) {
                        Thread.sleep(1000 * 3);
                    }

                    //执行抓取任务
                    GrabTask task = new GrabTask(amapUrl, fromPoints, toPoints, taskNum, totalTaskNum);
                    Future<String> future = threadPool.submit(task);
                    futures.add(future);
                }
            }
        }

        //4、获取结果
        for (Future<String> future : futures) {
            //记录每条数据
            String result = future.get();
            if (!StringUtils.isEmpty(saveContent.toString())) {
                saveContent.append("\r\n");
            }
            saveContent.append(result);
        }

        //5、保存结果到本地文件
        FileUtil.save(ResourceUtil.getAbsolutePath("output.txt"), saveContent.toString());
        System.exit(0);
    }
}
