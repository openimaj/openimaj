/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.workinprogress;

import java.io.IOException;

import org.openimaj.hardware.serial.SerialDevice;
import org.openimaj.stream.provider.twitter.TwitterStreamDataset;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.TwitterAPIToken;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.function.Operation;

import twitter4j.Status;

public class SignProgrammer {
	private static final byte CLEAR = (byte) 0x8C;
	private static final byte SPEED = (byte) 0x8D;
	public static byte LEFT = (byte) 0x81;
	public static byte RIGHT = (byte) 0x82;
	public static byte END = (byte) 0x80;
	public static byte FLASH = (byte) 0x88;
	public static byte START = (byte) 0xAA;
	public static byte ID = (byte) 0xBB;
	public static byte WAIT = (byte) 0x8F;
	public static byte JUMP = (byte) 0x85; // instant replace
	public static byte RANDOM = (byte) 0x8E;

	public static void main(String[] args) throws Exception {
		final SerialDevice dev = new SerialDevice("/dev/tty.usbserial-FTB3MMNA", 2400, 8, 1, 0);
		final byte[] init = {
				START, START, START, START,
				START, START, START, START,
				ID,
				LEFT };
		final byte[] end = { END };

		final byte[] cmd = ArrayUtils.concatenate(init, new byte[] { SPEED, 1, CLEAR },
				"Starting twitter display".getBytes(), end);
		writeMessage(dev, cmd);

		Thread.sleep(10000);

		final TwitterStreamDataset data = new
				TwitterStreamDataset(DefaultTokenFactory.get(TwitterAPIToken.class));
		data.forEach(new Operation<Status>() {
			@Override
			public void perform(Status object) {
				try {
					if (object.getIsoLanguageCode().equals("en")) {
						final String tweet = object.getText().replaceAll("[^\\x00-\\x7F]", "") + "               ";
						final byte[] message = tweet.getBytes("US-ASCII");
						final byte[] cmd = ArrayUtils.concatenate(init, new byte[] { SPEED, 1, CLEAR }, message, end);

						System.out.println("Sending command " + tweet);
						writeMessage(dev, cmd);
						System.out.println("Done");
						Thread.sleep(100 * message.length);
					}
				} catch (final Exception e) {
					System.err.println(e);
				}
			}
		});

		// dev.close();
	}

	private static void writeMessage(final SerialDevice dev, final byte[] cmd) throws IOException {
		for (int i = 0; i < cmd.length; i++) {
			if (cmd[i] == ' ') {
				cmd[i] = ':';
			} else if (cmd[i] == ':') {
				cmd[i] = ' ';
			} else if (cmd[i] == '_') {
				cmd[i] = '>';
			} else if (cmd[i] == '>') {
				cmd[i] = '_';
			}
		}

		dev.getOutputStream().write(cmd);
	}
}
