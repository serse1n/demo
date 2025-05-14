package ru.mtuci.demo.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.demo.configuration.JwtTokenProvider;
import ru.mtuci.demo.model.ApplicationDevice;
import ru.mtuci.demo.model.ApplicationUser;
import ru.mtuci.demo.model.Requests;
import ru.mtuci.demo.model.Role;
import ru.mtuci.demo.service.DeviceService;
import ru.mtuci.demo.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/devices")
@AllArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<?> getAllDevices(@RequestParam(value = "id", required = false) UUID id) {
        try {

            if (id != null) {

                ApplicationDevice device = deviceService.getDeviceById(id);

                if (device == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Device not found");
                }

                return ResponseEntity.ok(device);
            }

            return ResponseEntity.ok(deviceService.getAllDevices());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createDevice(@RequestBody Requests.CreateDeviceRequest deviceReq,
                                          @RequestHeader("Authorization") String authHeader) {
        authHeader = authHeader.replace("Bearer ", "");

        ApplicationDevice device = new ApplicationDevice();

        device.setId(UUID.randomUUID());

        if (deviceService.getDeviceByMacAddress(deviceReq.getMacAddress()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Device with this mac address already exists");
        }

        device.setMacAddress(deviceReq.getMacAddress());

        if (device.getUser() == null) {
            device.setUser(userService.findUserByEmail(jwtTokenProvider.getUsername(authHeader)));
        }

        ApplicationDevice savedDevice = deviceService.saveDevice(device);

        return ResponseEntity.ok(savedDevice.getId());
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateDevice(@RequestBody Requests.ApplicationDeviceRequest deviceRequest) {
        try {
            ApplicationDevice updatedDevice = deviceService.getDeviceByMacAddress(deviceRequest.getMacAddress());

            if (updatedDevice == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Device not found");
            }

            updatedDevice.setName(deviceRequest.getName());
            updatedDevice.setMacAddress(deviceRequest.getMacAddress());

            if (deviceRequest.getUserid() == null) {
                updatedDevice.setUser(null);
            }
            else
            {
                updatedDevice.setUser(userService.findUserById(deviceRequest.getUserid()));
            }

            deviceService.saveDevice(updatedDevice);

            return ResponseEntity.ok().body(updatedDevice.getId());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteDevice(@RequestBody Requests.DeleteDeviceRequest deviceRequest,
                                          @RequestHeader("Authorization") String authHeader) {
        try {
            authHeader = authHeader.replace("Bearer ", "");

            ApplicationDevice device = deviceService.getDeviceById(deviceRequest.getId());

            if (device == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Device not found");
            }

            ApplicationUser reqUser = userService.findUserById(device.getUser().getId());
            ApplicationUser curUser = userService.findUserByEmail(jwtTokenProvider.getUsername(authHeader));

            if (reqUser != curUser && curUser.getRole() != Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Cant't delete this device");
            }

            deviceService.deleteDevice(device);

            return ResponseEntity.ok("Device deleted");
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }
}
