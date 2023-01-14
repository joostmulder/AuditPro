//
//  KDCReader.h
//  KDCReader
//
//  Created by SeungWoo Han on 9/14/14.
//  Copyright (c) 2014 AISolution. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <ExternalAccessory/ExternalAccessory.h>
#import "KDCConstants.h"
#import "KDCData.h"
#import "KPOSData.h"
#import "KPOSResult.h"
#import "KPOSConstants.h"
#import "KDCDevice.h"
#import "UHFResult.h"
#import "KDCResponse.h"

#if (1)
#define DEPRECATED
#else
#if !defined(DEPRECATED)
#define DEPRECATED __deprecated
#endif
#endif

@interface KDCReader : NSObject

- (NSMutableArray *) GetAvailableDeviceList;
- (NSMutableArray<KDCDevice *> *) GetAvailableDeviceListEx:(enum DeviceListType)type options:(NSDictionary *)options;

- (EAAccessory *) GetConnectedDevice DEPRECATED;
- (NSString *) GetConnectedDeviceName DEPRECATED;

- (KDCDevice *) GetConnectedDeviceEx;

- (NSString *) GetSDKRevisionHistory;
- (NSString *) GetKDCReaderVersion;

- (BOOL) Connect;
- (BOOL) Connect:(EAAccessory *)accessory;

- (BOOL) ConnectEx;
- (BOOL) ConnectEx:(id)targetDevice;

- (void) Disconnect;

- (BOOL) IsKDCConnected DEPRECATED;
- (BOOL) IsConnected;

- (void) SoftwareTrigger;
- (void) SoftwareTrigger:(BOOL)isWakeup;

//Device features
- (uint8_t *) GetSerialNumber DEPRECATED;
- (uint8_t *) GetFirmwareVersion DEPRECATED;
- (uint8_t *) GetModelName DEPRECATED;
- (uint8_t *) GetKDCSerialNumber;
- (uint8_t *) GetKDCFirmwareVersion;
- (uint8_t *) GetKDCFirmwareBuild;
- (uint8_t *) GetKDCModelName;

- (NSString *) GetKDCSerialNumber:(bool)fromRemote;

- (BOOL) Is2DSupported DEPRECATED;
- (BOOL) IsModel2D;

- (BOOL) IsNFCSupported;
- (BOOL) IsGPSSupported;
- (BOOL) IsMSRSupported;
- (BOOL) IsPOSSupported;
- (BOOL) IsBarcodeSupported;
- (BOOL) IsKeyPadSupported;
- (BOOL) IsFlashSupported;
- (BOOL) IsVibratorSupported;
- (BOOL) IsDisplaySupported;
- (BOOL) IsUHFSupported;
- (BOOL) IsCMDASupported;
- (BOOL) IsWiFiSupported;
- (BOOL) IsPassportReaderSupported;
- (BOOL) IsFingerPrintSupported;

//KDC Mode
- (BOOL) SetKDCMode:(enum KDCMode)mode;
- (enum KDCMode) GetKDCMode;

//Data related
- (enum DataType) GetDataType;
- (enum NFCTag) GetNFCTagType;

- (NSString *) GetData;
- (Byte *) GetDataBytes;
- (int) GetDataBytesLength;

- (NSString *) GetNFCUID;
- (NSString *) GetNFCUIDReversed;
- (NSString *) GetNFCData;
- (NSString *) GetNFCDataBase64;

- (NSArray *) GetUHFList;
- (enum UHFDataType) GetUHFListDataType;
- (NSArray *) GetUHFRssiList;

- (uint8_t *) GetNFCRawData DEPRECATED;
- (Byte *) GetNFCDataBytes;

- (int) GetNFCRawDataLength DEPRECATED;
- (int) GetNFCDataBytesLength;

- (NSString *) GetBarcodeData;
- (uint8_t *) GetBarcodeRawData DEPRECATED;
- (Byte *) GetBarcodeDataBytes;
- (int) GetBarcodeRawDataLength DEPRECATED;
- (int) GetBarcodeDataBytesLength;

- (NSString *) GetMSRData;
- (uint8_t *) GetMSRRawData DEPRECATED;
- (Byte *) GetMSRDataBytes;
- (int) GetMSRRawDataLength DEPRECATED;
- (int) GetMSRDataBytesLength;

- (NSString *) GetGPSData;

- (NSString *) GetRecordData DEPRECATED;
- (NSString *) GetRecord;

- (NSDateComponents *) GetTimestamp;

- (NSString *) GetKeyEvent;

- (void) EnableAttachType:(BOOL)enabled;
- (void) EnableAttachTimestamp:(BOOL)enabled;
- (void) EnableAttachSerialNumber:(BOOL)enabled;
- (void) EnableAttachLocation:(BOOL)enabled;
- (void) EnableAttachAppQuantity:(BOOL)enabled;

- (void) SetDataDelimiter:(enum DataDelimiter)delimiter;
- (NSString *) GetDataDelimiter;
- (void) SetRecordDelimiter:(enum RecordDelimiter)delimiter;
- (NSString *) GetRecordDelimiter;

//NFC related
- (BOOL) IsNFCInstalled;

- (enum NFCDataFormat) GetNFCDataFormat;
- (BOOL) SetNFCDataFormat:(enum NFCDataFormat)format;

- (BOOL) EnableNFCPower:(enum EnableDisable)enabled;
- (enum EnableDisable) IsNFCPowerEnabled;

- (BOOL) EnableNFCUIDOnly:(enum EnableDisable)enabled;
- (enum EnableDisable) IsNFCUIDOnlyEnabled;
- (BOOL) EnableNFCExtendedFormat:(enum EnableDisable)enabled;
- (enum EnableDisable) IsNFCExtendedFormatEnabled;

- (BOOL) WriteNDEFDataToNFCTag:(Byte *)nfcNDEFData length:(int)length;
- (BOOL) WriteBinaryDataToNFCTag:(Byte *)nfcData length:(int)length;

//Bluetooth related
- (uint8_t *) GetBluetoothMacAddress DEPRECATED;
- (uint8_t *) GetBluetoothMACAddress;
- (uint8_t *) GetBluetoothFirmwareVersion;

- (BOOL) EnableAutoConnect:(enum EnableDisable)setting DEPRECATED;
- (BOOL) EnableBluetoothAutoConnect:(enum EnableDisable)setting;
- (enum EnableDisable) IsAutoConnectEnabled;

- (BOOL) EnableAutoReconnect:(enum EnableDisable)setting;
- (enum EnableDisable) IsAutoReconnectEnabled;

- (BOOL) EnableAutoPowerOn:(enum EnableDisable)setting DEPRECATED;
- (BOOL) EnableBluetoothAutoPowerOn:(enum EnableDisable)setting;
- (enum EnableDisable) IsAutoPowerOnEnabled;

