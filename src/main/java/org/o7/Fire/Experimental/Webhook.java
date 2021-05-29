package org.o7.Fire.Experimental;

import Atom.Utility.Random;
import Atom.Utility.Utility;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Webhook {
    public static URL realUrl;
    static Gson gson = new GsonBuilder().create();
    final static Webhook h = new Webhook();
    static String url = "https://discord.com/api/webhooks/848190640373235752/WMml7em9QCxYanPPsgr0Idt8sPCtRPJ7ZzwV9A3eO4ul7pFeib_zLWBIZCpl_6KV-FTL";
    static String assad = "curl -i -H \"Accept: application/json\" -H \"Content-Type:application/json\" -X POST --url " + url;
    static ExecutorService executorService = Executors.newCachedThreadPool();
    static {
        h.username = "o7Fire Compute Network";
        h.content = "h";
    }
    
    static {
        try {
            realUrl = new URL(url);
        }catch(MalformedURLException e){
            e.printStackTrace();
        }
    }
    
    public String content = "", username, avatar_url;
    
    public static void post(String dat) {
        h.content = dat.replaceAll("itzbenz", "runner");
        executorService.submit((Runnable) Webhook::post);
    }
    
    public static void post() {
        try {
            // build connection
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            // set request properties
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            // enable output and input
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            String doAnother = "";
            if (h.content.length() > 1920){
                doAnother = h.content.substring(1900);
                h.content = h.content.substring(0, 1900);
            }
            h.content = "```java\n" + h.content + "\n```";
            conn.getOutputStream().write(gson.toJson(h).getBytes(StandardCharsets.UTF_8));
            conn.getOutputStream().flush();
            conn.getOutputStream().close();
            conn.getInputStream().close();
            conn.disconnect();
            if (!doAnother.isEmpty()){
                h.content = doAnother;
                post();
            }
        }catch(Exception e){
            e.printStackTrace();
            System.err.println(gson.toJson(h));
        }
    }
    
    public static void main(String[] args) throws Throwable {
        
        
        post(Random.getString());
    }
    
    
    public static void hook() {
        long lastFlush = System.currentTimeMillis();
        final long[] nextFlush = {lastFlush + 4000};
        PrintStream out = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            StringBuilder sb = new StringBuilder();
            
            @Override
            public void write(int b) throws IOException {
                sb.append((char) b);
            }
            
            @Override
            public void flush() throws IOException {
                if (nextFlush[0] > System.currentTimeMillis()) return;
                out.print(sb.toString());
                Webhook.post(sb.toString());
                sb = new StringBuilder();
                nextFlush[0] = System.currentTimeMillis() + 4000;
            }
        }, true));
        System.out.println(Utility.getDate());
    }
}
