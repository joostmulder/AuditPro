//
//  KDCConstants.h
//  KDCReader
//
//  Created by KoamTac on 10/18/14.
//  Copyright (c) 2014 AISolution. All rights reserved.
//

#ifndef KDCReader_KDCConstants_h
#define KDCReader_KDCConstants_h

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSInteger, EnableDisable)
{
    DISABLE = 0,
    ENABLE
};

typedef NS_ENUM(NSInteger, KDCMode)
{
    NORMAL = 0,
    APPLICATION
};

typedef NS_ENUM(NSInteger, DataDelimiter)
{
    DATA_NONE = 0,
    DATA_TAB,
    DATA_SPACE,
    DATA_COMMA,
    DATA_SEMICOLON
};

typedef NS_ENUM(NSInteger, RecordDelimiter)
{
    RECORD_NONE = 0,
    RECORD_LF,
    RECORD_CR,
    RECORD_TAB,
    RECORD_CRnLF
};

typedef NS_ENUM(NSInteger, NFCDataFormat)
{
    NFC_PACKET_FORMAT = 0,
    NFC_DATA_ONLY
};

//typedef NS_ENUM(NSInteger, AESKeyLength)
//{
//    AES_KEY_128 = 0,
//    AES_KEY_192,
//    AES_KEY_256
//};

typedef NS_ENUM(NSInteger, WedgeMode)
{
    WEDGE_ONLY = 0,
    WEDGE_STORE,
    STORE_ONLY,
    STORE_IF_SENT,
    STORE_IF_NOT_SENT
};

typedef NS_ENUM(NSInteger, AIMID)
{
    AIMID_NONE = 0,
    AIMID_PREFIX,
    AIMID_SUFFIX,
    IN_PREFIX = AIMID_PREFIX,
    IN_SUFFIX = AIMID_SUFFIX
};

typedef NS_ENUM(NSInteger, DataTerminator)
{
    TERMINATOR_NONE = 0,
    TERMINATOR_CR,
    TERMINATOR_LF,
    TERMINATOR_CRnLF,
    TERMINATOR_TAB,
    RIHGT_ARROW,
    LEFT_ARROW,
    DOWN_ARROW,
    UP_ARROW
};

typedef NS_ENUM(NSInteger, PowerOnTime)
{
    POWERON_DISABLED = 0,
    POWERON_1_SECOND,
    POWERON_2_SECONDS,
    POWERON_3_SECONDS,
    POWERON_4_SECONDS,
    POWERON_5_SECONDS,
    POWERON_6_SECONDS,
    POWERON_7_SECONDS,
    POWERON_8_SECONDS,
    POWERON_9_SECONDS,
    POWERON_10_SECONDS
};

typedef NS_ENUM(NSInteger, SleepTimeout)
{
    SLEEP_TIMEOUT_DISABLED = 0,
    SLEEP_TIMEOUT_1_SECOND = 1,
    SLEEP_TIMEOUT_2_SECONDS = 2,
    SLEEP_TIMEOUT_3_SECONDS = 3,
    SLEEP_TIMEOUT_4_SECONDS = 4 ,
    SLEEP_TIMEOUT_5_SECONDS = 5,
    SLEEP_TIMEOUT_10_SECONDS = 10,
    SLEEP_TIMEOUT_20_SECONDS = 20,
    SLEEP_TIMEOUT_30_SECONDS = 30,
    SLEEP_TIMEOUT_60_SECONDS = 60,
    SLEEP_TIMEOUT_120_SECONDS = 120,
    SLEEP_TIMEOUT_300_SECONDS = 300,
    SLEEP_TIMEOUT_600_SECONDS = 600
};

typedef NS_ENUM(NSInteger, DisplayFormat)
{
    TIME_BATTERY = 0,
    DISPLAY_FORMAT_TYPE_TIME,
    DISPLAY_FORMAT_TYPE_BATTERY,
    DISPLAY_FORMAT_MEMORY_STATUS,
    DISPLAY_FORMAT_GPS_DATA,
    DISPLAY_FORMAT_BARCODE_ONLY,
    DISPLAY_FORMAT_GRAPHIC
};

