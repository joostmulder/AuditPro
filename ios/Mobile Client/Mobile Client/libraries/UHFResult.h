//
//  UHFResult.h
//  KDCReader
//
//  Created by koamtac on 01/08/2018.
//  Copyright © 2018 AISolution. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "KDCConstants.h"

/*
 * Select Parameter class
 */
@interface SelectParameter : NSObject

@property int target;
@property int action;
@property enum UHFMemoryBank memBank;
@property int pointer;
@property int length;
@property (strong, nonatomic) NSData *mask;
@property BOOL isTruncated;

@end

/*
 * Query Parameter class
 */
@interface QueryParameter : NSObject

@property int dr;
@property int cycle;
@property int tRext;
@property int sel;
@property int session;
@property int target;
@property int slotNum;

@end

/*
 * UHF Result class
 */
@interface UHFResult : NSObject

/*! select parameter */
@property (strong, nonatomic) SelectParameter *selectParameter;

/*! query parameter */
@property (strong, nonatomic) QueryParameter *queryParameter;

@end