- (BOOL) EnablePowerOffMessage:(enum EnableDisable)setting DEPRECATED;
- (BOOL) EnableBluetoothPowerOffMessage:(enum EnableDisable)setting;
- (enum EnableDisable) IsPowerOffMessageEnabled;

- (BOOL) EnableWakeupNulls:(enum EnableDisable)setting DEPRECATED;
- (BOOL) EnableBluetoothWakeupNull:(enum EnableDisable)setting;
- (enum EnableDisable) IsWakeupNullsEnabled;

- (BOOL) EnableBluetoothToggle:(enum EnableDisable)setting;
- (enum EnableDisable) IsBluetoothToggleEnabled;

- (BOOL) EnableBluetoothDisconnectButton:(enum EnableDisable)setting;
- (enum EnableDisable) IsBluetoothDisconnectButtonEnabled;

- (BOOL) SetBluetoothPowerOnTime:(enum PowerOnTime)powerontime DEPRECATED;
- (enum PowerOnTime) GetBluetoothPowerOnTime DEPRECATED;
- (BOOL) SetBluetoothAutoPowerOnDelay:(enum PowerOnTime)powerontime;
- (enum PowerOnTime) GetBluetoothAutoPowerOnDelay;

- (BOOL) EnableBluetoothBeepWarning:(enum EnableDisable)setting;
- (enum EnableDisable) IsBluetoothBeepWarningEnabled;

- (BOOL) SetBluetoothAutoPowerOffTimeout:(enum AutoPowerOffTimeout)powerofftimeout;
- (enum AutoPowerOffTimeout) GetBluetoothAutoPowerOffTimeout;

- (BOOL) SetDeviceProfile:(enum DeviceProfile)profile;
- (enum DeviceProfile) GetDeviceProfile;

//Barcode related
- (BOOL) IsAllSymbologiesEnabled;
- (BOOL) DisableAllSymbologies;
- (BOOL) EnableAllSymbologies;
- (BOOL) IsAllOptionsEnabled;
- (BOOL) DisableAllOptions;
- (BOOL) EnableAllOptions;

- (struct BarcodeSymbolSettings) GetCurrentBarcodeSymbolSettings DEPRECATED;
- (BOOL) SetCurrentBarcodeSymbolSettings:(struct BarcodeSymbolSettings) settings DEPRECATED;
- (struct BarcodeSymbolSettings) GetSymbology;
- (BOOL) SetSymbology:(struct BarcodeSymbolSettings) settings;

- (BOOL) EnableDisableEAN13:(enum EnableDisable)enabled;
- (enum EnableDisable) IsEAN13Enabled;
- (BOOL) EnableDisableEAN8:(enum EnableDisable)enabled;
- (enum EnableDisable) IsEAN8Enabled;
- (BOOL) EnableDisableUPCA:(enum EnableDisable)enabled;
- (enum EnableDisable) IsUPCAEnabled;
- (BOOL) EnableDisableUPCE:(enum EnableDisable)enabled;
- (enum EnableDisable) IsUPCEEnabled;
- (BOOL) EnableDisableUPCE0:(enum EnableDisable)enabled;
- (enum EnableDisable) IsUPCE0Enabled;
- (BOOL) EnableDisableUPCE1:(enum EnableDisable)enabled;
- (enum EnableDisable) IsUPCE1Enabled;
- (BOOL) EnableDisableCODE39:(enum EnableDisable)enabled;
- (enum EnableDisable) IsCODE39Enabled;
- (BOOL) EnableDisableITF14:(enum EnableDisable)enabled;
- (enum EnableDisable) IsITF14Enabled;
- (BOOL) EnableDisableI2OF5:(enum EnableDisable)enabled;
- (enum EnableDisable) IsI2OF5Enabled;
- (BOOL) EnableDisableCODABAR:(enum EnableDisable)enabled;
- (enum EnableDisable) IsCODABAREnabled;
- (BOOL) EnableDisableCODE128:(enum EnableDisable)enabled;
- (enum EnableDisable) IsCODE128Enabled;
- (BOOL) EnableDisableGS1128:(enum EnableDisable)enabled;
- (enum EnableDisable) IsGS1128Enabled;
- (BOOL) EnableDisableCODE93:(enum EnableDisable)enabled;
- (enum EnableDisable) IsCODE93Enabled;
- (BOOL) EnableDisableCODE35:(enum EnableDisable)enabled;
- (enum EnableDisable) IsCODE35Enabled;
- (BOOL) EnableDisableBOOKLAND:(enum EnableDisable)enabled;
- (enum EnableDisable) IsBOOKLANDEnabled;
- (BOOL) EnableDisableEAN13_ADDON:(enum EnableDisable)enabled;
- (enum EnableDisable) IsEAN13_ADDONEnabled;
- (BOOL) EnableDisableEAN8_ADDON:(enum EnableDisable)enabled;
- (enum EnableDisable) IsEAN8_ADDONEnabled;
- (BOOL) EnableDisableUPCA_ADDON:(enum EnableDisable)enabled;
- (enum EnableDisable) IsUPCA_ADDONEnabled;
- (BOOL) EnableDisableUPCE_ADDON:(enum EnableDisable)enabled;
- (enum EnableDisable) IsUPCE_ADDONEnabled;
- (BOOL) EnableDisableCODE11:(enum EnableDisable)enabled;
- (enum EnableDisable) IsCODE11Enabled;
- (BOOL) EnableDisableCODE32:(enum EnableDisable)enabled;
- (enum EnableDisable) IsCODE32Enabled;
- (BOOL) EnableDisableMATRIX2OF5:(enum EnableDisable)enabled;
- (enum EnableDisable) IsMATRIX2OF5Enabled;
- (BOOL) EnableDisableMSI:(enum EnableDisable)enabled;
- (enum EnableDisable) IsMSIEnabled;
- (BOOL) EnableDisablePLESSY:(enum EnableDisable)enabled;
- (enum EnableDisable) IsPLESSYEnabled;
- (BOOL) EnableDisablePOSICODE:(enum EnableDisable)enabled;
- (enum EnableDisable) IsPOSICODEEnabled;
- (BOOL) EnableDisableGS1OMNI:(enum EnableDisable)enabled;
- (enum EnableDisable) IsGS1OMNIEnabled;
- (BOOL) EnableDisableGS1LIMITED:(enum EnableDisable)enabled;
- (enum EnableDisable) IsGS1LIMITEDEnabled;
- (BOOL) EnableDisableGS1EXPANDED:(enum EnableDisable)enabled;
- (enum EnableDisable) IsGS1EXPANDEDEnabled;
- (BOOL) EnableDisableS2OF5IND:(enum EnableDisable)enabled;
- (enum EnableDisable) IsS2OF5INDEnabled;
- (BOOL) EnableDisableS2OF5IATA:(enum EnableDisable)enabled;
- (enum EnableDisable) IsS2OF5IATAEnabled;
- (BOOL) EnableDisableTLC39:(enum EnableDisable)enabled;
- (enum EnableDisable) IsTLC39Enabled;
- (BOOL) EnableDisableTELEPEN:(enum EnableDisable)enabled;
- (enum EnableDisable) IsTELEPENEnabled;
- (BOOL) EnableDisableTRIOPTIC:(enum EnableDisable)enabled;
- (enum EnableDisable) IsTRIOPTICEnabled;
- (BOOL) EnableDisableAZTEC:(enum EnableDisable)enabled;
- (enum EnableDisable) IsAZTECEnabled;
- (BOOL) EnableDisableAZTECRUNES:(enum EnableDisable)enabled;
- (enum EnableDisable) IsAZTECRUNESEnabled;
- (BOOL) EnableDisableCODABLOCKF:(enum EnableDisable)enabled;
- (enum EnableDisable) IsCODABLOCKFEnabled;
- (BOOL) EnableDisableCODE16K:(enum EnableDisable)enabled;
- (enum EnableDisable) IsCODE16KEnabled;
- (BOOL) EnableDisableCODE49:(enum EnableDisable)enabled;
- (enum EnableDisable) IsCODE49Enabled;
- (BOOL) EnableDisableDATAMATRIX:(enum EnableDisable)enabled;
- (enum EnableDisable) IsDATAMATRIXEnabled;
- (BOOL) EnableDisableMAXICODE:(enum EnableDisable)enabled;
- (enum EnableDisable) IsMAXICODEEnabled;
- (BOOL) EnableDisableMICROPDF:(enum EnableDisable)enabled;
- (enum EnableDisable) IsMICROPDFEnabled;
- (BOOL) EnableDisablePDF417:(enum EnableDisable)enabled;
- (enum EnableDisable) IsPDF417Enabled;
- (BOOL) EnableDisableQRCODE:(enum EnableDisable)enabled;
- (enum EnableDisable) IsQRCODEEnabled;
- (BOOL) EnableDisableHANXIN:(enum EnableDisable)enabled;
- (enum EnableDisable) IsHANXINEnabled;
- (BOOL) DisableOCR;
- (enum EnableDisable) IsOCREnabled;
- (BOOL) EnableOCRA;
- (enum EnableDisable) IsOCRAEnabled;
- (BOOL) EnableOCRB;
- (enum EnableDisable) IsOCRBEnabled;
- (BOOL) EnableOCRUSC;
- (enum EnableDisable) IsOCRUSCEnabled;
- (BOOL) EnableOCRMICR;
- (enum EnableDisable) IsOCRMICREnabled;
- (BOOL) EnableOCRSEMI;
- (enum EnableDisable) IsOCRSEMIEnabled;