typedef NS_ENUM(NSInteger, AutoPowerOffTimeout)
{
    POWEROFF_1_MINUTE = 1,
    POWEROFF_2_MINUTES = 2,
    POWEROFF_3_MINUTES = 3,
    POWEROFF_4_MINUTES = 4,
    POWEROFF_5_MINUTES = 5,
    POWEROFF_6_MINUTES = 6,
    POWEROFF_7_MINUTES = 7,
    POWEROFF_8_MINUTES = 8,
    POWEROFF_9_MINUTES = 9,
    POWEROFF_10_MINUTES = 10,
    POWEROFF_11_MINUTES = 11,
    POWEROFF_12_MINUTES = 12,
    POWEROFF_13_MINUTES = 13,
    POWEROFF_14_MINUTES = 14,
    POWEROFF_15_MINUTES = 15,
    POWEROFF_16_MINUTES = 16,
    POWEROFF_17_MINUTES = 17,
    POWEROFF_18_MINUTES = 18,
    POWEROFF_19_MINUTES = 19,
    POWEROFF_20_MINUTES = 20,
    POWEROFF_21_MINUTES = 21,
    POWEROFF_22_MINUTES = 22,
    POWEROFF_23_MINUTES = 23,
    POWEROFF_24_MINUTES = 24,
    POWEROFF_25_MINUTES = 25,
    POWEROFF_26_MINUTES = 26,
    POWEROFF_27_MINUTES = 27,
    POWEROFF_28_MINUTES = 28,
    POWEROFF_29_MINUTES = 29,
    POWEROFF_30_MINUTES = 30
};

typedef NS_ENUM(NSInteger, DeviceProfile)
{
    PROFILE_SPP = 0,
    PROFILE_HID_IOS = 1,
    PROFILE_IPHONE = 2,
    PROFILE_SPP_2_0 = 3,
    PROFILE_HID_NORMAL = 4
};

struct DateTime
{
    uint8_t     Year;
    uint8_t     Month;
    uint8_t     Day;
    uint8_t     Hour;
    uint8_t     Minute;
    uint8_t     Second;
};

typedef NS_ENUM(NSInteger, MemoryConfiguration)
{
    MEMORY_0p5M_3p5M = 0,
    MEMORY_1M_3M,
    MEMORY_2M_2M,
    MEMORY_3M_1M,
    MEMORY_4M_0M
};

typedef NS_ENUM(NSInteger, GPSPowerSaveMode)
{
    GPS_NORMAL = 0,
    GPS_POWER_SAVE
};

struct BarcodeSymbolSettings
{
    uint32_t    FirstSymbols;
    uint32_t    SecondSymbols;
};

struct BarcodeOptionSettings
{
    uint32_t    FirstOptions;
    uint32_t    SecondOptions;
};

typedef NS_ENUM(NSInteger, ScanTimeout)
{
    SCANTIMEOUT_500_MS = 500,
    SCANTIMEOUT_1_SECOND = 1000,
    SCANTIMEOUT_2_SECONDS = 2000,
    SCANTIMEOUT_3_SECONDS = 3000,
    SCANTIMEOUT_4_SECONDS = 4000,
    SCANTIMEOUT_5_SECONDS = 5000,
    SCANTIMEOUT_6_SECONDS = 6000,
    SCANTIMEOUT_7_SECONDS = 7000,
    SCANTIMEOUT_8_SECONDS = 8000,
    SCANTIMEOUT_9_SECONDS = 9000,
    SCANTIMEOUT_10_SECONDS = 10000
};

typedef NS_ENUM(NSInteger, AutoTriggerRereadDelay)
{
    REREAD_CONTINUOUS = 0,
    REREAD_SHORT,
    REREAD_MEDIUM,
    REREAD_LONG,
    REREAD_EXTRA_LONG
};

typedef NS_ENUM(NSInteger, PartialAction)
{
    ERASE = 0,
    SELECT
};

typedef NS_ENUM(NSInteger, DataFormat)
{
    BARCODE_ONLY = 0,
    PACKET_DATA
};

