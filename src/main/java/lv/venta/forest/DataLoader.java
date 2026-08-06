package lv.venta.forest;

import lv.venta.forest.model.*;
import lv.venta.forest.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired private AppUserRepository appUserRepository;
    @Autowired private ForestZoneRepository forestZoneRepository;
    @Autowired private ForestSensorRepository forestSensorRepository;
    @Autowired private ForestAlertRepository forestAlertRepository;
    @Autowired private SensorReadingRepository sensorReadingRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Seed users
        if (appUserRepository.count() == 0) {
            AppUser superadmin = new AppUser();
            superadmin.setUsername("ForestAdmin");
            superadmin.setPassword(passwordEncoder.encode("Forest123#"));
            superadmin.setEmail("admin@forest.lv");
            superadmin.setRole("SUPERADMIN");
            appUserRepository.save(superadmin);

            AppUser admin = new AppUser();
            admin.setUsername("ranger1");
            admin.setPassword(passwordEncoder.encode("Ranger123#"));
            admin.setEmail("ranger1@forest.lv");
            admin.setRole("ADMIN");
            appUserRepository.save(admin);

            AppUser user = new AppUser();
            user.setUsername("observer1");
            user.setPassword(passwordEncoder.encode("Observer123#"));
            user.setEmail("observer1@forest.lv");
            user.setRole("USER");
            appUserRepository.save(user);
        }

        // Seed forest zones
        if (forestZoneRepository.count() == 0) {
            ForestZone zone1 = new ForestZone();
            zone1.setName("Northern Pine Forest");
            zone1.setLocation("57.3845, 21.5608");
            zone1.setAreaHectares(450.5);
            zone1.setForestType("Pine");
            forestZoneRepository.save(zone1);

            ForestZone zone2 = new ForestZone();
            zone2.setName("Eastern Mixed Forest");
            zone2.setLocation("57.1200, 22.0100");
            zone2.setAreaHectares(320.0);
            zone2.setForestType("Mixed");
            forestZoneRepository.save(zone2);

            ForestZone zone3 = new ForestZone();
            zone3.setName("Southern Oak Reserve");
            zone3.setLocation("56.9800, 21.7500");
            zone3.setAreaHectares(180.0);
            zone3.setForestType("Oak");
            forestZoneRepository.save(zone3);

            // Seed sensors for zone1
            ForestSensor s1 = new ForestSensor();
            s1.setName("Sensor-NP-001");
            s1.setSensorType("Multi-Environmental");
            s1.setZone(zone1);
            s1.setActive(true);
            forestSensorRepository.save(s1);

            ForestSensor s2 = new ForestSensor();
            s2.setName("Sensor-EM-001");
            s2.setSensorType("Fire-Risk");
            s2.setZone(zone2);
            s2.setActive(true);
            forestSensorRepository.save(s2);

            // Seed sample readings
            SensorReading r1 = new SensorReading();
            r1.setSensor(s1);
            r1.setTimestamp(LocalDateTime.now().minusHours(1));
            r1.setTemperature(24.5);
            r1.setHumidity(65.0);
            r1.setSoilMoisture(45.0);
            r1.setWindSpeed(12.3);
            r1.setCo2Level(410.0);
            r1.setFireRiskIndex(15);
            r1.setBatteryVoltage(3.8);
            r1.setRecommendation("Normal conditions. No action required.");
            sensorReadingRepository.save(r1);

            SensorReading r2 = new SensorReading();
            r2.setSensor(s2);
            r2.setTimestamp(LocalDateTime.now().minusMinutes(30));
            r2.setTemperature(31.2);
            r2.setHumidity(28.0);
            r2.setSoilMoisture(15.0);
            r2.setWindSpeed(22.0);
            r2.setCo2Level(430.0);
            r2.setFireRiskIndex(78);
            r2.setBatteryVoltage(3.6);
            r2.setRecommendation("HIGH FIRE RISK: Consider preventive patrol and notify authorities.");
            sensorReadingRepository.save(r2);

            // Seed a sample alert
            ForestAlert alert = new ForestAlert();
            alert.setZone(zone2);
            alert.setSensor(s2);
            alert.setMessage("Critical fire risk detected. FRI >= 75. Immediate action required.");
            alert.setTimestamp(LocalDateTime.now().minusMinutes(29));
            alert.setResolved(false);
            forestAlertRepository.save(alert);
        }
    }
}
