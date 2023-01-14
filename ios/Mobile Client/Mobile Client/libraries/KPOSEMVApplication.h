//
//  KPOSEMVApplication.h
//  KDCReader
//
//  Created by Jude on 2015. 6. 1..
//  Copyright (c) 2015년 AISolution. All rights reserved.
//

#ifndef KDCReader_KPOSEMVApplication_h
#define KDCReader_KPOSEMVApplication_h

@interface KPOSEMVApplication : NSObject

- (id)initWithIndex:(short)index priority:(short)priority name:(NSString *)name;
- (short)GetIndex;
- (short)GetPriority;
- (NSString *)GetName;

@end

#endif