//Barcode options
- (struct BarcodeOptionSettings) GetCurrentBarcodeOptionSettings DEPRECATED;
- (BOOL) SetCurrentBarcodeOptionSettings:(struct BarcodeOptionSettings) settings DEPRECATED;
- (struct BarcodeOptionSettings) GetBarcodeOption;
- (BOOL) SetBarcodeOption:(struct BarcodeOptionSettings) settings;

- (BOOL) EnableDisableCODABAR_TX_STARTSTOP:(enum EnableDisable)enabled;
- (enum EnableDisable) IsCODABAR_TX_STARTSTOPEnabled;
- (BOOL) EnableDisableLASER_UPCE_AS_UPCA:(enum EnableDisable)enabled;
- (enum EnableDisable) IsLASER_UPCE_AS_UPCAEnabled;

- (BOOL) EnableDisablePOSTNET_TX_CHECKDIGIT:(enum EnableDisable)enabled;
- (enum EnableDisable) IsPOSTNET_TX_CHECKDIGITEnabled;
- (BOOL) EnableDisablePLANET_TX_CHECKDIGIT:(enum EnableDisable)enabled;
- (enum EnableDisable) IsPLANET_TX_CHECKDIGITEnabled;
- (BOOL) EnableDisableMSI_TX_CHECKDIGIT:(enum EnableDisable)enabled;
- (enum EnableDisable) IsMSI_TX_CHECKDIGITEnabled;
- (BOOL) EnableDisableCODE128_CONCATENATE:(enum EnableDisable)enabled;
- (enum EnableDisable) IsCODE128_CONCATENATEEnabled;
- (BOOL) EnableDisableCODE39_TX_STARTSTOP:(enum EnableDisable)enabled;
- (enum EnableDisable) IsCODE39_TX_STARTSTOPEnabled;
- (BOOL) EnableDisableCODE39_APPEND:(enum EnableDisable)enabled;
- (enum EnableDisable) IsCODE39_APPENDEnabled;
- (BOOL) EnableDisableCODE39_FULLASCII:(enum EnableDisable)enabled;
- (enum EnableDisable) IsCODE39_FULLASCIIEnabled;
- (BOOL) EnableDisableUPCA_CHECKDIGIT:(enum EnableDisable)enabled;
- (enum EnableDisable) IsUPCA_CHECKDIGITEnabled;
- (BOOL) EnableDisableUPCA_NUMBERSYS:(enum EnableDisable)enabled;
- (enum EnableDisable) IsUPCA_NUMBERSYSEnabled;
- (BOOL) EnableDisableUPCA_2DIGIT_ADDENDA:(enum EnableDisable)enabled;
- (enum EnableDisable) IsUPCA_2DIGIT_ADDENDAEnabled;
- (BOOL) EnableDisableUPCA_5DIGIT_ADDENDA:(enum EnableDisable)enabled;
- (enum EnableDisable) IsUPCA_5DIGIT_ADDENDAEnabled;
- (BOOL) EnableDisableUPCA_REQ_ADDENDA:(enum EnableDisable)enabled;
- (enum EnableDisable) IsUPCA_REQ_ADDENDAEnabled;
- (BOOL) EnableDisableUPCA_SEP_ADDENDA:(enum EnableDisable)enabled;
- (enum EnableDisable) IsUPCA_SEP_ADDENDAEnabled;
- (BOOL) EnableDisableUPCA_COUPONCODE:(enum EnableDisable)enabled;
- (enum EnableDisable) IsUPCA_COUPONCODEEnabled;
- (BOOL) EnableDisableUPCE_EXPAND:(enum EnableDisable)enabled;
- (enum EnableDisable) IsUPCE_EXPANDEnabled;
- (BOOL) EnableDisableUPCE_REQADDENDA:(enum EnableDisable)enabled;
- (enum EnableDisable) IsUPCE_REQADDENDAEnabled;
- (BOOL) EnableDisableUPCE_ADDENDASEP:(enum EnableDisable)enabled;
- (enum EnableDisable) IsUPCE_ADDENDASEPEnabled;
- (BOOL) EnableDisableUPCE_CHECKDIGIT:(enum EnableDisable)enabled;
- (enum EnableDisable) IsUPCE_CHECKDIGITEnabled;
- (BOOL) EnableDisableUPCE_NUMBERSYS:(enum EnableDisable)enabled;
- (enum EnableDisable) IsUPCE_NUMBERSYSEnabled;
- (BOOL) EnableDisableUPCE_2DIGIT_ADDENDA:(enum EnableDisable)enabled;
- (enum EnableDisable) IsUPCE_2DIGIT_ADDENDAEnabled;
- (BOOL) EnableDisableUPCE_5DIGIT_ADDENDA:(enum EnableDisable)enabled;
- (enum EnableDisable) IsUPCE_5DIGIT_ADDENDAEnabled;
- (BOOL) EnableDisableEAN13_CHECKDIGIT:(enum EnableDisable)enabled;
- (enum EnableDisable) IsEAN13_CHECKDIGITEnabled;
- (BOOL) EnableDisableEAN13_2DIGIT_ADDENDA:(enum EnableDisable)enabled;
- (enum EnableDisable) IsEAN13_2DIGIT_ADDENDAEnabled;
- (BOOL) EnableDisableEAN13_5DIGIT_ADDENDA:(enum EnableDisable)enabled;
- (enum EnableDisable) IsEAN13_5DIGIT_ADDENDAEnabled;
- (BOOL) EnableDisableEAN13_REQADDENDA:(enum EnableDisable)enabled;
- (enum EnableDisable) IsEAN13_REQADDENDAEnabled;
- (BOOL) EnableDisableEAN13_SEPADDENDA:(enum EnableDisable)enabled;
- (enum EnableDisable) IsEAN13_SEPADDENDAEnabled;
- (BOOL) EnableDisableEAN13_ISBNTRANS:(enum EnableDisable)enabled;
- (enum EnableDisable) IsEAN13_ISBNTRANSEnabled;
- (BOOL) EnableDisableEAN8_CHECKDIGIT:(enum EnableDisable)enabled;
- (enum EnableDisable) IsEAN8_CHECKDIGITEnabled;
- (BOOL) EnableDisableEAN8_2DIGIT_ADDENDA:(enum EnableDisable)enabled;
- (enum EnableDisable) IsEAN8_2DIGIT_ADDENDAEnabled;
- (BOOL) EnableDisableEAN8_5DIGIT_ADDENDA:(enum EnableDisable)enabled;
- (enum EnableDisable) IsEAN8_5DIGIT_ADDENDAEnabled;
- (BOOL) EnableDisableEAN8_REQADDENDA:(enum EnableDisable)enabled;
- (enum EnableDisable) IsEAN8_REQADDENDAEnabled;
- (BOOL) EnableDisableEAN8_SEPADDENDA:(enum EnableDisable)enabled;
- (enum EnableDisable) IsEAN8_SEPADDENDAEnabled;
- (BOOL) EnableDisableGS1128_UPCEAN_VERSION:(enum EnableDisable)enabled;
- (enum EnableDisable) IsGS1128_UPCEAN_VERSIONEnabled;
//Codabar
- (BOOL) EnableCODABAR_DONOT_VERIFY;
- (enum EnableDisable) IsCODABAR_DONOT_VERIFYEnabled;
- (BOOL) EnableCODABAR_VERIFY_DONOT_TX;
- (enum EnableDisable) IsCODABAR_VERIFY_DONOT_TXEnabled;
- (BOOL) EnableCODABAR_VERIFY_DO_TX;
- (enum EnableDisable) IsCODABAR_VERIFY_DO_TXEnabled;
- (BOOL) EnableCODABAR_CONCATENATE_DISABLE;
- (enum EnableDisable) IsCODABAR_CONCATENATE_DISABLEEnabled;
- (BOOL) EnableCODABAR_CONCATENATE_ENABLE;
- (enum EnableDisable) IsCODABAR_CONCATENATE_ENABLEEnabled;
- (BOOL) EnableCODABAR_CONCATENATE_REQUIRED;
- (enum EnableDisable) IsCODABAR_CONCATENATE_REQUIREDEnabled;
//Code39
- (BOOL) EnableCODE39_DONOT_VERIFY;
- (enum EnableDisable) IsCODE39_DONOT_VERIFYEnabled;
- (BOOL) EnableCODE39_VERIFY_DONOT_TX;
- (enum EnableDisable) IsCODE39_VERIFY_DONOT_TXEnabled;
- (BOOL) EnableCODE39_VERIFY_DO_TX;
- (enum EnableDisable) IsCODE39_VERIFY_DO_TXEnabled;
//I2of5
- (BOOL) EnableI2OF5_DONOT_VERIFY;
- (enum EnableDisable) IsI2OF5_DONOT_VERIFYEnabled;
- (BOOL) EnableI2OF5_VERIFY_DONOT_TX;
- (enum EnableDisable) IsI2OF5_VERIFY_DONOT_TXEnabled;
- (BOOL) EnableI2OF5_VERIFY_DO_TX;
- (enum EnableDisable) IsI2OF5_VERIFY_DO_TXEnabled;
//CODE11
- (BOOL) EnableDisableCODE11_2_CHECKDIGIT:(enum EnableDisable)enabled;
- (enum EnableDisable) IsCODE11_2_CHECKDIGITEnabled;
//TELEPEN
- (BOOL) EnableDisableTELEPEN_OUTPUT_ORIGINAL:(enum EnableDisable)enabled;
- (enum EnableDisable) IsTELEPEN_OUTPUT_ORIGINALEnabled;
//Posicode
- (BOOL) EnablePOSICODE_AB_MASK;
- (enum EnableDisable) IsPOSICODE_AB_MASKEnabled;
- (BOOL) EnablePOSICODE_AB_LIMITEDA_MASK;
- (enum EnableDisable) IsPOSICODE_AB_LIMITEDA_MASKEnabled;
- (BOOL) EnablePOSICODE_AB_LIMITEDB_MASK;
- (enum EnableDisable) IsPOSICODE_AB_LIMITEDB_MASKEnabled;
//GS1-128
- (BOOL) EnableGS1128_NO_EMULATION;
- (enum EnableDisable) IsGS1128_NO_EMULATIONEnabled;
- (BOOL) EnableGS1128_128_EMULATION;
- (enum EnableDisable) IsGS1128_128_EMULATIONEnabled;
- (BOOL) EnableGS1128_RSS_EMULATION;
- (enum EnableDisable) IsGS1128_RSS_EMULATIONEnabled;
//Laser
- (BOOL) EnableDisableLASER_EAN8_AS_EAN13:(enum EnableDisable)enabled;
- (enum EnableDisable) IsLASER_EAN8_AS_EAN13Enabled;
- (BOOL) EnableDisableLASER_UPCE_AS_EAN13:(enum EnableDisable)enabled;
- (enum EnableDisable) IsLASER_UPCE_AS_EAN13Enabled;
- (BOOL) EnableDisableLASER_RETURN_CHECK_DIGIT:(enum EnableDisable)enabled;
- (enum EnableDisable) IsLASER_RETURN_CHECK_DIGITEnabled;
- (BOOL) EnableDisableLASER_VERIFY_CHECK_DIGIT:(enum EnableDisable)enabled;
- (enum EnableDisable) IsLASER_VERIFY_CHECK_DIGITEnabled;
- (BOOL) EnableDisableLASER_WIDE_SCAN_ANGLE:(enum EnableDisable)enabled;
- (enum EnableDisable) IsLASER_WIDE_SCAN_ANGLEEnabled;
- (BOOL) EnableDisableLASER_HIGH_FILTER_MODE:(enum EnableDisable)enabled;
- (enum EnableDisable) IsLASER_HIGH_FILTER_MODEEnabled;
- (BOOL) EnableDisableLASER_UPCA_AS_EAN13:(enum EnableDisable)enabled;
- (enum EnableDisable) IsLASER_UPCA_AS_EAN13Enabled;
- (BOOL) EnableDisableLASER_I2OF5_VERIFY_CHECK_DIGIT:(enum EnableDisable)enabled;
- (enum EnableDisable) IsLASER_I2OF5_VERIFY_CHECK_DIGITEnabled;
- (BOOL) EnableDisableLASER_CODE39_VERIFY_CHECK_DIGIT:(enum EnableDisable)enabled;
- (enum EnableDisable) IsLASER_CODE39_VERIFY_CHECK_DIGITEnabled;
- (BOOL) EnableDisableLASER_UPCE_RETURN_CHECK_DIGIT:(enum EnableDisable)enabled;
- (enum EnableDisable) IsLASER_UPCE_RETURN_CHECK_DIGITEnabled;
- (BOOL) EnableDisableLASER_UPCA_RETURN_CHECK_DIGIT:(enum EnableDisable)enabled;
- (enum EnableDisable) IsLASER_UPCA_RETURN_CHECK_DIGITEnabled;
- (BOOL) EnableDisableLASER_EAN8_RETURN_CHECK_DIGIT:(enum EnableDisable)enabled;
- (enum EnableDisable) IsLASER_EAN8_RETURN_CHECK_DIGITEnabled;
- (BOOL) EnableDisableLASER_EAN13_RETURN_CHECK_DIGIT:(enum EnableDisable)enabled;
- (enum EnableDisable) IsLASER_EAN13_RETURN_CHECK_DIGITEnabled;

