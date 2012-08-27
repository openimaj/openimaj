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
