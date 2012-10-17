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
package org.openimaj.demos.sandbox.kestrel;

import org.openimaj.kestrel.SimpleKestrelClient;


/**
 * Tests for the {@link SimpleKestrelClient}. Not sure how to integrate these without a running 
 * kestrel server on jenkins..
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SimpleKestrelClientTest {
//    private SimpleKestrelClient client;
//
//    @Before
//    public void before() throws Exception {
//        try {
//            client = new SimpleKestrelClient("127.0.0.1", 22133);
//            client.delete("hoge");
//        } catch (Exception e) {
//            System.err.println("Run kestrel server before test!");
//            throw e;
//        }
//    }
//
//    @After
//    public void after() {
//        client.delete("hoge");
//        client.close();
//    }
//
//    @Test
//    public void set_peek_get() throws Exception {
//        client.set("hoge", "hoge\r\nhoge");
//
//        assertThat(client.peek("hoge"), is("hoge\r\nhoge"));
//        assertThat(client.get("hoge"), is("hoge\r\nhoge"));
//        assertThat(client.get("hoge"), is(nullValue()));
//    }
//
//    @Test
//    public void timeout_set_peek_get() throws Exception {
//        new Thread() {
//            @Override
//            public void run() {
//                try {
//                    SimpleKestrelClient client = new SimpleKestrelClient(
//                            "127.0.0.1", 22133);
//                    Thread.sleep(1000);
//                    client.set("hoge", "hogefuga");
//                    Thread.sleep(1000);
//                    client.set("hoge", "hogemoge");
//                    client.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//
//        assertThat(client.peek("hoge", 5000), is("hogefuga"));
//        assertThat(client.get("hoge"), is("hogefuga"));
//        assertThat(client.get("hoge"), is(nullValue()));
//        assertThat(client.get("hoge", 5000), is("hogemoge"));
//    }
//
//    @Test
//    public void set_delete() throws Exception {
//        client.set("hoge", "hogehoge");
//
//        assertThat(client.peek("hoge"), is("hogehoge"));
//
//        client.delete("hoge");
//        assertThat(client.get("hoge"), is(nullValue()));
//    }
//
//    @Test
//    public void many_set() throws Exception {
//        for (int i = 0; i < 40000; ++i) {
//            client.set("hoge", "hogehoge");
//        }
//        client.delete("hoge");
//    }
//
//    @Test
//    public void many_get() throws Exception {
//        client.set("hoge", "hogehoge");
//        for (int i = 0; i < 40000; ++i) {
//            client.peek("hoge");
//        }
//        client.delete("hoge");
//    }
//
//    @Test
//    public void ends_with_crlf() throws Exception {
//        client.set("hoge", "\r\n");
//
//        assertThat(client.peek("hoge"), is("\r\n"));
//        assertThat(client.get("hoge"), is("\r\n"));
//        assertThat(client.get("hoge"), is(nullValue()));
//
//        client.delete("hoge");
//        assertThat(client.get("hoge"), is(nullValue()));
//    }
//
//    @Test
//    public void ends_with_empty() throws Exception {
//        client.delete("hoge");
//        client.set("hoge", "");
//        assertThat(client.peek("hoge"), is(""));
//        assertThat(client.get("hoge"), is(""));
//        assertThat(client.get("hoge"), is(nullValue()));
//    }
//
//    @Test
//    public void test_delete() throws Exception {
//        client.delete("moge");
//        client.set("moge", "");
//        assertThat(client.peek("moge"), is(""));
//        client.delete("moge");
//        assertThat(client.peek("moge"), is(nullValue()));
//    }
//
//    @Test
//    public void test_timeout() throws Exception {
//        client.delete("moge");
//        assertThat(client.peek("moge", 35000), is(nullValue()));
//        Thread.sleep(35000);
//        assertThat(client.peek("moge"), is(nullValue()));
//    }
}
