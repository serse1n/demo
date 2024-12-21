package ru.mtuci.demo.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationDevice;
import ru.mtuci.demo.model.ApplicationDeviceLicense;
import ru.mtuci.demo.repository.DeviceRepository;
import ru.mtuci.demo.service.DeviceLicenseService;
import ru.mtuci.demo.service.DeviceService;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceLicenseService deviceLicenseService;

    @Override
    public ApplicationDevice saveDevice(ApplicationDevice device) {
        return deviceRepository.save(device);
    }

    @Override
    public ApplicationDevice getDeviceById(UUID id) {
        return deviceRepository.findById(id)
                .orElse(null);
    }

    @Override
    public ApplicationDevice getDeviceByMacAddress(String macAddress) {
        return deviceRepository.findByMacAddress(macAddress);
    }

    @Override
    public List<ApplicationDevice> getAllDevices() {
        return deviceRepository.findAll();
    }

    @Override
    public ApplicationDevice updateDevice(ApplicationDevice device) {
        return deviceRepository.save(device);
    }

    @Override
    public void deleteDevice(ApplicationDevice device) {

        List<ApplicationDeviceLicense> applicationDeviceLicenses = deviceLicenseService.getDeviceLicensesByDeviceId(device.getId());

        if (!applicationDeviceLicenses.isEmpty()) {
            for (ApplicationDeviceLicense i : applicationDeviceLicenses) {
                deviceLicenseService.deleteById(i.getId());
            }
        }

        deviceRepository.deleteById(device.getId());
    }
}
