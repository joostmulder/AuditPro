//
//  KDCData.h
//  KDCReader
//
//  Created by KoamTac on 10/21/14.
//  Copyright (c) 2014 AISolution. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "KDCConstants.h"

@interface KDCData : NSObject

- (enum DataType)GetDataType;
- (enum NFCTag) GetNFCTagType;

- (NSString *)GetData;
- (Byte *)GetDataBytes;
- (int)GetDataBytesLength;

- (NSString *)GetNFCUID;
- (NSString *)GetNFCUIDReversed;
- (NSString *)GetNFCData;
- (Byte *)GetNFCDataBytes;
- (int)GetNFCDataBytesLength;
- (NSString *)GetNFCDataBase64;

- (NSArray *)GetUHFList;
- (enum UHFDataType)GetUHFListDataType;
- (NSArray *)GetUHFRssiList;

- (NSString *)GetBarcodeData;
- (Byte *)GetBarcodeDataBytes;
- (int)GetBarcodeDataBytesLength;

- (NSString *)GetMSRData;
- (Byte *)GetMSRDataBytes;
- (int)GetMSRDataBytesLength;

- (NSString *)GetGPSData;
- (NSString *)GetRecord;
- (NSDateComponents *)GetTimestamp;

- (NSString *)GetKeyEvent;

@end
