package org.lamisplus.modules.biometric.services;

import SecuGen.FDxSDKPro.jni.*;
import lombok.Data;
import org.lamisplus.modules.biometric.config.SecugenProperties;
import org.lamisplus.modules.biometric.domain.dto.BiometricTemplateDTO;
import org.lamisplus.modules.biometric.domain.dto.DeviceDTO;
import org.lamisplus.modules.biometric.domain.dto.BiometricEnrollmentDto;
import org.lamisplus.modules.biometric.enumeration.DeviceNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static SecuGen.FDxSDKPro.jni.SGPPPortAddr.USB_AUTO_DETECT;

/**
 * @author
 */
@Data
@Service
public class SecugenManager {

    private JSGFPLib sgfplib;
    private SGDeviceInfoParam deviceInfo;
    private Long error;
    private long iCount = 0L;
    private int sgFingerPositionNumber = SGFingerPosition.SG_FINGPOS_UK;

    @Autowired
    private SecugenProperties secugenProperties;

    private static Logger logger = LoggerFactory.getLogger(SecugenManager.class);

    /**
     * @param sgFDxDeviceName
     * @return
     */
    public Long boot(long sgFDxDeviceName) {
        if (this.sgfplib != null) {
            this.sgfplib.CloseDevice();
            this.sgfplib.Close();
            this.sgfplib = null;
        }

        this.sgfplib = new JSGFPLib();

        //Init
        error = sgfplib.Init(SGFDxDeviceName.SG_DEV_AUTO);
        //Open Device
        error = this.openDeviceWithUsbAutoDetect();
        //Get Device Information
        deviceInfo = new SGDeviceInfoParam();
        error = sgfplib.GetDeviceInfo(deviceInfo);
        //setTimeout
        this.secugenProperties.setTimeout(180000L);
        return error;
    }

    public Long boot() {
        return this.boot(SGFDxDeviceName.SG_DEV_AUTO);
    }

    /*public JSGFPLib getSgfplib() {
        if (this.sgfplib == null) {
            this.boot();
        }
        return this.sgfplib;
    }*/

    public Long openDeviceWithUsbAutoDetect() {
        return this.sgfplib.OpenDevice(USB_AUTO_DETECT);
    }

    public Long closeDevice() {
        return this.sgfplib.Close();
    }

    public void setLedOn(boolean ledStatus) {
        this.sgfplib.SetLedOn(ledStatus);
    }

    /**
     * @return Device
     */
    public DeviceDTO getBasicDeviceInfo() {
        DeviceNames deviceNames = DeviceNames.getDeviceNames(deviceInfo.deviceID, deviceInfo.imageWidth, deviceInfo.imageHeight);
        DeviceDTO deviceDTO = new DeviceDTO();
        deviceDTO.setId(deviceNames.getDeviceID() + "");
        deviceDTO.setName(deviceNames.name());
        deviceDTO.setDeviceName(deviceNames);
        deviceDTO.setImageWidth(deviceInfo.imageWidth);
        deviceDTO.setImageHeight(deviceInfo.imageHeight);
        deviceDTO.setImageDPI(deviceInfo.imageDPI);
        deviceDTO.setGain(deviceInfo.gain);
        deviceDTO.setBrightness(deviceInfo.brightness);
        deviceDTO.setContrast(deviceInfo.contrast);
        deviceDTO.setFWVersion(deviceInfo.FWVersion);
        return deviceDTO;
    }

    /**
     * @return
     */
    public byte[] captureFingerPrintImage() {
        byte[] imageBuffer = new byte[deviceInfo.imageWidth * deviceInfo.imageHeight];
        Long timeout = secugenProperties.getTimeout();
        Long quality = secugenProperties.getQuality();
        error = this.sgfplib.GetImageEx(imageBuffer, timeout, 1, quality);

        if (error != SGFDxErrorCode.SGFDX_ERROR_NONE) {
            logger.info("Image capture error: " + error);
            if (iCount < 1L) {
                iCount++;
                this.captureFingerPrintImage();
            }
            return null;
        }
        iCount = 0L;
        return imageBuffer;
    }

    public List<DeviceDTO> getDevices() {
        List<DeviceDTO> deviceDTOS = new ArrayList<>();
        DeviceNames[] deviceNames = DeviceNames.values();
        for (int i = deviceNames.length-1; i > 0; i--) {
            DeviceDTO deviceDTO = new DeviceDTO();
            deviceDTO.setId(deviceNames[i].getDeviceID().toString());
            deviceDTO.setName(deviceNames[i].getDeviceDriver().replace("/", "OR"));
            deviceDTO.setDeviceName(deviceNames[i]);
            deviceDTOS.add(deviceDTO);
        }
        return deviceDTOS;
    }

