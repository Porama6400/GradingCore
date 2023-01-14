package dev.porama.gradingcore.core.metrics;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import dev.porama.gradingcore.core.grader.data.GradingRequest;
import dev.porama.gradingcore.core.grader.data.GradingResult;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MetricsManager {
    private final String influxUrl;
    private final String nodeId;
    private final List<Point> points = new ArrayList<>();
    private final InfluxDBClient influx;
    private final WriteApi writeApi;

    public MetricsManager(String influxUrl, String influxToken, String influxOrg, String influxBucket, String nodeId) {
        this.influxUrl = influxUrl;
        this.nodeId = nodeId;

        influx = InfluxDBClientFactory.create(influxUrl, influxToken.toCharArray(), influxOrg, influxBucket);
        writeApi = influx.makeWriteApi();
    }

    public void handleRequest(GradingRequest request) {
        synchronized (points) {
            points.add(Point.measurement("request")
                    .time(System.currentTimeMillis(), WritePrecision.MS)
                    .addTag("node", nodeId)
                    .addTag("type", request.getType())
                    .addField("dummy", 0)
            );
        }
    }

    public void handleResponse(GradingRequest request, GradingResult gradingResult) {
        synchronized (points) {
            points.add(Point.measurement("response")
                    .time(System.currentTimeMillis(), WritePrecision.MS)
                    .addTag("node", nodeId)
                    .addTag("status", gradingResult.getStatus().toString())
                    .addTag("type", request.getType())
                    .addField("duration", gradingResult.getDuration())
            );
        }
    }

    public void tickPublish() {
        synchronized (points) {
            writeApi.writePoints(points);
            points.clear();
        }
    }
}
