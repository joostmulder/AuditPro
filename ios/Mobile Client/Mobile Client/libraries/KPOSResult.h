//
//  KPOSResult.h
//  KDCReader
//
//  Created by Jude on 2015. 4. 6..
//  Copyright (c) 2015년 AISolution. All rights reserved.
//

#ifndef KDCReader_KPOSResult_h
#define KDCReader_KPOSResult_h

#import "KPOSEMVTagList.h"

@interface KPOSResult : NSObject

- (id)initWithData:(Byte *)data length:(int)length;
- (void) SetResultCode:(short)resultCode;
- (short) GetResultCode;
- (short) GetCommandCode;
- (NSString *) GetSerialNumber;
- (NSString *) GetLoaderVersion;
- (NSString *) GetFirmwareVersion;
- (NSString *) GetApplicationVersion;
- (NSString *) GetBluetoothVersion;
- (short) GetBarcodeType;
- (short) GetBatteryStatus;
- (NSString *) GetBluetoothName;
- (BOOL) IsMSREnabled;
- (BOOL) IsNFCEnabled;
- (BOOL) IsKeypadMenuEntryEnabled;
- (short) GetKeyToneVolume;
- (short) GetBeepVolume;
- (BOOL) GetBeepSoundFlag;
- (BOOL) IsBeepOnPowerOnEvent;
- (BOOL) IsBeepOnBarcodeScanEvent;
- (BOOL) IsBeepOnConnectionEvent;
- (NSDateComponents *) GetDateTime;
- (Byte) GetSupportedLocales;

@end

#endif