//Scan Option
- (BOOL) SetScanTimeout:(enum ScanTimeout)timeout;
- (enum ScanTimeout) GetScanTimeout;
- (BOOL) SetMinimumBarcodeLength:(int)minlength;
- (int) GetMinimumBarcodeLength;

- (BOOL) EnableAutoTriggerMode:(enum EnableDisable)setting DEPRECATED;
- (enum EnableDisable) IsAutoTriggerModeEnabled DEPRECATED;
- (BOOL) EnableAutoTrigger:(enum EnableDisable)setting;
- (enum EnableDisable) IsAutoTriggerEnabled;

- (BOOL) EnablePartialDisplayMenuEntry:(enum EnableDisable)setting;

- (BOOL) SetAutoTriggerRereadDelay:(enum AutoTriggerRereadDelay)delay;
- (int) GetAutoTriggerRereadDelay;

- (BOOL) SetPartialDisplayStartPosition:(int)position DEPRECATED;
- (BOOL) SetPartialDisplayLength:(int)length DEPRECATED;
- (BOOL) SetPartialDataDisplayStartPosition:(int)position;
- (BOOL) SetPartialDataDisplayLength:(int)length;

- (BOOL) SetPartialDisplayAction:(enum PartialAction)action;
- (enum PartialAction) GetPartialDisplayAction;

