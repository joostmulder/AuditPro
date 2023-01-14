//
//  KDCResponse.h
//  KDCReader
//
//  Created by koamtac on 14/02/2019.
//  Copyright © 2019 AISolution. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface KDCResponse : NSObject

/*
 * response data in binary format
 * response or null
 */
@property (strong, nonatomic) NSData *data;

/*
 * check whether a response was received successfully or not
 * true, if a response was received from KDC device, otherwise, false
 */
@property BOOL status;

@end