    public Long getDeviceId(String deviceName) {
        deviceName = deviceName.replace("OR", "/");
        for (DeviceNames dn : DeviceNames.values()) {
            if (dn.getDeviceDriver().equalsIgnoreCase(deviceName) || dn.getDeviceID().toString().equalsIgnoreCase(deviceName)) {
                return dn.getDeviceID();
            }
        }
        return SGFDxDeviceName.SG_DEV_AUTO;
    }

    /**
     * @param imageBuffer
     * @return
     */
    public int getImageQuality(byte[] imageBuffer) {
        if (imageBuffer == null) {
            return 0;
        }
        Long quality = secugenProperties.getQuality();
        int[] img_qlty = new int[1];
        this.sgfplib.GetImageQuality(deviceInfo.imageWidth, deviceInfo.imageHeight, imageBuffer, img_qlty);
        if (img_qlty[0] < quality) {
            logger.info("Image quality below " + quality + ": " + img_qlty[0]);
        }
        return img_qlty[0];
    }

    /**
     * @param imageBuffer
     * @param imageQuality
     * @return
     */
    public byte[] createTemplateFromCapturedImage(byte[] imageBuffer, int imageQuality) {
        if (imageQuality == 0) {
            return new byte[0];
        }
        int[] maxTemplateSize = new int[1];
        error = this.sgfplib.GetMaxTemplateSize(maxTemplateSize);
        byte[] regTemplate = new byte[maxTemplateSize[0]];

        SGFingerInfo finger_info = new SGFingerInfo();
        //Finger Position/Type: SG_FINGPOS_UK -> Unknown Finger
        finger_info.FingerNumber = this.sgFingerPositionNumber;
        finger_info.ImageQuality = imageQuality;
        finger_info.ImpressionType = SGImpressionType.SG_IMPTYPE_LP;
        finger_info.ViewNumber = 1; //The number of the view. This is important if there are multiple samples of the same finger.
        error = this.sgfplib.CreateTemplate(finger_info, imageBuffer, regTemplate);
        if (error == SGFDxErrorCode.SGFDX_ERROR_NONE) {
            return regTemplate;
        } else {
            return null;
        }
    }

    /**
     * @param template1
     * @param template2
     * @return 
     */
    public Boolean matchTemplate(byte[] template1, byte[] template2) {
        boolean[] matched = new boolean[1];
        long sl = SGFDxSecurityLevel.SL_NORMAL;
        if ( (template1.length - template2.length) > 200){
            return false;
        }
        error = this.sgfplib.MatchTemplate(template1, template2, sl, matched);
        return matched[0];
    }
    