- (BOOL) SetSecurityLevel:(int)level;
- (int) GetSecurityLevel;

//Data process
- (BOOL) SetDataFormat:(int)format;
- (int) GetDataFormat;

- (BOOL) SetWedgeStoreMode:(enum WedgeMode)wedgemode DEPRECATED;
- (BOOL) SetDataProcessMode:(enum WedgeMode)wedgemode;

- (enum WedgeMode) GetWedgeStoreMode DEPRECATED;
- (enum WedgeMode) GetDataProcessMode;

- (char *) GetPrefix DEPRECATED;
- (char *) GetDataPrefix;

- (BOOL) SetPrefix:(char *)prefix DEPRECATED;
- (BOOL) SetDataPrefix:(char *)prefix;

- (char *) GetSuffix DEPRECATED;
- (char *) GetDataSuffix;

- (BOOL) SetSuffix:(char *)suffix DEPRECATED;
- (BOOL) SetDataSuffix:(char *)suffix;

- (BOOL) EnableAgeVerify:(enum EnableDisable) isEnable;
- (enum EnableDisable) IsAgeVerifyEnabled;

- (enum AIMID) GetAIMIDSetting;
- (BOOL) SetAIMIDSetting:(enum AIMID)aimid;
- (BOOL) SetPartialDataStartPosition:(int)position;
- (int) GetPartialDataStartPosition;
- (BOOL) SetPartialDataLength:(int)length;
- (int) GetPartialDataLength;
- (BOOL) SetPartialDataAction:(enum PartialAction)action;
- (enum PartialAction) GetPartialDataAction;

- (BOOL) FinishSynchronization;
- (BOOL) StartSynchronization;

- (BOOL) SetDataTerminator:(enum DataTerminator)terminator;
- (enum DataTerminator) GetDataTerminator;
- (BOOL) EnableDuplicateCheck:(enum EnableDisable)duplicateCheckOn;
- (enum EnableDisable) IsDuplicateCheckEnabled;
- (BOOL) EnableEnterKeyFunction:(enum EnableDisable)enterkey;
- (enum EnableDisable) IsEnterKeyFunctionEnabled;
- (BOOL) EnableExtendKeypad:(enum EnableDisable)extendkey;
- (enum EnableDisable) IsExtendKeypadEnabled;

//GPS config
- (BOOL) SetGPSPower:(enum EnableDisable)power DEPRECATED;
- (BOOL) SetGPSBypassMode:(enum EnableDisable)setting DEPRECATED;
- (BOOL) EnableGPSModulePower:(enum EnableDisable)power;
- (BOOL) EnableGPSBypassDataMode:(enum EnableDisable)setting;

- (BOOL) ResetGPSModule;
- (BOOL) GPSAcquireTest;

- (BOOL) SetGPSPowerSaveMode:(enum GPSPowerSaveMode)mode DEPRECATED;
- (enum GPSPowerSaveMode) GetGPSPowerSaveMode DEPRECATED;
- (BOOL) SetGPSPowerMode:(enum GPSPowerSaveMode)mode;
- (enum GPSPowerSaveMode) GetGPSPowerMode;

- (BOOL) SetGPSAutoPowerOffTimeout:(int)timeout;
- (int) GetGPSAutoPowerOffTimeout;

//System Config
- (BOOL) SetMemoryConfiguration:(enum MemoryConfiguration)memoryconfig;
- (enum MemoryConfiguration) GetMemoryConfiguration;

