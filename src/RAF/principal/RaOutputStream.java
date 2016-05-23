package raf.principal;

import java.io.*;


public class RaOutputStream extends ObjectOutputStream
{
    public RaOutputStream(OutputStream in) throws IOException{
        super(in);
    }
}