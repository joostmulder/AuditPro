//
//  KPOSstatic constants.h
//  KDCReader
//
//  Created by Jude on 2015. 4. 6..
//  Copyright (c) 2015년 AISolution. All rights reserved.
//

#ifndef KDCReader_KPOSConstants_h
#define KDCReader_KPOSConstants_h

// General Events
static const short EVT_TIMEOUT                             = 0x0101;
static const short EVT_TIMEOUT_CARD_READ                   = 0x0201;
static const short EVT_TIMEOUT_PIN_ENTRY                   = 0x0301;
static const short EVT_CANCELLED                           = 0x0102;
static const short EVT_CANCELLED_CARD_READ                 = 0x0202;
static const short EVT_CANCELLED_PIN_ENTRY                 = 0x0302;
static const short EVT_CARD_SWIPED                         = 0x0103;
static const short EVT_EMV_CARD_INSERTED                   = 0x0104;
static const short EVT_EMV_CARD_TAPPED                     = 0x0105;
static const short EVT_CARD_READ_FAILED                    = 0x0106;
static const short EVT_PINBLOCK_GENERATED                  = 0x0107;
static const short EVT_PINBLOCK_GENERATION_FAILED          = 0x0108;
static const short EVT_SIGNATURE_CAPTURED                  = 0x0109;
static const short EVT_SIGNATURE_CAPTURE_FAILED            = 0x010A;
static const short EVT_BARCODE_SCANNED                     = 0x010B;
static const short EVT_NFC_CARD_TAPPED                     = 0x010C;
static const short EVT_ITEM_SELECTED                       = 0x010D;
static const short EVT_VALUE_ENTERED                       = 0x010E;
static const short EVT_CONFIRMED                           = 0x010F;
static const short EVT_TIP_ENTERED                         = 0x0110;
static const short EVT_BARCODE_SCAN_FAILED                 = 0x0113;
static const short EVT_EMV_TRANSACTION_REQUESTED           = 0x0114;
static const short EVT_EMV_TRANSACTION_FAILED              = 0x0115;
static const short EVT_CARD_SWIPED_ENCRYPTED               = 0x0116;
static const short EVT_EMV_TRANSACTION_REVERSED            = 0x0119;
static const short EVT_EMV_TRANSACTION_CONFIRMED           = 0x011A;
static const short EVT_EMV_PAYPASS_MSG_SIGNAL              = 0x011B;
static const short EVT_EMV_PAYPASS_OUT_SIGNAL              = 0x011C;
static const short EVT_EMV_PAYWAVE_CLEARING_RECORDS        = 0x011D;
static const short EVT_EMV_CT_DATA_READ                    = 0x011E;
static const short EVT_EMV_CARD_REMOVED                    = 0x011F;
static const short EVT_IC_CARD_SWIPED                      = 0x0122;
static const short EVT_EMV_FALLBACK_TRIGGERED              = 0x0123;
static const short EVT_KEY_PRESSED                         = 0x0125;
static const short EVT_BATTERY_LOW                         = 0x0126;

// Customer Specific Events
static const short EVT_CARD_SWIPED_PINBLOCK_GENERATED_SSG  = 0x0124; // SSG
static const short EVT_IC_CASH_READ_COMPLETED              = 0x0127; // KOREAN IC Card
static const short EVT_IC_CASH_MULTI_ACCOUNT_READ          = 0x0128; // KOREAN IC Card


// Card Types
static const short CARD_TYPE_MAGNETIC                      = 1;
static const short CARD_TYPE_EMV_CONTACT                   = 2;
static const short CARD_TYPE_EMV_CONTACTLESS               = 4;

// Card Data Encryption Type
static const short CARD_DATA_ENCRYPTION_NONE               = 0;
static const short CARD_DATA_ENCRYPTION_TDES               = 1;
static const short CARD_DATA_ENCRYPTION_AES128             = 2;
static const short CARD_DATA_ENCRYPTION_AES196             = 3;
static const short CARD_DATA_ENCRYPTION_AES256             = 4;

// Read Data Target
static const short READ_DATA_TARGET_MSR 					= 1;
static const short READ_DATA_TARGET_BARCODE 				= 2;
static const short READ_DATA_TARGET_NFC		 			= 4;
static const short READ_DATA_TARGET_NUMERIC_KEYPAD         = 8;
static const short READ_DATA_TARGET_ALPHANUMERIC_KEYPAD	= 16;

// Result Codes
static const int RCODE_SUCCESS                             = 0x0000;
static const int RCODE_NO_RESPONSE                         = 0x0001;
static const int RCODE_NO_CONNECTED_DEVICE                 = 0x0002;
static const int RCODE_DEVICE_BUSY                         = 0x0003;
static const int RCODE_INVALID_RESPONSE                    = 0x0004;

