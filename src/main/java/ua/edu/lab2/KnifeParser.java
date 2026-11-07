package ua.edu.lab2;

import java.util.List;

public interface KnifeParser {
    List<Knife> parse(byte[] xml) throws Exception;
}