typedef NS_ENUM(NSInteger, MessageFontSize)
{
    FONT_8x8 = 0,
    FONT_8x16,
    FONT_16x16,
    FONT_16x24,
    FONT_16x32,
    FONT_24x24,
    FONT_24x32,
    FONT_32x32
};

typedef NS_ENUM(NSInteger, MessageTextAttribute)
{
    NORMAL_TEXT = 0,
    REVERSE_TEXT
};

typedef NS_ENUM(NSInteger, LEDState)
{
    GREEN_LED_OFF = 0,
    GREEN_LED_ON,
    RED_LED_OFF,
    RED_LED_ON,
    BOTH_LED_OFF,
    BOTH_LED_ON
};

typedef NS_ENUM(NSInteger, DataType)
{
    UNKNOWN = 0,
    BARCODE,
    MSR,
    GPS,
    NFC_OLD, // internal only
    NFC_NEW, // internal only
    APPLICATION_DATA,
    KEY_EVENT,
    NFC,
    UHF_LIST
};

typedef NS_ENUM(NSInteger, NFCTag)
{
    NDEF_TYPE1 = 0,
    NDEF_TYPE2,
    RFID,
    CALYPSO,
    MIFARE_4K,
    TYPE_A,
    TYPE_B,
    FELICA,
    JEWEL,
    MIFARE_1K,
    MIFARE_UL_C,
    MIFARE_UL,
    MIFARE_DESFIRE,
    ISO15693
};

typedef NS_ENUM(NSInteger, AESBitLengths)
{
    AES_128_BITS = 0,
    AES_192_BITS,
    AES_256_BITS
};

typedef NS_ENUM(NSInteger, MSRCardType)
{
    MSR_CARD_ISO = 0,
    MSR_CARD_OTHER_1,
    MSR_CARD_AAMVA
};

typedef NS_ENUM(NSInteger, MSRDataType)
{
    MSR_DATA_PAYLOAD = 0,
    MSR_DATA_PACKET
};

typedef NS_ENUM(NSInteger, MSRDataEncryption)
{
    ENCRYPT_NONE = 0,
    ENCRYPT_AES
};

typedef NS_ENUM(NSInteger, MSRTrack)
{
    MSR_TRACK1 = 0x01,
    MSR_TRACK2 = 0x01 << 1,
    MSR_TRACK3 = 0x01 << 3
};

typedef NS_ENUM(NSInteger, MSRTrackSeparator)
{
    SEPARATOR_NONE,
    SEPARATOR_SPACE,
    SEPARATOR_COMMA,
    SEPARATOR_SEMICOLON,
    SEPARATOR_CR,
    SEPARATOR_LF,
    SEPARATOR_CRLF,
    SEPARATOR_TAB
};

typedef NS_ENUM(NSInteger, WiFiProtocol)
{
    UDP = 0,
    TCP,
    HTTP_GET,
    HTTP_POST
};

typedef NS_ENUM(NSInteger, HIDAutoLockTime)
{
    AUTO_LOCK_TIME_DISABLED = 0,
    AUTO_LOCK_TIME_1 = 1,
    AUTO_LOCK_TIME_2 = 2,
    AUTO_LOCK_TIME_3 = 3,
    AUTO_LOCK_TIME_4 = 4,
    AUTO_LOCK_TIME_5 = 5,
    AUTO_LOCK_TIME_10 = 10,
    AUTO_LOCK_TIME_15 = 15
};

typedef NS_ENUM(NSInteger, HIDKeyboard)
{
    KEYBOARD_ENGLISH = 0,
    KEYBOARD_GERMAN = 1,
    KEYBOARD_ITALIAN = 2,
    KEYBOARD_FRENCH = 3,
    KEYBOARD_SPANISH = 4
};

typedef NS_ENUM(NSInteger, HIDInitialDelay)
{
    INITIAL_DELAY_DISABLED = 0,
    INITIAL_DELAY_1 = 1,
    INITIAL_DELAY_2 = 2,
    INITIAL_DELAY_3 = 3,
    INITIAL_DELAY_5 = 5,
    INITIAL_DELAY_10 = 10
};