static const int RCODE_CRC_ERROR                           = 0x0101;
static const int RCODE_INTERNAL_ERROR                      = 0x0102;
static const int RCODE_NOT_SUPPORT                         = 0x0103;
static const int RCODE_INSUFFICIENT_MEMORY                 = 0x0104;

static const int RCODE_INVALID_STATE                       = 0x0201;
static const int RCODE_INVALID_PARAMETER                   = 0x0202;
static const int RCODE_DEVICE_ERROR                        = 0x0203;
static const int RCODE_KEY_NOT_FOUND                       = 0x0204;
static const int RCODE_PINPAD_SKIMMER_DETECTED             = 0x0205;
static const int RCODE_ENCRYPTION_FAILED                   = 0x0206;
static const int RCODE_COMPROMISED                         = 0x0207;
static const int RCODE_NOT_VERIFIEDD                       = 0x0208; // SSG
static const int RCODE_NO_ENCRYPT_SEED                     = 0x0209; // SSG
static const int RCODE_RSA_INITIALIZE_FAILED               = 0x020A;
static const int RCODE_RSA_LOAD_FAILED                     = 0x020B;
static const int RCODE_RSA_ENCRYPTION_FAILED               = 0x020C;
static const int RCODE_AES_INITIALIZE_FAILED               = 0x020D;
static const int RCODE_DUKPT_LOAD_FAILED                   = 0x020E;
static const int RCODE_DIGEST_INITIALIZE_FAILED            = 0x020F;
static const int RCODE_DIGEST_PROCESS_FAILED               = 0x0210;
static const int RCODE_DIGEST_VALUE_MISMATCHED             = 0x0211;
static const int RCODE_KEY_ALLOCATION_FAILED               = 0x0212;

static const int RCODE_EMV_DECLINED                        = 0x0300;
static const int RCODE_EMV_READ_CARD_FAILED                = 0x0301;
static const int RCODE_EMV_BUILD_CANDIDATE_FAILED          = 0x0302;
static const int RCODE_EMV_TRY_NEXT_APPLICATION            = 0x0303;
static const int RCODE_EMV_CARD_ERROR                      = 0x0304;
static const int RCODE_EMV_CARD_BLOCKED                    = 0x0305;
static const int RCODE_EMV_TRANSACTION_NOT_ACCEPTED        = 0x0306;
static const int RCODE_EMV_PAN_MISMATCHED                  = 0x0307;
static const int RCODE_EMV_EXPIRY_DATE_MISMATCHED          = 0x0308;
static const int RCODE_EMV_TRANSACTION_NOT_VALID           = 0x0309;
static const int RCODE_EMV_CHECKSUM_ERROR                  = 0x030A;
static const int RCODE_EMV_TRANSACTION_REVERSAL            = 0x030B;
static const int RCODE_EMV_PROTOCOL_ERROR                  = 0x030C;
static const int RCODE_EMV_SELECT_APP_FAILED               = 0x030D;
static const int RCODE_EMV_POWER_ON_FAILED                 = 0x030E;
static const int RCODE_EMV_ABORT_TRANSACTION               = 0x030F;
static const int RCODE_EMV_PROCESSING_ERROR                = 0x0310;

static const int RCODE_EMV_CL_TRY_AGAIN                    = 0x0401;
static const int RCODE_EMV_CL_TRY_ANOTHER_INTERFACE        = 0x0402;
static const int RCODE_EMV_CL_TRY_ANOTHER_CARD             = 0x0403;

static const int RCODE_KICC_VAN_CODE_MISMATCHED            = 0x0501;
static const int RCODE_KICC_SEQ_NUM_FAILED                 = 0x0502;
static const int RCODE_KICC_DATA_LENGTH_FAILED             = 0x0503;
static const int RCODE_KICC_INVALID_PARAMETER              = 0x0504;
static const int RCODE_KICC_KEY_SLOT_FULL                  = 0x0505;
static const int RCODE_KICC_RSA_KEY_NOT_FOUND              = 0x0506;


// EMV Transaction Type
static const short EMV_TRANS_PURCHASE                      = 0x00;
static const short EMV_TRANS_PURCHASE_WITH_CASHBACK        = 0x09;
static const short EMV_TRANS_REFUND                        = 0x14;


// EMV Additional Operation
static const short EMV_ADDITIONAL_OP_NONE                  = 0x00;
static const short EMV_ADDITIONAL_OP_BYPASSPIN             = 0x01;
static const short EMV_ADDITIONAL_OP_FORCEDONLINE          = 0x02;


