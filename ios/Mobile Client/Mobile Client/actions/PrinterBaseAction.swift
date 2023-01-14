//
//  PrinterBaseAction.swift
//  Mobile Client
//
//  Created by Eric Ruck on 7/18/18.
//  Copyright 2018-2019 AuditPro. All rights reserved.
//

import Foundation


/**
 * Provides common functionality for all printer actions.
 * @author Eric Ruck
 */
public class PrinterBaseAction : NSObject, StreamDelegate {
	private var _errorMessage: String? = nil
	private var data: Data? = nil
	private var offset: Int = 0
	private var printer: EAAccessory? = nil
	private var outs: OutputStream? = nil
	private var inps: InputStream? = nil
	private var completeSend: (() -> Void)? = nil
	private var completeXact: ((_ : String?) -> Bool)? = nil

	/**
	 * Override to provide more specific error messages and logging.
	 * @return Describes printer action
	 */
	var actionType: String? {
		return nil;
	}

	/**
	 * Indicates if we found a paired printer.
	 * @return Found printer flag
	 */
	var isPrinterFound: Bool {
		return printer != nil
	}

	/**
	 * Gets the most recent error message or nil if none.
	 * @return Error message string
	 */
	var errorMessage: String? {
		return _errorMessage
	}

	/**
	 * Sends a payload to the printer asynchronously.
	 * @param payload Message to send the printer
	 * @param complete Completion delegate
	 */
	func sendToPrinter(payload: String, _ complete: @escaping () -> Void) {
		self.completeSend = complete
		doPrinter(payload)
	}

	/**
	 * Transacts with the printer asynchronously.  The completion delegate receives
	 * data in its parameter from the input stream and returns true to end the transaction
	 * with the printer.
	 * @param payload Message to send the printer
	 * @param complete Completion delegate
	 */
	func transactWithPrinter(payload: String, _ complete: @escaping (_ : String?) -> Bool) {
		self.completeXact = complete
		doPrinter(payload)
	}

	/**
	 * Sends a payload to the printer asynchronously. 
	 */
	private func doPrinter(_ payload: String) {
		// Find the printer
		if !findPrinter() {
			self.completeError("Please make sure the printer is paired with your phone and turned on.")
			return
		}

		// Connect to the printer
		guard let session = EASession(accessory: self.printer!, forProtocol: "com.zebra.rawport") else {
			self.completeError("Unable to connect to the printer, make sure it is turned on and ready to operate.")
			return
		}


		// Open the printer streams
		guard
			let outs = session.outputStream,
			let inps = session.inputStream
		else {
			self.completeError("Unable to talk to the printer, verify that it is turned on and ready to operate.")
			return
		}

		// Convert the receipt to printer data
		_errorMessage = nil
		data = payload.data(using: .utf8)
		offset = 0

		// Reference the input stream
		self.inps = inps
		if (self.completeXact != nil || self.completeSend != nil) {
			// Open the input stream to receive the result
			inps.delegate = self
			inps.schedule(in: RunLoop.current, forMode: .default)
			inps.open()
		}

		// Open output stream for printing
		self.outs = outs
		outs.delegate = self
		outs.schedule(in: RunLoop.current, forMode: .default)
		outs.open()
	}