- (BOOL) EraseSystemMemory DEPRECATED;
- (BOOL) EraseMemory;

- (BOOL) EraseLastData;

- (int) GetStoredBarcodeNumber DEPRECATED;
- (int) GetNumberOfStoredBarcode;

- (int) GetMemoryLeft;

- (int) GetCurrentDBMemorySize;

- (int) GetBatteryCapacity DEPRECATED;
- (int) GetBatteryLevel;

- (NSString *) GetSystemRTC DEPRECATED;
- (NSString *) GetTime;

- (BOOL) SetSystemRTC:(struct DateTime)datetime DEPRECATED;
- (BOOL) SetTime:(struct DateTime)datetime;

- (BOOL) SyncSystemRTC DEPRECATED;
- (BOOL) SyncSystemTime;

- (BOOL) ResetSystemRTC DEPRECATED;
- (BOOL) ResetSystemTime;

- (BOOL) EnableAutoErase:(enum EnableDisable)setting;
- (enum EnableDisable) IsAutoEraseEnabled;

- (BOOL) EnableBluetoothAutoPowerOff:(enum EnableDisable)setting;
- (enum EnableDisable) IsBluetoothAutoPowerOffEnabled;

- (BOOL) EnableGPSAutoPowerOff:(enum EnableDisable)setting;

- (BOOL) EnableButtonLock:(enum EnableDisable)setting;
- (BOOL) EnableScanButtonLock:(enum EnableDisable)setting;

- (BOOL) EnableBeepSound:(enum EnableDisable)setting;
- (enum EnableDisable) IsBeepSoundEnabled;

- (BOOL) EnableBeepOnScan:(enum EnableDisable)setting;
- (enum EnableDisable) IsBeepOnScanEnabled;

- (BOOL) EnableVibrator:(enum EnableDisable)setting;
- (enum EnableDisable) IsVibratorEnabled;

- (BOOL) EnableMenuAutoExit:(enum EnableDisable)setting DEPRECATED;
- (BOOL) EnableAutoMenuExit:(enum EnableDisable)setting;
- (enum EnableDisable) IsAutoMenuExitEnabled;

- (BOOL) EnableMenuBarcode:(enum EnableDisable)setting DEPRECATED;
- (BOOL) EnableMenuBarcodeState:(enum EnableDisable)setting;
- (enum EnableDisable) IsMenuBarcodeEnabled DEPRECATED;
- (enum EnableDisable) IsMenuBarcodeStateEnabled;

- (BOOL) EnableScrolling:(enum EnableDisable)setting DEPRECATED;
- (BOOL) EnableDisplayScroll:(enum EnableDisable)setting;
- (enum EnableDisable) IsScrollingEnabled;

- (BOOL) SetSleepTimeout:(enum SleepTimeout)sleeptimeout;
- (enum SleepTimeout) GetSleepTimeout;

- (BOOL) SetDisplayFormat:(enum DisplayFormat)displayformat;
- (enum DisplayFormat) GetDisplayFormat;

- (BOOL) SetAutoPowerOffTimeout:(int)powerofftimeout DEPRECATED;
- (int) GetAutoPowerOffTimeout DEPRECATED;;

- (BOOL) EnableDisplayPortStatus:(enum EnableDisable)setting DEPRECATED;
- (BOOL) EnableDisplayConnectionStatus:(enum EnableDisable)setting;
- (enum EnableDisable) IsDisplayConnectionStatusEnabled;

- (BOOL) EnableHighBeepVolume:(enum EnableDisable)setting;
- (enum EnableDisable) IsHighBeepVolumeEnabled;

- (BOOL) SetFactoryDefault;
- (BOOL) SetMenuPassword:(NSString *)password;
- (BOOL) LockDateTimeSetting:(enum EnableDisable)setting;

- (enum EnableDisable) IsKeypadEnabled;
- (BOOL) EnableKeypad:(enum EnableDisable)setting;

- (BOOL) EnableScanIfConnected:(enum EnableDisable)setting;
- (enum EnableDisable) IsScanIfConnectedEnabled;

- (BOOL) EnablePowerOnBeep:(enum EnableDisable)setting;
- (enum EnableDisable) IsPowerOnBeepEnabled;

- (BOOL) EnableBeepOnConnect:(enum EnableDisable)setting;
- (enum EnableDisable) IsBeepOnConnectEnabled;

- (BOOL) EnableMFiMode:(enum EnableDisable)setting;
- (enum EnableDisable) IsMFiEnabled;

- (enum EnableDisable) IsMFiAuthChipInstalled;

- (BOOL) SetBrightnessLevel:(enum BrightnessLevel)level;
- (enum BrightnessLevel) GetBrightnessLevel;

- (BOOL) SetLanguage:(enum Language)language;
- (enum Language) GetLanguage;

- (int) GetExtendedBatteryLevel;

// Direct
- (KDCResponse *) SendKDCCommand:(NSData *)command timeout:(int)timeout isWakeup:(BOOL)isWakeup;

//Host message
- (BOOL) SetDisplayPosition:(int)row :(int)col;
- (BOOL) ClearDisplay;

- (BOOL) DisplayTextMessageOnKDC:(char *)text DEPRECATED;
- (BOOL) SetDisplayMessage:(char *)text;

- (BOOL) SetDisplayDuration:(int)seconds DEPRECATED;
- (BOOL) SetDisplayMessageDuration:(int)seconds;

- (BOOL) SetDisplayTextFontSize:(enum MessageFontSize)fontsize DEPRECATED;
- (BOOL) SetDisplayMessageFontSize:(enum MessageFontSize)fontsize;

- (BOOL) BeepSuccess DEPRECATED;
- (BOOL) SetSuccessAlertBeep;

- (BOOL) BeepFailure DEPRECATED;
- (BOOL) SetFailureAlertBeep;

- (BOOL) MakeCustomBeep:(int)ontime :(int)offtime :(int)repeat DEPRECATED;
- (BOOL) SetCustomBeepTone:(int)ontime :(int)offtime :(int)repeat;

- (BOOL) MakeCustomVibration:(int)ontime :(int)offtime :(int)repeat DEPRECATED;
- (BOOL) SetCustomVibration:(int)ontime :(int)offtime :(int)repeat;

- (BOOL) SetMessageTextAttribute:(enum MessageTextAttribute)attribute;
- (BOOL) SetLEDState:(enum LEDState)state;
- (BOOL) DisplayBitmap:(int)bitmapIndex;
- (BOOL) DisplayBitmapWithClearScreen:(int)bitmapIndex;

//Stored data
- (void) GetStoredDataSingle;