typedef NS_ENUM(NSInteger, HIDInterDelay)
{
    INTER_DELAY_DISABLED = 0,
    INTER_DELAY_10 = 10,
    INTER_DELAY_20 = 20,
    INTER_DELAY_30 = 30,
    INTER_DELAY_50 = 50,
    INTER_DELAY_100 = 100,
};

typedef NS_ENUM(NSInteger, HIDControlCharacter)
{
    CONTROL_DISABLE = 0,
    CONTROL_ALT_NUMPAD = 1,
    CONTROL_CNTL_CHAR = 2,
    CONTROL_REPLCAE_TO_PIPE = 3
};

typedef NS_ENUM(NSInteger, AppDataType)
{
    APP_DATA_UNKNOWN = 0,
    APP_DATA_COMPLIANT,
    APP_DATA_NONCOMPLIANT
};

typedef NS_ENUM(NSInteger, BrightnessLevel)
{
    BRIGHTNESS_LEVEL_1 = 1,
    BRIGHTNESS_LEVEL_2 = 2,
    BRIGHTNESS_LEVEL_3 = 3,
    BRIGHTNESS_LEVEL_4 = 4,
    BRIGHTNESS_LEVEL_5 = 5,
    BRIGHTNESS_LEVEL_6 = 6,
    BRIGHTNESS_LEVEL_7 = 7,
    BRIGHTNESS_LEVEL_8 = 8,
    BRIGHTNESS_LEVEL_9 = 9,
    BRIGHTNESS_LEVEL_10 = 10,
    BRIGHTNESS_LEVEL_11 = 11,
    BRIGHTNESS_LEVEL_12 = 12,
    BRIGHTNESS_LEVEL_13 = 13,
    BRIGHTNESS_LEVEL_14 = 14,
    BRIGHTNESS_LEVEL_15 = 15
};

typedef NS_ENUM(NSInteger, Language)
{
    LANGUAGE_DISABLE = 0,
    LANGUAGE_ENGLISH = 1,
    LANGUAGE_FRENCH = 3,
    LANGUAGE_ITALIAN = 4,
    LANGUAGE_SPANISH = 5,
    LANGUAGE_KOREAN = 6,
    LANGUAGE_JAPANESE = 7
};

typedef NS_ENUM(NSInteger, UHFPowerTime)
{
    UHF_POWER_TIME_500MS = 0,
    UHF_POWER_TIME_1000MS,
    UHF_POWER_TIME_1500MS,
    UHF_POWER_TIME_2000MS,
    UHF_POWER_TIME_2500MS,
    UHF_POWER_TIME_3000MS,
    UHF_POWER_TIME_3500MS,
    UHF_POWER_TIME_4000MS,
    UHF_POWER_TIME_4500MS,
    UHF_POWER_TIME_5000MS
};

typedef NS_ENUM(NSInteger, UHFDataFormat)
{
    UHF_DATA_BINARY = 0,
    UHF_DATA_HEX_DECIMAL
};

typedef NS_ENUM(NSInteger, UHFPowerLevel)
{
    UHF_LEVEL0 = 0,
    UHF_LEVEL1,
    UHF_LEVEL2,
    UHF_LEVEL3,
    UHF_LEVEL4,
    UHF_LEVEL5,
    UHF_LEVEL6,
    UHF_LEVEL7,
    UHF_LEVEL8,
    UHF_LEVEL9,
    UHF_LEVEL10,
    UHF_LEVEL11,
    UHF_LEVEL12
};

typedef NS_ENUM(NSInteger, UHFDataType)
{
    UHF_DATA_TYPE_EPC = 0,
    UHF_DATA_TYPE_PC_EPC,
    UHF_DATA_TYPE_RSSI_EPC,
    UHF_DATA_TYPE_RSSI_PC_EPC
};

typedef NS_ENUM(NSInteger, UHFReadMode)
{
    UHF_READ_MODE_NFC_RFID = 0,
    UHF_READ_MODE_BARCODE
};

typedef NS_ENUM(NSInteger, UHFReadTagMode)
{
    UHF_READ_TAG_MODE_SINGLE = 0,
    UHF_READ_TAG_MODE_MULTIPLE,
    UHF_READ_TAG_MODE_ACTIVE
};

