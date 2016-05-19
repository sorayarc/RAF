package org.kaariboga.core;

import java.io.*;
import java.net.URL;


/**
 *  Writes a kaariboga objcect with the class file to the stream.
 *  Really does nothing special at the moment, but should be
 *  used in case Kaariboga uses another kind of serialization
 *  in the future.
 */
public class KaaribogaOutputStream extends ObjectOutputStream
{
    public KaaribogaOutputStream(OutputStream in) throws IOException{
        super(in);
    }
}