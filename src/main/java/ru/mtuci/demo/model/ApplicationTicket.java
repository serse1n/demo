package ru.mtuci.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationTicket {
    private Date currentDate;
    private Date lifetime;
    private Date activationDate;
    private Date expirationDate;
    private UUID userId;
    private UUID deviceId;
    private boolean licenseBlocked;
    private String digitalSignature;
    private String info;
    private String status;
}
