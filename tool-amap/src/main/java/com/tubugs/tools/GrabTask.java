package com.tubugs.tools;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tubugs.tools.utils.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Created by xuzhang on 2017/9/23.
 */
class GrabTask implements Callable<String> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String serverUrl;
    private String fromPoints;
    private String toPoints;
    private int tryTimes = 0;
    private int num;
    private int total;

    public GrabTask(String serverUrl, String fromPoints, String toPoints, int num, int total) {
        this.serverUrl = serverUrl;
        this.fromPoints = fromPoints;
        this.toPoints = toPoints;
        this.num = num;
        this.total = total;
    }

    private String grab(String from_id, String from_lon, String from_lat, String to_id, String to_lon, String to_lat) {
        String url = String.format(serverUrl, from_lon + "," + from_lat, to_lon + "," + to_lat);
        try {
            String jsonString = HttpUtil.doGet(url);
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            boolean requestSuccess = jsonObject.getString("info").equals("OK");
            if (!requestSuccess) {
                logger.error(url + "\r\n" + jsonObject.toJSONString());
                return null;
            }
            JSONArray steps = jsonObject.getJSONObject("route").getJSONArray("paths").getJSONObject(0).getJSONArray("steps");
            String[] points = steps.stream().map((step) -> ((JSONObject) step).getString("polyline")).collect(Collectors.joining(";")).split(";");
            String result = Arrays.stream(points).distinct().collect(Collectors.joining(";"));
            return String.format("%s\t%s\t%s", from_id, to_id, result);
        } catch (Exception ex) {
            logger.error(url, ex);
            return null;
        }
    }

    @Override
    public String call() throws Exception {
        String[] from = fromPoints.split("\\t");
        String[] to = toPoints.split("\\t");
        while (tryTimes < 3) {
            String result = grab(from[0], from[1], from[2], to[0], to[1], to[2]);
            if (!StringUtils.isEmpty(result)) {
                logger.info("task {}-{} complete", total, num);
                return result;
            } else {
                tryTimes++;
            }
        }
        logger.error("抓取任务连续执行三次失败");
        return null;
    }
}
