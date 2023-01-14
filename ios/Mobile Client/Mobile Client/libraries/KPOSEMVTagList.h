//
//  KPOSEMVTagList.h
//  KDCReader
//
//  Created by Jude on 2015. 6. 1..
//  Copyright (c) 2015년 AISolution. All rights reserved.
//

#ifndef KDCReader_KPOSEMVTagList_h
#define KDCReader_KPOSEMVTagList_h

@interface KPOSEMVTagList : NSObject

- (id)initWithTlvs:(Byte [])tlvs length:(short)length;
- (Byte *)GetTLVs;
- (short)GetLength;

@end

#endif