// EMV Fallback Type
static const short EMV_FALLBACK_IC_MALFUNCTION             = 0x01;
static const short EMV_FALLBACK_POWER_ON_FAIL              = 0x02;
static const short EMV_FALLBACK_BUILD_CANDIDATE_FAIL       = 0x03;
static const short EMV_FALLBACK_CHIP_DATA_READ_FAIL        = 0x04;
static const short EMV_FALLBACK_TRACK2_DATA_MISSING        = 0x05;


// EMV CL Online PIN Support
static const short EMV_CL_ONLINE_PIN_DISABLED              = 0x00;
static const short EMV_CL_ONLINE_PIN_ENABLED               = 0X01;
static const short EMV_CL_ONLINE_PIN_NOTUSED               = 0X02;


// EMV CL Track Data Activity
static const short EMV_CL_TRACK_DATA_ACTIVITY_TRACK1       = 1;
static const short EMV_CL_TRACK_DATA_ACTIVITY_TRACK2       = 2;


// POS Entry Mode
static const short POS_ENTRY_MODE_MS                       = 0x90; // MS
static const short POS_ENTRY_MODE_FALLABACK_MS             = 0x80; // Fallback MS


// Predefined Title ID
static const short TITLE_NONE                              = 0;
static const short TITLE_EMPLYOEE_CARD                     = 48;
static const short TITLE_CASH_RECEIPT_CARD                 = 49;
static const short TITLE_MEMBERSHIP_CARD                   = 50;
static const short TITLE_DISCOUNT_CARD                     = 51;
static const short TITLE_ENTER_PIN                         = 52; // available only for non-PCI mode
static const short TITLE_ENTER_CARD_PHONE                  = 53;
static const short TITLE_ENTER_PHONE                       = 54;
static const short TITLE_ENTER_7DIGIT_ID                   = 55;

// Key Tone
static const short KEYTONE_NONE                            = 0;
static const short KEYTONE_LOW                             = 1;
static const short KEYTONE_MEDIUM                          = 2;
static const short KEYTONE_HIGH                            = 3;

// Beep Volume
static const short BEEP_VOLUME_LOW                         = 1;
static const short BEEP_VOLUME_HIHG                        = 3;

// Encryption Digest Type
static const short DIGEST_SHA256                           = 1;

// PIN Block Format
static const short PINBLOCK_FORMAT0                        = 0;
static const short PINBLOCK_FORMAT1                        = 1;
static const short PINBLOCK_FORMAT2                        = 2;
static const short PINBLOCK_FORMAT3                        = 3;

// Encryption Spec
static const short ENCRYPTION_SPEC_NONE					= 0; // NO Encryption
static const short ENCRYPTION_SPEC_1						= 1; // KOAMTAC STANDARD
static const short ENCRYPTION_SPEC_2						= 2; // SSG

// Keypad Key Value
static const short KEYPAD_ENTER_KEY                        = 0x0D;
static const short KEYPAD_CLEAR_KEY                        = 0x08;
static const short KEYPAD_CANCEL_KEY                       = 0x18;

// Keypad Type
static const short KEYPAD_TYPE_NUMERIC = 0x00;
static const short KEYPAD_TYPE_ALPHA_NUMERIC = 0x01;

// Pre-defined Message String
static const short KT_MSG_NONE = 0;
static const short KT_MSG_ENTER = 1;
static const short KT_MSG_PLEASE_ENTER = 2;
static const short KT_MSG_COUPON_NO = 3;
static const short KT_MSG_EMAIL = 4;
static const short KT_MSG_GIFT_CARD_NO = 5;
static const short KT_MSG_MEMBERSHIP_NO = 6;
static const short KT_MSG_PREPAID_CARD_NO = 7;
static const short KT_MSG_TIP_AMOUNT = 8;
static const short KT_MSG_TIP_RATE = 9;
static const short KT_MSG_ZIP = 10;
static const short KT_MSG_ENTER_CARD_NO = 11;
static const short KT_MSG_ENTER_PHONE_NO = 12;
static const short KT_MSG_CARD_NO = 13;
static const short KT_MSG_PHONE_NO = 14;

// Locale <bit mask>
enum KPOSLocale {
    ENGLISH     = 0x01,
    FRENCH      = 0x01 << 1,
    GERMAN      = 0x01 << 2,
    ITALIAN     = 0x01 << 3,
    SPANISH     = 0x01 << 4,
    KOREAN      = 0x01 << 5,
    JAPANESE    = 0x01 << 6
};

// Alignment
enum KPOSAlign {
    LEFT    = 0x00,
    CENTER  = 0x01,
    RIGHT   = 0x02
};

extern  NSString *kposDataArrivedNotification; // KDC500
extern  NSString *keyKPOSData; // KDC500

#endif