typedef NS_ENUM(NSInteger, UHFMemoryBank)
{
    UHF_MEMORY_BANK_RFU = 0, // Reserved Bank
    UHF_MEMORY_BANK_EPC,
    UHF_MEMORY_BANK_TID,
    UHF_MEMORY_BANK_USER
};

typedef NS_ENUM(NSInteger, UHFRegion)
{
    UHF_REGION_US = 1,
    UHF_REGION_KR = 2,
    UHF_REGION_JP = 3,
    UHF_REGION_EU = 4
};

struct UHFStatus
{
    enum UHFDataFormat format;
    short errorCode;
};

typedef NS_ENUM(NSInteger, ConnectionMode)
{
    CONNECTION_MODE_NONE = 0,
    CONNECTION_MODE_ACCESSORY,
    CONNECTION_MODE_BLUETOOTH_SMART
};

typedef NS_ENUM(NSInteger, DeviceListType)
{
	EXTERNAL_ACCESSORY_LIST,
    SCANNED_PERIPHERAL_LIST,
    CONNECTED_PERIPHERAL_LIST,
    KNOWN_PERIPHERAL_LIST
};

typedef NS_ENUM(NSInteger, ConnectionState)
{
    CONNECTION_STATE_NONE = 0,
    CONNECTION_STATE_LISTEN = 1,
    CONNTECTION_STATE_CONNECTING = 2,
    CONNECTION_STATE_CONNECTED = 3,
    CONNECTION_STATE_LOST = 4,
    CONNECTION_STATE_FAILED = 5,
    CONNECTION_STATE_INITIALIZING = 7,
    CONNECTION_STATE_INITIALIZING_FAILED = 8
};

static const int ERROR_SUCCESS = 0x0000;
static const int ERROR_CONNECT_FAILED = 0x0001;
static const int ERROR_DISCOVER_SERVICE = 0x0002;
static const int ERROR_DISCOVER_CHARACTERISTIC = 0x0003;
static const int ERROR_READ_VALUE = 0x0004;
static const int ERROR_WRITE_VALUE = 0x0005;
static const int ERROR_DISCONNECT_FAILED = 0x0006;

/*
 * UHF Error Code
 *
 * 0x00  : Success
 * 0x100 : Common Error(No Response, Command Not Supported...)
 *
 * 0.5W models (Phychips)
 * 0x01 - 0x0F : EPC G2v2 Error Message
 * 0x10 - 0x7F : Vendor Specific Error
 * 0x80 - 0x8F : Protocol Error
 * 0x90 - 0x9F : Modem Error
 * 0xA0 - 0xAF : Registry
 * 0xB0 - 0xBF : Peripheral
 * 0xC0 - 0xDF : Reserved
 * 0xE0 - 0xFF : Custom Error
 *
 * 1.0W models (RodinBell)
 * 0x5011 - 0x5057 : Error Code
 */
static const short UHF_SUCCESS = 0x00;
static const short UHF_COMMON_ERROR = 0x100; // Common or Not Response

/*
 * Phychips Error Code
 */
static const short UHF_NOT_SUPPORTED = 0x01;
static const short UHF_INSUFFICIENT_PRIVILEGES = 0x02;
static const short UHF_MEMORY_OVERRUN = 0x03;
static const short UHF_MEMORY_LOCKED = 0x04;
static const short UHF_CRYPTO_SUITE_ERROR = 0x05;
static const short UHF_COMMAND_NOT_ENCAPSULATED = 0x06;
static const short UHF_RESPONSE_BUFFER_OVERFLOW = 0x07;
static const short UHF_SECURITY_TIMEOUT = 0x08;
static const short UHF_INSUFFICIENT_POWER = 0x0B;
static const short UHF_NON_SPECIFIC_ERROR = 0x0F;

static const short UHF_SENSOR_SCHEDULING_CONFIG = 0x11;
static const short UHF_TAG_BUSY = 0x12;
static const short UHF_MEASUREMENT_TYPE_NOT_SUPPORTED = 0x13;

static const short UHF_NO_TAG_DETECTED = 0x80;
static const short UHF_HANDLE_ACQUSITION_FAILED = 0x81;
static const short UHF_ACCESS_PASSWORD_FAILED = 0x82;

