//
//  KPOSData.h
//  KDCReader
//
//  Created by Jude on 2015. 4. 6..
//  Copyright (c) 2015년 AISolution. All rights reserved.
//

#ifndef KDCReader_KPOSData_h
#define KDCReader_KPOSData_h

#import "KPOSEMVApplication.h"
#import "KPOSEMVTagList.h"

@interface KPOSData : NSObject

- (id)initWithData:(Byte *)data length:(int)length;
- (BOOL) ParseData;
- (short) GetCommandCode;
- (short) GetEventCode;

- (Byte *) GetBarcodeBytes;
- (int) GetBarcodeLength;
- (NSString *) GetNFCUID;
- (NSString *) GetTrack1;
- (NSString *) GetTrack2;
- (NSString *) GetTrack3;

- (short) GetPOSEntryMode;

- (short) GetEncryptionSpec;
- (short) GetEncryptionType;
- (short) GetEncryptedDataSize;

- (short) GetUnencryptedTrack1Length;
- (short) GetUnencryptedTrack2Length;
- (short) GetUnencryptedTrack3Length;
- (short) GetUnencryptedPANLength;

- (short) GetEncryptedTrack1Length;
- (short) GetEncryptedTrack2Length;
- (short) GetEncryptedTrack3Length;
- (short) GetEncryptedPANLength;

- (Byte *) GetEncryptedTrack1Bytes;
- (Byte *) GetEncryptedTrack2Bytes;
- (Byte *) GetEncryptedTrack3Bytes;
- (Byte *) GetEncryptedPANBytes;

- (short) GetDigestType;
- (short) GetTrack1DigestLength;
- (short) GetTrack2DigestLength;
- (short) GetTrack3DigestLength;
- (short) GetPANDigestLength;

- (Byte *) GetTrack1DigestBytes;
- (Byte *) GetTrack2DigestBytes;
- (Byte *) GetTrack3DigestBytes;
- (Byte *) GetPANDigestBytes;

- (NSString *) GetCardDataKSN;
- (NSString *) GetDeviceSerialNumber;

- (BOOL) IsAutoAppSelection;
- (short) GetNumberOfAIDs;
- (NSMutableArray *) GetEMVApplicationList;
- (KPOSEMVTagList *) GetEMVTagList;
- (short) GetEMVResultCode;
- (short) GetEMVFallbackType;
- (short) GetErrorCode;

- (Byte *) GetPinBlockBytes;
- (short) GetPinBlockLength;
- (NSString *) GetPinBlockKSN;

- (NSString *) GetValueEntered;
- (char) GetPressedKey;
- (short) GetBatteryStatus;

- (short) GetSelectedItemIndex;

@end

#endif
