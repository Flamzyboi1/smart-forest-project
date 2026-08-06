package lv.venta.forest.controller;

import lv.venta.forest.model.*;
import lv.venta.forest.service.ForestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forest")
public class ForestController {

    @Autowired
    private ForestService forestService;

    // === ZONES ===
    @GetMapping("/zones")
    public List<ForestZone> getAllZones() {
        return forestService.getAllZones();
    }

    @GetMapping("/zones/{id}")
    public ResponseEntity<ForestZone> getZoneById(@PathVariable Long id) {
        return forestService.getZoneById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @PostMapping("/zones")
    public ForestZone createZone(@RequestBody ForestZone zone) {
        return forestService.saveZone(zone);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @PutMapping("/zones/{id}")
    public ResponseEntity<ForestZone> updateZone(@PathVariable Long id, @RequestBody ForestZone zone) {
        return forestService.getZoneById(id).map(existing -> {
            existing.setName(zone.getName());
            existing.setLocation(zone.getLocation());
            existing.setAreaHectares(zone.getAreaHectares());
            existing.setForestType(zone.getForestType());
            return ResponseEntity.ok(forestService.saveZone(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('SUPERADMIN')")
    @DeleteMapping("/zones/{id}")
    public ResponseEntity<Void> deleteZone(@PathVariable Long id) {
        forestService.deleteZone(id);
        return ResponseEntity.noContent().build();
    }

    // === SENSORS ===
    @GetMapping("/sensors")
    public List<ForestSensor> getAllSensors() {
        return forestService.getAllSensors();
    }

    @GetMapping("/sensors/{id}")
    public ResponseEntity<ForestSensor> getSensorById(@PathVariable Long id) {
        return forestService.getSensorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @PostMapping("/sensors")
    public ForestSensor createSensor(@RequestBody ForestSensor sensor) {
        return forestService.saveSensor(sensor);
    }

    @PreAuthorize("hasRole('SUPERADMIN')")
    @DeleteMapping("/sensors/{id}")
    public ResponseEntity<Void> deleteSensor(@PathVariable Long id) {
        forestService.deleteSensor(id);
        return ResponseEntity.noContent().build();
    }

    // === SENSOR READINGS ===
    @GetMapping("/readings")
    public List<SensorReading> getAllReadings() {
        return forestService.getAllReadings();
    }

    @GetMapping("/readings/sensor/{sensorId}")
    public List<SensorReading> getReadingsBySensor(@PathVariable Long sensorId) {
        return forestService.getReadingsBySensorId(sensorId);
    }

    @PostMapping("/readings")
    public SensorReading saveReading(@RequestBody SensorReading reading) {
        return forestService.saveReading(reading);
    }

    // === ALERTS ===
    @GetMapping("/alerts")
    public List<ForestAlert> getAllAlerts() {
        return forestService.getAllAlerts();
    }

    @GetMapping("/alerts/zone/{zoneId}")
    public List<ForestAlert> getAlertsByZone(@PathVariable Long zoneId) {
        return forestService.getAlertsByZoneId(zoneId);
    }

    @GetMapping("/alerts/unresolved")
    public List<ForestAlert> getUnresolvedAlerts() {
        return forestService.getUnresolvedAlerts();
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @PutMapping("/alerts/{id}/resolve")
    public ResponseEntity<ForestAlert> resolveAlert(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(forestService.resolveAlert(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('SUPERADMIN')")
    @DeleteMapping("/alerts/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        forestService.deleteAlert(id);
        return ResponseEntity.noContent().build();
    }
}