// MSR - Only applicable to KDC415, KDC425 and KDC430 models
- (BOOL) EnableMSRErrorBeep:(enum EnableDisable)isEnable;
- (enum EnableDisable) IsMSRErrorBeepEnabled;
- (NSString *) GetAESKey;
- (BOOL) SetAESKey:(NSString *)aesKey;
- (enum AESBitLengths) GetAESKeyLength;
- (BOOL) SetAESKeyLength:(enum AESBitLengths)keyLength;
- (enum MSRCardType) GetMSRCardType;
- (BOOL) SetMSRCardType:(enum MSRCardType)cardType;
- (enum MSRDataEncryption) GetMSRDataEncryption;
- (BOOL) SetMSRDataEncryption:(enum MSRDataEncryption)encryption;
- (enum MSRDataType) GetMSRDataType;
- (BOOL) SetMSRDataType:(enum MSRDataType)dataType;
- (int) GetMSRTrackSelection;
- (BOOL) SetMSRTrackSelection:(int)selection;
- (enum MSRTrackSeparator) GetMSRTrackSeparator;
- (BOOL) SetMSRTrackSeparator:(enum MSRTrackSeparator)separator;
- (enum PartialAction) GetPartialDataMSRAction;
- (BOOL) SetPartialDataMSRAction:(enum PartialAction)action;
- (int) GetPartialDataMSRLength;
- (BOOL) SetPartialDataMSRLength:(int)length;
- (int) GetPartialDataMSRStartPosition;
- (BOOL) SetPartialDataMSRStartPosition:(int)position;

- (BOOL) EnableMSRSentinel:(enum EnableDisable)isEnable;
- (enum EnableDisable) IsMSRSentinelEnabled;

- (BOOL) EnableDecryptMSRData:(enum EnableDisable)isEnable withKey:(NSString *)key length:(enum AESBitLengths)length;

// Wifi
- (BOOL) IsWiFiInstalled;
- (BOOL) EnableWiFiPower:(enum EnableDisable)isEnable;
- (BOOL) IsWiFiPowerEnabled;
- (BOOL) EnableWiFiAutoConnect:(enum EnableDisable)isEnable;
- (BOOL) IsWiFiAutoConnectEnabled;
- (BOOL) SetWiFiServerIPAddress:(NSString *)ipAddress;
- (NSString *) GetWiFiServerIPAddress;
- (BOOL) SetWiFiServerURLAddress:(NSString *)url;
- (NSString *) GetWiFiServerURLAddress;
- (BOOL) SetWiFiServerPortNumber:(int)portNumber;
- (int) GetWiFiServerPortNumber;
- (BOOL) SetWiFiProtocol:(enum WiFiProtocol)protocol;
- (enum WiFiProtocol) GetWiFiProtocol;
- (BOOL) EnableWiFiSSL:(enum EnableDisable)isEnable;
- (BOOL) IsWiFiSSLEnabled;
- (BOOL) SetWiFiServerPage:(NSString *)page;
- (NSString *) GetWiFiServerPage;
- (BOOL) SetWiFiCertification:(Byte *)data length:(int)length;
- (NSData *) GetWiFiCertification;
- (BOOL) SetWiFiApSSID:(NSString *)ssid;
- (NSString *) GetWiFiApSSID;
- (BOOL) SetWiFiApPasscode:(NSString *)passcode;
- (NSString *) GetWiFiApPasscode;

// HID
- (BOOL) SetHIDAutoLockTime:(enum HIDAutoLockTime)timeout;
- (enum HIDAutoLockTime) GetHIDAutoLockTime;
- (BOOL) SetHIDKeyboard:(enum HIDKeyboard)keyboard;
- (enum HIDKeyboard) GetHIDKeyboard;
- (BOOL) SetHIDInitialDelay:(enum HIDInitialDelay)delay;
- (enum HIDInitialDelay) GetHIDInitialDelay;
- (BOOL) SetHIDInterDelay:(enum HIDInterDelay)delay;
- (enum HIDInterDelay) GetHIDInterDelay;
- (BOOL) SetHIDControlCharacter:(enum HIDControlCharacter)character;
- (enum HIDControlCharacter) GetHIDControlCharacter;

// UHF
- (BOOL) IsUHFModuleAttached;
- (BOOL) EnableUHFPower:(enum EnableDisable)isEnable;
- (BOOL) IsUHFPowerEnabled;
- (BOOL) SetUHFPowerOnTime:(enum UHFPowerTime)time;
- (enum UHFPowerTime) GetUHFPowerOnTime;
- (BOOL) SetUHFPowerOffTime:(enum UHFPowerTime)time;
- (enum UHFPowerTime) GetUHFPowerOffTime;
- (BOOL) SetUHFDataFormat:(enum UHFDataFormat)format;
- (enum UHFDataFormat) GetUHFDataFormat;
- (BOOL) SetUHFPowerLevel:(enum UHFPowerLevel)level;
- (enum UHFPowerLevel) GetUHFPowerLevel;

- (BOOL) EnableUHFSingleRead; DEPRECATED
- (BOOL) IsUHFSingleReadEnabled; DEPRECATED
- (BOOL) EnableUHFMultipleRead; DEPRECATED
- (BOOL) IsUHFMultipleReadEnabled; DEPRECATED

- (BOOL) SetUHFReadMode:(enum UHFReadMode)mode;
- (enum UHFReadMode) GetUHFReadMode;

- (BOOL) SetUHFDataType:(enum UHFDataType)type;
- (enum UHFDataType) GetUHFDataType;

- (BOOL) IsUHFDuplicateCheckEnabled;
- (BOOL) EnableUHFDuplicateCheck:(enum EnableDisable)isEnable;
- (NSString *) GetUHFId;

- (BOOL) SetUHFReadingTimeout:(int)timeout;
- (int) GetUHFReadingTimeout;

- (BOOL) SetUHFReadTagMode:(enum UHFReadTagMode)mode;
- (enum UHFReadTagMode) GetUHFReadTagMode;

- (BOOL) IsUHFStopActiveEnabled;
- (BOOL) EnableUHFStopActiveRead:(enum EnableDisable)isEnable;

- (BOOL) SetUHFBeepCount:(int)count;
- (int) GetUHFBeepCount;

- (BOOL) IsUHFStoreDataEnabled;
- (BOOL) EnableUHFStoreData:(enum EnableDisable)isEnable;

- (BOOL) IsUHFChargingEnabled;
- (BOOL) EnableUHFCharging:(enum EnableDisable)isEnable;

- (BOOL) StartUHFUserReadWriteMode; DEPRECATED
- (BOOL) FinishUHFUserReadWriteMode; DEPRECATED

- (BOOL) EnableUHFBurstMode; DEPRECATED

- (BOOL) IsUHFBurstModeEnabled;
- (BOOL) EnableUHFBurstMode:(enum EnableDisable)isEnable;

- (BOOL) IsUHFKeyEventEnabled;
- (BOOL) EnableUHFKeyEventEnabled:(enum EnableDisable)isEnable;
            
- (void) CancelUHFReading;

