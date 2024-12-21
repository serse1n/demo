package ru.mtuci.demo.service;

import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationDevice;

import java.util.List;
import java.util.UUID;

@Service
public interface DeviceService {
    ApplicationDevice getDeviceById(UUID id);
    List<ApplicationDevice> getAllDevices();
    ApplicationDevice updateDevice(ApplicationDevice device);
    void deleteDevice(ApplicationDevice device);
    ApplicationDevice saveDevice(ApplicationDevice device);
    ApplicationDevice getDeviceByMacAddress(String macAddress);
}
