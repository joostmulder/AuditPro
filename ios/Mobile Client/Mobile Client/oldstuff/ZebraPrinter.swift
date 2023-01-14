//
//  ZebraPrinter.swift
//  Mobile Client
//
//  Created by Eric Ruck on 5/4/18.
//  Copyright 2018-2019 AuditPro. All rights reserved.
//

import Foundation
import ExternalAccessory

// TODO: EAAccessoryManager.registerForLocalNotifications to ensure the device list stays updated?

class ZebraPrinter : NSObject, StreamDelegate {
	private var outs: OutputStream? = nil
	private var inps: InputStream? = nil
	private var data: Data?

	private let test = "! U1 setvar \"device.languages\" \"zpl\"\r\n ^XA^LL100^CF0,15,10^FO0,25^FB250,100,0,C,0^FDHello World^FS^XZ"

	func testPrinter() {
		let device = findPrinter()
		dump(device?.serialNumber ?? "NOT FOUND")
		if (device == nil) {
			return
		}

		guard let session = EASession(accessory: device!, forProtocol: "com.zebra.rawport") else {
			NSLog("Failed to initiate session with printer")
			return
		}

		outs = session.outputStream
		if (outs == nil) {
			NSLog("Failed to open output stream to printer")
			return
		}

		data = test.data(using: .utf8)
		inps = session.inputStream
		outs!.delegate = self
		outs!.schedule(in: RunLoop.current, forMode: .default)
		outs!.open()
	}

	func stream(_ aStream: Stream, handle eventCode: Stream.Event) {
		switch eventCode {
			case .hasSpaceAvailable:
				if (data != nil) {
					data!.withUnsafeBytes { (bytes: UnsafeRawBufferPointer)->Void in
						let ptr = bytes.bindMemory(to: UInt8.self)
						let len = outs?.write(ptr.baseAddress!, maxLength: data!.count)
						dump(String(format: "Wrote %d of %d", len!, data!.count))
						// TODO Trim data by length
						data = nil
					}
				} else {
					// Wait for printing to complete
					Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true, block: {timer in
						let status = self.outs!.streamStatus
						if status != .writing {
							self.outs!.close()
							self.outs!.remove(from: RunLoop.current, forMode: .default)
							self.outs!.delegate = nil
							self.outs = nil
							self.inps!.close()
							self.inps = nil
							timer.invalidate()
						} else {
							NSLog("Still waiting on printer")
						}
					})
				}
/*
NOTE: After experimenting I couldn't find any way ensure the output buffer flushed before closing,
nor any useful ideas in the Googlesphere, so I think the only working possibility is keeping the
connection open for an arbitrary long time.

				outs!.close()
				outs!.remove(from: RunLoop.current, forMode: .defaultRunLoopMode)
				outs!.delegate = nil
				outs = nil
				Timer.scheduledTimer(withTimeInterval: 1, repeats: false, block: {timer in
					self.inps!.close()
					self.inps = nil
				})
*/
				break
			default:
				dump(eventCode)
		}
	}

	private func findPrinter() -> EAAccessory? {
		// Find the printer in our connected accessories
		let devices = EAAccessoryManager.shared().connectedAccessories
		for device in devices {
			if device.manufacturer.contains("Zebra") && device.modelNumber.contains("MZ32") {
				return device
			}
		}
		return nil
	}
}