	/**
	 * Handles stream events.
	 * @param aStream Stream emitting event
	 * @param eventCode Which event
	 */
	public func stream(_ aStream: Stream, handle eventCode: Stream.Event) {
        
        switch eventCode {
            case .endEncountered:
                print("End Encountered")
                break
            case .errorOccurred:
                print("Error Occurred")
                break
            case .hasBytesAvailable:
                // Read from the input stream
                let bufferSize = 1024
                let buffer = UnsafeMutablePointer<UInt8>.allocate(capacity: bufferSize)
                let res = inps!.read(buffer, maxLength: bufferSize)
                if (res < 0) {
                    // Handle error
                    completeError("Unable to read the printer response")
                    disconnectPrinter()
                } else if (res == 0) {
                    // End of stream
                    if (self.completeXact?("") ?? true) {
                        self.completeXact = nil
                        disconnectPrinter()
                    }
                } else {
                    // Send the data to the completion delegate
                    var data = Data()
                    data.append(buffer, count: res)
                    if (self.completeXact?(String(data: data, encoding: .utf8)) ?? true) {
                        self.completeXact = nil
                        disconnectPrinter()
                    }
                }
                break
            case .hasSpaceAvailable:
                if (writeNextBlock()) {
                    // Wait for printing to complete
                    if (self.completeSend != nil) {
                        Timer.scheduledTimer(withTimeInterval: 2, repeats: false) { timer in
                            self.disconnectPrinter()
                        }
                    }
                }
                break
            default:
                print("No stream event")
                break
        }
        
//		if ((aStream == outs) && (eventCode == .hasSpaceAvailable)) {
//			if (writeNextBlock()) {
//				// Wait for printing to complete
//				if (self.completeSend != nil) {
//					Timer.scheduledTimer(withTimeInterval: 2, repeats: false) { timer in
//						self.disconnectPrinter()
//					}
//				}
//			}
//		}
//		if ((aStream == inps) && (eventCode == .hasBytesAvailable)) {
//			// Read from the input stream
//			let bufferSize = 1024
//        	let buffer = UnsafeMutablePointer<UInt8>.allocate(capacity: bufferSize)
//        	let res = inps!.read(buffer, maxLength: bufferSize)
//        	if (res < 0) {
//        		// Handle error
//        		completeError("Unable to read the printer response")
//        		disconnectPrinter()
//			} else if (res == 0) {
//        		// End of stream
//        		if (self.completeXact?("") ?? true) {
//        			self.completeXact = nil
//        			disconnectPrinter()
//				}
//			} else {
//				// Send the data to the completion delegate
//				var data = Data()
//				data.append(buffer, count: res)
//				if (self.completeXact?(String(data: data, encoding: .utf8)) ?? true) {
//					self.completeXact = nil
//					disconnectPrinter()
//				}
//			}
//		}
	}

	/**
	 * Writes the next section of data out to the printer.
	 * @return Completion flag
	 */
	private func writeNextBlock() -> Bool {
		// Is there a next block?
		if ((data == nil) || (offset >= data!.count)) {
			// No next block
			return true
		}

		// Get the next block
		let maxLength = min(1000, data!.count - offset)
		let endOffset = offset + maxLength
		data!.subdata(in: offset..<endOffset).withUnsafeBytes { (bytes: UnsafeRawBufferPointer)->Void in
			// Write out the raw bytes to the printer
			let ptr = bytes.bindMemory(to: UInt8.self)
			guard let buf = ptr.baseAddress, let outLength = outs?.write(buf, maxLength: maxLength) else {
				// Failed to write to the printer
				completeError("Failed to write %@ to the printer.")
				disconnectPrinter()
				return
			}

			// Prepare for the next block
			offset += outLength
		}

		// Not completed
		return false
	}

	/**
	 * Disconnects from the printer when we're done printing.
	 */
	private func disconnectPrinter() {
		// Close the connection to the printer
		outs?.close()
		outs?.remove(from: RunLoop.current, forMode: .default)
		outs?.delegate = nil
		inps?.close()

		// Invoke the completion delegates
		self.completeSend?()
		_ = self.completeXact?(nil)
		self.completeSend = nil
		self.completeXact = nil
	}

	/**
	 * Find the connected Zebra printer.
	 * @return Printer accessory instance or nil if not found
	 */
	private func findPrinter() -> Bool {
		// Find the printer in our connected accessories
		let devices = EAAccessoryManager.shared().connectedAccessories
		for device in devices {
			if device.manufacturer.contains("Zebra") && device.modelNumber.contains("MZ32") {
				printer = device
				return true
			}
            if device.manufacturer.contains("Zebra") && (device.modelNumber.contains("QLn320")) {
                printer = device
                return true
            }
            if device.manufacturer.contains("Zebra") && (device.modelNumber.contains("ZQ320")) {
                printer = device
                return true
            }
            if device.manufacturer.contains("Zebra") && (device.modelNumber.contains("ZQ51")) {
                printer = device
                return true
            }
            if device.manufacturer.contains("Zebra") && (device.modelNumber.contains("ZQ620")) {
                printer = device
                return true
            }
		}
		return false
	}

	/**
	 * Reports an error back to the completion delegate
	 */
	private func completeError(_ msg: String) {
		// Complete the error formatting
		if (msg.firstIndex(of: "@") != nil) {
			let useType = self.actionType == nil ? "" : String(format:"the %@", self.actionType!)
			_errorMessage = String(format: msg, useType)
		} else {
			_errorMessage = msg
		}

		// Invoke the completion delegates
		self.completeSend?()
		_ = self.completeXact?(nil)
		self.completeSend = nil
		self.completeXact = nil
	}
}
