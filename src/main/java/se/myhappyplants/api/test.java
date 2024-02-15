package se.myhappyplants.api;

import io.javalin.Javalin;

public class test {
    public static void main(String[] args) {
        var app = Javalin.create(/*config*/)
                .get("/", ctx -> ctx.result("Hello World"))
                .start(7070);
        String key = System.getenv("TREFLE_API_KEY");
        System.out.println(key);
    }
}