- (NSArray *) GetUHFTagList:(int)timeout status:(struct UHFStatus *) status;
- (BOOL) SelectUHFTag:(NSData *)epc status:(struct UHFStatus *) status;
- (NSData *) ReadUHFTagMemory:(NSData *)pwd bank:(enum UHFMemoryBank)bank start:(int)start length:(int)length status:(struct UHFStatus *) status;
- (BOOL) WriteUHFTagMemory:(NSData *)pwd bank:(enum UHFMemoryBank)bank start:(int)start length:(int)length data:(NSData *)data status:(struct UHFStatus *) status;
- (BOOL) SetUHFTagLock: (NSData *)pwd mask:(int)mask status:(struct UHFStatus *) status;
- (BOOL) KillUHFTag:(NSData *)pwd status:(struct UHFStatus *) status;

- (BOOL) SetUHFRegion:(enum UHFRegion)region;
- (enum UHFRegion) GetUHFRegion;

- (BOOL) ExecuteUHFSmartHopping;
- (BOOL) EnableUHFSmartHopping:(enum EnableDisable)isEnable;
- (BOOL) IsUHFSmartHoppingEnabled;

- (BOOL) SetUHFUntraceable:(int)xpc epc:(int)epc tid:(int)tid user:(int)user range:(int)range pwd:(NSData *)pwd status:(struct UHFStatus *)status;

- (BOOL) SetUHFSelectParameter:(int)target action:(int)action memBank:(UHFMemoryBank)memBank pointer:(int)pointer length:(int)length mask:(NSData *)mask isTruncated:(BOOL)isTruncated status:(struct UHFStatus *)status;
- (UHFResult *) GetUHFSelectParameter:(struct UHFStatus *)status;

- (BOOL) SetUHFQueryParameter:(int)dr cycle:(int)cycle tRext:(int)tRext sel:(int)sel session:(int)session target:(int)target slotNum:(int)slotNum status:(struct UHFStatus *)status;
- (UHFResult *)GetUHFQueryParameter:(struct UHFStatus *)status;

- (NSData *) AuthenticateUHFTag:(int)senRep incRepLen:(int)incRepLen csi:(int)csi length:(int)length message:(NSData *)message status:(struct UHFStatus *)status;


// POS
- (KPOSData *) GetPOSData;

// Barcode/NFC
- (int) SoftwareTrigger_POS;
- (int) EnableNFC_POS;
- (int) DisableNFC_POS;

// MS/IC Transaction
- (int) EnableMSR_POS;
- (int) DisableMSR_POS;
- (int) EnableCardReader_POS:(short)target;
- (int) DisableCardReader_POS:(short)target;
- (int) SelectEMVApplication_POS:(short)aid;
- (int) InitiateEMVTransaction_POS:(short)pinblockFormat maxDigit:(short)maxDigit transType:(short)transType amount:(int)amount otherAmount:(int)otherAmount additionalOperation:(short)addtionalOperation;
- (int) SelectApplicationAndInitiateEMVTransaction_POS:(short)pinblockFormat maxDigit:(short)maxDigit aid:(short)aid transType:(short)transType amount:(int)amount otherAmount:(int)otherAmount additionalOperation:(short)addtionalOperation;
- (int) ReplyEMVTransaction_POS:(KPOSEMVTagList *)tagList;
- (int) EnterPIN_POS:(NSString *)pan pinblockFormat:(short)pinblockFormat maxDigit:(short)maxDigit timeout:(short)timeout;
- (int) CancelEnterPIN_POS;

// Configuration
- (KPOSResult *) GetDeviceStatus_POS;
- (int) SetBeepSound_POS:(short)keytoneVolume beepVolume:(short)beepVolume beepSoundFlag:(BOOL)beepSoundFlag enableBeepPowerOn:(BOOL)enableBeepPowerOn enableBeepBarcode:(BOOL)enableBeepBarcode enableBeepConnection:(BOOL)enableBeepConnection;
- (KPOSResult *) GetBeepSound_POS;
- (int) SetKeypadMenuEntry_POS:(BOOL)enableKeypad;
- (int) SyncDateTime_POS;
- (KPOSResult *) GetDateTime_POS;
- (int) EnableBatteryAlarm_POS:(short)interval level:(short)level;
- (int) DisableBatteryAlarm_POS;

// Others
- (int) ReadData_POS:(short)titleID target:(short)target timeout:(short)timeout maxDigit:(short)maxDigit enableMask:(BOOL)enableMask;
- (int) CancelReadData_POS;
- (int) FindMyKDC_POS:(short)count;
- (int) SetDisplayMessage_POS:(NSString *)line1Message line2Message:(NSString *)line2Message line3Message:(NSString *)line3Message line4Message:(NSString *)line4Message timeout:(short)timeout;
- (int) ClearDisplay_POS;
- (int) EnableKeypad_POS:(short)line maxDigit:(short)maxDigit clearFlag:(BOOL)clearFlag enableMask:(BOOL)enableMask timeout:(short)timeout;
- (int) EnableKeypadEventOnly_POS;
- (int) DisableKeypad_POS:(BOOL)clearFlag;

// Payment Application Generator API
- (KPOSResult *) GetSupportedLocales_POS;
- (int) SetDisplayMessage_POS:(NSString *)line1Message line2Message:(NSString *)line2Message line3Message:(NSString *)line3Message line4Message:(NSString *)line4Message locale:(enum KPOSLocale)locale timeout:(short)timeout;
- (int) SetDisplayMessageAndReadKeypad_POS:(short)line1Id line2Id:(short)line2Id line3Id:(short)line3Id line4Id:(short)line4Id locale:(enum KPOSLocale)locale keypadType:(short)keypadType enableMask:(BOOL)enableMask keyInputLineNumber:(short)keyInputLineNumber keyInputAlign:(enum KPOSAlign)keyInputAlign maxDigit:(short)maxDigit timeout:(short)timeout;
- (int) SetDisplayMessageAndReadKeypad_POS:(NSString *)line1Message line2Message:(NSString *)line2Message line3Message:(NSString *)line3Message line4Message:(NSString *)line4Message locale:(enum KPOSLocale)locale keypadType:(short)keypadType enableMask:(BOOL)enableMask keyInputLineNumber:(short)keyInputLineNumber keyInputAlign:(enum KPOSAlign)keyInputAlign maxDigit:(short)maxDigit timeout:(short)timeout;
- (int) SetDisplayMessageAndSelectItem_POS:(NSString *)line1Message line2Message:(NSString *)line2Message line3Message:(NSString *)line3Message locale:(enum KPOSLocale)locale firstItemLineNumber:(short)firstItemLineNumber items:(NSArray *)items timeout:(short)timeout;
- (int) ClearDisplayAndCancelKeypad_POS;

// SDK Configuration
- (void) EnablePostNewDeviceArrived:(BOOL)enabled;
- (BOOL) SetConnectionMode:(enum ConnectionMode)mode;
- (enum ConnectionMode) GetConnectionMode;

// BLE
- (BOOL) StartScan;
- (void) StopScan;

@end

