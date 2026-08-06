package lv.venta.forest.service;

import lv.venta.forest.model.*;
import lv.venta.forest.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ForestService {

    @Autowired private ForestSensorRepository sensorRepo;
    @Autowired private SensorReadingRepository readingRepo;
    @Autowired private ForestAlertRepository alertRepo;
    @Autowired private ForestZoneRepository zoneRepo;

    public List<Map<String, Object>> getAllSensorSummaries() {
        List<Map<String, Object>> result = new ArrayList<>();
        sensorRepo.findAll().forEach(sensor -> {
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("sensorId", sensor.getSensorId());
            dto.put("type", sensor.getType());
            dto.put("label", sensor.getLocationLabel());
            dto.put("active", sensor.isActive());
            dto.put("latitude", sensor.getLatitude());
            dto.put("longitude", sensor.getLongitude());
            if (sensor.getZone() != null) {
                dto.put("zoneName", sensor.getZone().getName());
                dto.put("treeSpecies", sensor.getZone().getTreeSpecies());
                dto.put("fireRisk", sensor.getZone().getFireRisk());
            }
            // Find latest reading
            List<SensorReading> readings = new ArrayList<>();
            readingRepo.findAll().forEach(r -> { if (r.getSensor() != null && r.getSensor().getSensorId().equals(sensor.getSensorId())) readings.add(r); });
            readings.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
            if (!readings.isEmpty()) {
                SensorReading r = readings.get(0);
                dto.put("temperature", r.getTemperature());
                dto.put("humidity", r.getHumidity());
                dto.put("soilMoisture", r.getSoilMoisture());
                dto.put("windSpeed", r.getWindSpeed());
                dto.put("co2Level", r.getCo2Level());
                dto.put("fireRiskIndex", r.getFireRiskIndex());
                dto.put("batteryVoltage", r.getBatteryVoltage());
                dto.put("recommendation", r.getRecommendation());
                dto.put("timestamp", r.getTimestamp().toString());
            }
            result.add(dto);
        });
        return result;
    }

    public SensorReading submitReading(String sensorId, Map<String, Double> payload) {
        Optional<ForestSensor> opt = new ArrayList<ForestSensor>() {{ addAll((Collection<ForestSensor>) sensorRepo.findAll()); }}
            .stream().filter(s -> s.getSensorId().equals(sensorId)).findFirst();
        if (opt.isEmpty()) throw new RuntimeException("Sensor not found: " + sensorId);
        ForestSensor sensor = opt.get();
        SensorReading r = new SensorReading();
        r.setSensor(sensor);
        r.setTimestamp(LocalDateTime.now());
        r.setTemperature(payload.getOrDefault("temperature", 0.0));
        r.setHumidity(payload.getOrDefault("humidity", 0.0));
        r.setSoilMoisture(payload.getOrDefault("soilMoisture", 0.0));
        r.setWindSpeed(payload.getOrDefault("windSpeed", 0.0));
        r.setCo2Level(payload.getOrDefault("co2Level", 400.0));
        r.setFireRiskIndex(payload.getOrDefault("fireRiskIndex", 0.0));
        r.setBatteryVoltage(payload.getOrDefault("batteryVoltage", 3.7));
        // Calculate recommendation
        double fri = r.getFireRiskIndex();
        if (fri >= 75) r.setRecommendation("EVACUATE");
        else if (fri >= 50) r.setRecommendation("ALERT");
        else if (fri >= 25) r.setRecommendation("MONITOR");
        else r.setRecommendation("LOW_RISK");
        // Auto-generate alert if critical
        if (fri >= 75 && sensor.getZone() != null) {
            ForestAlert alert = new ForestAlert("FIRE_RISK", "CRITICAL",
                "Critical fire risk detected at " + sensor.getLocationLabel() + " (Index: " + fri + ")",
                sensor.getZone(), sensor);
            alertRepo.save(alert);
        }
        return readingRepo.save(r);
    }
}
