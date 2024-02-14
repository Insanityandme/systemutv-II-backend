package se.myhappyplants.api;

import io.javalin.Javalin;

public class test {
    public static void main(String[] args) {
        var app = Javalin.create(/*config*/)
                .get("/", ctx -> ctx.result("Hello World"))
                .start(7070);
    }
}