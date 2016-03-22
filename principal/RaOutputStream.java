package raf.principal;

import java.io.*;
import java.net.URL;



public class RaOutputStream extends ObjectOutputStream
{
    public RaOutputStream(OutputStream in) throws IOException{
        super(in);
    }
}