package com.nettakrim.fake_afk;

import java.io.Closeable;
import java.util.Scanner;

public class PeekableScanner implements Closeable {
    public PeekableScanner(Scanner scanner) {
        this.scanner = scanner;
        this.current = null;
        this.peeked = false;
    }

    private final Scanner scanner;
    private String current;
    private boolean peeked;

    public String nextLine() {
        if (peeked) {
            peeked = false;
        } else {
            current = scanner.nextLine();
        }
        return current;
    }

    public String peek() {
        if (!peeked) {
            current = scanner.hasNextLine() ? scanner.nextLine() : "";
            peeked = true;
        }
        return current;
    }

    public boolean hasNextLine() {
        return scanner.hasNextLine() || peeked;
    }

    @Override public void close() {
        scanner.close();
    }
}
