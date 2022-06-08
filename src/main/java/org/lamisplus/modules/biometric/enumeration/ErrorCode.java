
package org.lamisplus.modules.biometric.enumeration;

import SecuGen.FDxSDKPro.jni.SGFDxErrorCode;
import lombok.Getter;

/**
 * @author
 */
public enum ErrorCode {

    SGFDX_ERROR_NONE(SGFDxErrorCode.SGFDX_ERROR_NONE, "SGFDX_ERROR_NONE", "Success"),
    SGFDX_ERROR_CREATION_FAILED(Long.valueOf(SGFDxErrorCode.SGFDX_ERROR_CREATION_FAILED), "SGFDX_ERROR_CREATION_FAILED", "JSGFPLib object creation failed"),
    SGFDX_ERROR_FUNCTION_FAILED(SGFDxErrorCode.SGFDX_ERROR_FUNCTION_FAILED, "SGFDX_ERROR_FUNCTION_FAILED", "Function call failed"),
    SGFDX_ERROR_INVALID_PARAM(SGFDxErrorCode.SGFDX_ERROR_INVALID_PARAM, "SGFDX_ERROR_INVALID_PARAM", "Invalid parameter used"),
    SGFDX_ERROR_NOT_USED(SGFDxErrorCode.SGFDX_ERROR_NOT_USED, "SGFDX_ERROR_NOT_USED", "Not used function"),
    SGFDX_ERROR_DLLLOAD_FAILED(SGFDxErrorCode.SGFDX_ERROR_DLLLOAD_FAILED, "SGFDX_ERROR_DLLLOAD_FAILED", "DLL loading failed"),
    SGFDX_ERROR_DLLLOAD_FAILED_DRV(SGFDxErrorCode.SGFDX_ERROR_DLLLOAD_FAILED_DRV, "USB UPx driver", "260*300"),
    SGFDX_ERROR_DLLLOAD_FAILED_ALG(SGFDxErrorCode.SGFDX_ERROR_DLLLOAD_FAILED_ALG, "SGFDX_ERROR_DLLLOAD_FAILED_ALG", "Algorithm DLL loading failed"),
    SGFDX_ERROR_SYSLOAD_FAILED(SGFDxErrorCode.SGFDX_ERROR_SYSLOAD_FAILED, "SGFDX_ERROR_SYSLOAD_FAILED", "Cannot find driver sys file"),
    SGFDX_ERROR_INITIALIZE_FAILED(SGFDxErrorCode.SGFDX_ERROR_INITIALIZE_FAILED, "SGFDX_ERROR_INITIALIZE_FAILED", "Chip initialization failed"),
    SGFDX_ERROR_LINE_DROPPED(SGFDxErrorCode.SGFDX_ERROR_LINE_DROPPED, "SGFDX_ERROR_LINE_DROPPED", " Image data lost"),
    SGFDX_ERROR_TIME_OUT(SGFDxErrorCode.SGFDX_ERROR_TIME_OUT, "SGFDX_ERROR_TIME_OUT", "GetImageEx() timeout"),
    SGFDX_ERROR_DEVICE_NOT_FOUND(SGFDxErrorCode.SGFDX_ERROR_DEVICE_NOT_FOUND, "SGFDX_ERROR_DEVICE_NOT_FOUND", "Device not found"),
    SGFDX_ERROR_DRVLOAD_FAILED(SGFDxErrorCode.SGFDX_ERROR_DRVLOAD_FAILED, "SGFDX_ERROR_DRVLOAD_FAILED", "Driver file load failed"),
    SGFDX_ERROR_WRONG_IMAGE(SGFDxErrorCode.SGFDX_ERROR_WRONG_IMAGE, "SGFDX_ERROR_WRONG_IMAGE", "Wrong image"),
    SGFDX_ERROR_LACK_OF_BANDWIDTH(SGFDxErrorCode.SGFDX_ERROR_LACK_OF_BANDWIDTH, "SGFDX_ERROR_LACK_OF_BANDWIDTH", "Lack of USB bandwidth"),
    SGFDX_ERROR_DEV_ALREADY_OPEN(SGFDxErrorCode.SGFDX_ERROR_DEV_ALREADY_OPEN, "SGFDX_ERROR_DEV_ALREADY_OPEN", "Device is already opened"),
    SGFDX_ERROR_GETSN_FAILED(SGFDxErrorCode.SGFDX_ERROR_GETSN_FAILED, "SGFDX_ERROR_GETSN_FAILED", "Serial number does not exist"),
    SGFDX_ERROR_UNSUPPORTED_DEV(SGFDxErrorCode.SGFDX_ERROR_UNSUPPORTED_DEV, "SGFDX_ERROR_UNSUPPORTED_DEV", "Unsupported device"),
    SGFDX_ERROR_FEAT_NUMBER(SGFDxErrorCode.SGFDX_ERROR_FEAT_NUMBER, "SGFDX_ERROR_FEAT_NUMBER", "Inadequate number of minutiae"),
    SGFDX_ERROR_INVALID_TEMPLATE_TYPE(SGFDxErrorCode.SGFDX_ERROR_INVALID_TEMPLATE_TYPE, "SGFDX_ERROR_INVALID_TEMPLATE_TYPE", "Wrong template type"),
    SGFDX_ERROR_INVALID_TEMPLATE1(SGFDxErrorCode.SGFDX_ERROR_INVALID_TEMPLATE1, "SGFDX_ERROR_INVALID_TEMPLATE1", "Error in decoding template 1"),
    SGFDX_ERROR_INVALID_TEMPLATE2(SGFDxErrorCode.SGFDX_ERROR_INVALID_TEMPLATE2, "SGFDX_ERROR_INVALID_TEMPLATE2", "Error in decoding template 2"),
    SGFDX_ERROR_EXTRACT_FAIL(SGFDxErrorCode.SGFDX_ERROR_EXTRACT_FAIL, "SGFDX_ERROR_EXTRACT_FAIL", "Extraction failed"),
    SGFDX_ERROR_MATCH_FAIL(SGFDxErrorCode.SGFDX_ERROR_MATCH_FAIL, "SGFDX_ERROR_MATCH_FAIL", "Matching failed"),
    SGFDX_ERROR_JNI_DLLLOAD_FAILED(SGFDxErrorCode.SGFDX_ERROR_JNI_DLLLOAD_FAILED, "SGFDX_ERROR_JNI_DLLLOAD_FAILED", "An error occurred while loading JSGFPLIB.DLL JNI Wrapper"),
    SGFDX_ERROR_NOT_AVAILABLE(987654321L, "SGFDX_ERROR_NOT_AVAILABLE", " Device not reachable");

    @Getter
    private final Long errorID;

    @Getter
    private final String errorName;

    @Getter
    private final String errorMessage;

    ErrorCode(Long errorID, String errorName, String errorMessage) {
        this.errorID = errorID;
        this.errorName = errorName;
        this.errorMessage = errorMessage;
    }   

    
    public static ErrorCode getErrorCode(Long errorID){
        for(ErrorCode errorCode : ErrorCode.values()){
            if (errorCode.getErrorID() == errorID){
                return errorCode;
            }
        }
        return ErrorCode.getErrorCode(987654321L);
    }
}