static const short UHF_CRC_ERROR = 0x90;
static const short UHF_RX_TIMEOUT = 0x91;

static const short UHF_REGISTRY_UPDATE_FAILED = 0xA0;
static const short UHF_REGISTRY_ERASE_FAILED = 0xA1;
static const short UHF_REGISTRY_WRITE_FAILED = 0xA2;
static const short UHF_REGISTRY_NOT_EXIST = 0xA3;

static const short UHF_UART_FAILED = 0xB0;
static const short UHF_SPI_FAILED = 0xB1;
static const short UHF_I2C_FAILED = 0xB2;
static const short UHF_GPIO_FAILED = 0xB3;

static const short UHF_NOT_SUPPORTED_COMMAND = 0xE0;
static const short UHF_UNDEFINED_COMMAND = 0xE1;
static const short UHF_INVALID_PARAMETER = 0xE2;
static const short UHF_TOO_HIGH_PARAMETER = 0xE3;
static const short UHF_TOO_LOW_PARAMETER = 0xE4;
static const short UHF_AUTO_READ_OPERATION_FAILED = 0xE5;
static const short UHF_NOT_AUTO_READ_MODE = 0xE6;
static const short UHF_GET_LAST_RESPONSE_FAILED = 0xE7;
static const short UHF_CONTROL_TEST_FAILED = 0xE8;
static const short UHF_RESET_READER_FAILED = 0xE9;
static const short UHF_RFID_BLOCK_CONTROL_FAILED = 0xEA;
static const short UHF_AUTO_READ_IN_OPERATION = 0xEB;
static const short UHF_UNDEFINED_OTHER_ERROR = 0xF0;
static const short UHF_VERIFY_WRITE_OPERATION_FAILED = 0xF1;
static const short UHF_ABNORMAL_ANTENNA = 0xFC;
static const short UHF_NOT_TAG_SELECTED = 0xFE;
static const short UHF_NONE_ERROR = 0xFF;

/*
 * RodinBell Error Code
 */
static const short UHF_COMMAND_ERROR = 0x5011;

static const short UHF_MCU_RESET_ERROR = 0x5020;
static const short UHF_CW_ON_ERROR = 0x5021;
static const short UHF_ANTENNA_MISSING = 0x5022;
static const short UHF_WRITE_FLASH_ERROR = 0x5023;
static const short UHF_READ_FLASH_ERROR = 0x5024;
static const short UHF_SET_OUTPUT_POWER_ERROR = 0x5025;

static const short UHF_TAG_INVENTORY_ERROR = 0x5031;
static const short UHF_TAG_READ_ERROR = 0x5032;
static const short UHF_TAG_WRITE_ERROR = 0x5033;
static const short UHF_TAG_LOCK_ERROR = 0x5034;
static const short UHF_TAG_KILL_ERROR = 0x5035;
static const short UHF_NO_TAG_ERROR = 0x5036;
static const short UHF_INVENTORY_ACCESS_ERROR = 0x5037;
static const short UHF_BUFFER_IS_EMPTY = 0x5038;

static const short UHF_ACCESS_PASSWORD_ERROR = 0x5040;
static const short UHF_PARAMETER_ERROR = 0x5041;
static const short UHF_WORD_COUNT_TOO_LONG = 0x5042;
static const short UHF_MEMBANK_OUT_OF_RANGE = 0x5043;
static const short UHF_LOCK_REGION_OUT_OF_RANGE = 0x5044;
static const short UHF_LOCK_ACTION_OUT_OF_RANGE = 0x5045;
static const short UHF_ADDRESS_ERROR = 0x5046;
static const short UHF_ANTENNA_ID_OUT_OF_RANGE = 0x5047;
static const short UHF_OUTPUT_POWER_OUT_OF_RANGE = 0x5048;
static const short UHF_FREQUENCY_REGION_OUT_OF_RANGE = 0x5049;
static const short UHF_BAUDRATE_OUT_OF_RANGE = 0x504A;
static const short UHF_BEEPER_MODE_OUT_OF_RANGE = 0x504B;
static const short UHF_EPC_MATCH_LENGTH_TOO_LONG = 0x504C;
static const short UHF_EPC_MATCH_LENGTH_ERROR = 0x504D;
static const short UHF_EPC_MATCH_MODE_ERROR = 0x504E;
static const short UHF_FREQUENCY_RANGE_ERROR = 0x504F;
static const short UHF_GET_RN16_ERROR = 0x5050;
static const short UHF_DRM_MODE_ERROR = 0x5051;
static const short UHF_PLL_LOCK_ERROR = 0x5052;
static const short UHF_RF_CHIP_NO_RESPONSE = 0x5053;
static const short UHF_ACHIEVE_OUPUT_POWER_ERROR = 0x5054;
static const short UHF_FIRMWARE_AUTHENTICATION_ERROR = 0x5055;
static const short UHF_SPECTRUM_REGULATION_ERROR = 0x5056;
static const short UHF_OUTPUT_POWER_TOO_LOW = 0x5057;


