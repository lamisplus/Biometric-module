package org.lamisplus.modules.biometric.domain.dto;

import lombok.Data;
import org.lamisplus.modules.biometric.enumeration.DeviceNames;

@Data
public class DeviceDTO {
    private String id;
    private String name;
    private DeviceNames deviceName;
    private int imageWidth, 
            imageHeight, 
            contrast, 
            brightness, 
            gain, 
            imageDPI, 
            FWVersion;
}