    /**
     * @return
     */
    public BiometricEnrollmentDto captureFingerPrint(BiometricEnrollmentDto biometric) {
        try {
            //Instant start = Instant.now();
            this.sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_ISO19794);
            long iError;
            // Get first fingerprint image and create template from image
            byte[] imageBuffer = this.captureFingerPrintImage();
            int imageQuality = this.getImageQuality(imageBuffer);
            byte[] regTemplate = this.createTemplateFromCapturedImage(imageBuffer, imageQuality);
            biometric.setIso(true);

            // Get second fingerprint image and create template from image
            byte[] imageBuffer2 = this.captureFingerPrintImage();
            int imageQuality2 = this.getImageQuality(imageBuffer2);
            byte[] regTemplate2 = this.createTemplateFromCapturedImage(imageBuffer2, imageQuality2);
            
            if (regTemplate != null && regTemplate2 != null) {
                Boolean matched = this.matchTemplate(regTemplate, regTemplate2);
                if (matched) {
                    int[] score = new int[1];

                    iError = this.sgfplib.GetMatchingScore(regTemplate, regTemplate2, score);
                    if (iError == SGFDxErrorCode.SGFDX_ERROR_NONE) {
                        biometric.setMatchingScore(score[0]);
                        if (score[0] >= 80) {   // Enroll these fingerprints to database
                            biometric.setImage(imageQuality > imageQuality2 ? imageBuffer : imageBuffer2);
                            biometric.setImageQuality(imageQuality > imageQuality2 ? imageQuality : imageQuality2);
                            biometric.setTemplate(imageQuality > imageQuality2 ? regTemplate : regTemplate2);
                            biometric.setImageWeight(deviceInfo.imageWidth);
                            biometric.setImageHeight(deviceInfo.imageHeight);
                            biometric.setImageResolution(deviceInfo.imageDPI);
                            //biometricTemplate = mapBiometricTemplate(biometricTemplate, imageQuality, imageQuality2, regTemplate, regTemplate2, imageBuffer, imageBuffer2);
                        } else {
                            //biometricTemplate.setId("QUALITY_ERROR");
                            biometric.getMessage().put("QUALITY_ERROR", "Quality is less than " + score[0]);
                            biometric.setType(BiometricEnrollmentDto.Type.ERROR);
                            return biometric;
                        }
                    } else if (iError == SGFDxErrorCode.SGFDX_ERROR_TIME_OUT) {
                        //biometricTemplate.setId("TIME_OUT");
                        biometric.getMessage().put("TIME_OUT", "TIME_OUT");
                        biometric.setType(BiometricEnrollmentDto.Type.ERROR);
                        return biometric;
                    } else {
                        //biometricTemplate.setId("CAPTURE_ERROR");
                        biometric.getMessage().put("CAPTURE_ERROR", "CAPTURE_ERROR");
                        biometric.setType(BiometricEnrollmentDto.Type.ERROR);
                    }
                } else {
                    //biometricTemplate.setId("MATCH_ERROR");
                    biometric.getMessage().put("MATCH_ERROR", "MATCH_ERROR");
                    biometric.setType(BiometricEnrollmentDto.Type.ERROR);
                    return biometric;
                }
                if(biometric.getType() == null){
                    biometric.setType(BiometricEnrollmentDto.Type.SUCCESS);
                }
                return biometric;
            } else {
                biometric.getMessage().put("CAPTURE_ERROR", "CAPTURE_ERROR");
                biometric.setType(BiometricEnrollmentDto.Type.ERROR);
                return biometric;
            }
        }
        catch(Exception e){
            biometric.getMessage().put("Finger Print Capture Error: ", e.getMessage());
            biometric.setType(BiometricEnrollmentDto.Type.ERROR);
            logger.info("Finger Print Capture Error: "+e.getMessage());
        }
        if(biometric.getType() == null){
            biometric.setType(BiometricEnrollmentDto.Type.SUCCESS);
        }
        return biometric;
    }

    
    /**
     * @param storedTemplate
     * @return
     */
    public BiometricTemplateDTO verifyFingerPrint(byte[] storedTemplate) {

        this.sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_ISO19794);

        // Get first fingerprint image and create template from image
        byte[] imageBuffer = this.captureFingerPrintImage();
        int imageQuality = this.getImageQuality(imageBuffer);
        byte[] regTemplate = this.createTemplateFromCapturedImage(imageBuffer, imageQuality);

        // Get second fingerprint image and create template from image
        byte[] imageBuffer2 = this.captureFingerPrintImage();
        int imageQuality2 = this.getImageQuality(imageBuffer2);
        byte[] regTemplate2 = this.createTemplateFromCapturedImage(imageBuffer2, imageQuality2);
        
        Boolean matched = this.matchTemplate(regTemplate, storedTemplate);
        Boolean matched2 = this.matchTemplate(regTemplate2, storedTemplate);
                        
        BiometricTemplateDTO biometricTemplateDTO = new BiometricTemplateDTO();
        if (matched && matched2) {
            /*biometricTemplate.setImage(imageQuality > imageQuality2 ? imageBuffer : imageBuffer2);
            biometricTemplate.setImageQuality(imageQuality > imageQuality2 ? imageQuality : imageQuality2);
            biometricTemplate.setTemplate(imageQuality > imageQuality2 ? regTemplate : regTemplate2);
            biometricTemplate.setImageWeight(deviceInfo.imageWidth);
            biometricTemplate.setImageHeight(deviceInfo.imageHeight);
            biometricTemplate.setImageResolution(deviceInfo.imageDPI);*/
            return mapBiometricTemplate(biometricTemplateDTO, imageQuality, imageQuality2, regTemplate, regTemplate2, imageBuffer, imageBuffer2);
        }
        return null;
    }

    private BiometricTemplateDTO mapBiometricTemplate(BiometricTemplateDTO biometricTemplateDTO, int imageQuality, int imageQuality2,
                                                      byte[] regTemplate, byte[] regTemplate2, byte[] imageBuffer, byte[] imageBuffer2){
        biometricTemplateDTO.setImage(imageQuality > imageQuality2 ? imageBuffer : imageBuffer2);
        biometricTemplateDTO.setImageQuality(imageQuality > imageQuality2 ? imageQuality : imageQuality2);
        biometricTemplateDTO.setTemplate(imageQuality > imageQuality2 ? regTemplate : regTemplate2);
        biometricTemplateDTO.setImageWeight(deviceInfo.imageWidth);
        biometricTemplateDTO.setImageHeight(deviceInfo.imageHeight);
        biometricTemplateDTO.setImageResolution(deviceInfo.imageDPI);

        return biometricTemplateDTO;
    }

}