/*
 * UHF Lock Mask & Action
 */
static const int UHF_KILL_PWD_MASK = 0x01 << 19;
static const int UHF_KILL_PWD_LOCK = 0x01 << 9;

static const int UHF_KILL_PWD_PERM_MASK = 0x01 << 18;
static const int UHF_KILL_PWD_PERM_LOCK = 0x01 << 8;

static const int UHF_ACCESS_PWD_MASK = 0x01 << 17;
static const int UHF_ACCESS_PWD_LOCK = 0x01 << 7;

static const int UHF_ACCESS_PWD_PERM_MASK = 0x01 << 16;
static const int UHF_ACCESS_PWD_PERM_LOCK = 0x01 << 6;

static const int UHF_EPC_MEMORY_MASK = 0x01 << 15;
static const int UHF_EPC_MEMORY_LOCK = 0x01 << 5;

static const int UHF_EPC_MEMORY_PERM_MASK = 0x01 << 14;
static const int UHF_EPC_MEMORY_PERM_LOCK = 0x01 << 4;

static const int UHF_TID_MEMORY_MASK = 0x01 << 13;
static const int UHF_TID_MEMORY_LOCK = 0x01 << 3;

static const int UHF_TID_MEMORY_PERM_MASK = 0x01 << 12;
static const int UHF_TID_MEMORY_PERM_LOCK = 0x01 << 2;

static const int UHF_USER_MEMORY_MASK = 0x01 << 11;
static const int UHF_USER_MEMORY_LOCK = 0x01 << 1;

static const int UHF_USER_MEMORY_PERM_MASK = 0x01 << 10;
static const int UHF_USER_MEMORY_PERM_LOCK = 0x01;


// Notification name
extern  NSString *kdcConnectionChangedNotification;
extern  NSString *kdcNewDeviceArrivedNotification;
extern  NSString *kdcDeviceLeaveNotification;

extern  NSString *kdcDataArrivedNotification;
extern  NSString *kdcBarcodeDataArrivedNotification;
extern  NSString *kdcMSRDataArrivedNotification;
extern  NSString *kdcGPSDataArrivedNotification;
extern  NSString *kdcNFCDataArrivedNotification;

extern  NSString *kdcErrorReceivedNotification;

extern  NSString *kdcInfoUpdatedNotification;
extern  NSString *kdcDeviceScannedNotification;

// user info key of connection state when kdcConnectionChangedNotification is received.
extern NSString *keyConnectionState;

// user info key for device when kdcConnectionChangedNotification, kdcNewDeviceArrivedNotification, kdcDeviceLeaveNotification is received
extern  NSString *keyAccessory;
extern  NSString *keyPeripheral;

// user info key of kdcInfoUpdatedNotification
extern  NSString *keyCBManagerState;

// user info key of kdcDeviceScannedNotification
extern  NSString *keyScannedKdcDevice;
extern  NSString *keyScannedPeripheral;

// user info key of kdcErrorReceivedNotification
extern  NSString *keyErrorCode;
extern  NSString *keyErrorObject;

// user info key of legacy kdc data - kdcDataArrivedNotification, kdcBarcodeDataArrivedNotification...
extern  NSString *keyKDCData;

// options key of getAvailableDeviceListEx
extern  NSString *keyIdentifiers;

#endif
