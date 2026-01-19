package org.example;

import org.junit.platform.console.ConsoleLauncher;

public class Runner {
    public static void main(String[] args) {
        ConsoleLauncher.main(new String[]{
                "-c", "org.example.purchase.CatalogTest"
        });
    }
}