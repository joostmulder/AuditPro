//
//  KDCDevice.h
//  KDCReader
//
//  Created by koamtac on 05/01/2017.
//  Copyright © 2017 AISolution. All rights reserved.
//

#import <Foundation/Foundation.h>

/*! Unknown device */
extern NSString * const TYPE_UNKNOWN;

/*! Serial Port device */
extern NSString * const TYPE_SERIAL;

/*! External Accessory device */
extern NSString * const TYPE_ACCESSORY;

/*! Core Bluetooth device */
extern NSString * const TYPE_CORE_BLUETOOTH;


/*! Unknown */
extern NSString * const SUBTYPE_UNKNOWN;

// Subtype for Serial Port
/*! Serial Port based on Bluetooth SPP */
extern NSString * const SUBTYPE_SERIAL_SPP;

/*! Serial Port based on USB CDC */
extern NSString * const SUBTYPE_SERIAL_CDC;

// Subtype for External Accessory
/*! External Accessory */
extern NSString * const SUBTYPE_ACCESSORY;

// Subtype for Bluetooth
/*! Bluetooth Classic */
extern NSString * const SUBTYPE_BLUETOOTH_CLASSIC;

/*! Bluetooth LE Smart(CoreBluetooth) */
extern NSString * const SUBTYPE_BLUETOOTH_SMART;


@interface KDCDevice : NSObject

/*! KDC Device Type */
@property (nonatomic, readonly)NSString* type;

/*! KDC Device SubType */
@property (nonatomic, readonly)NSString* subtype;

/*! KDC Device display name */
@property (nonatomic, readonly)NSString* name;

/*! A object that provides information for a application to connect to remote KDC devices */
@property (nonatomic, readonly)id device;

@end
