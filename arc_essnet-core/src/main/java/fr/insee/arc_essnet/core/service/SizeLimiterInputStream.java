package fr.insee.arc_essnet.core.service;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SizeLimiterInputStream extends FilterInputStream {

    final long maxSize;
    final InputStream base;
    long alreadyRead;

    public SizeLimiterInputStream(InputStream in, long maxSize) {
      super(in);
      this.maxSize = maxSize;
      this.alreadyRead = 0;
      this.base = in;
    }

    @Override
    public synchronized int available() throws IOException {
      long a = this.base.available();
      if (this.alreadyRead + a > this.maxSize)
        a = this.maxSize - this.alreadyRead;
      return (int)a;
    }

    @Override
    public void close() {
      // do nothing
    }

    @Override
    public boolean markSupported() {
      return false;
    }

    @Override
    public void mark(int readlimit) {
      // do nothing
    }

    @Override
    public void reset() throws IOException {
      // do nothing
    }

    @Override
    public synchronized int read() throws IOException {
      if (this.alreadyRead >= this.maxSize)
        throw new EOFException();
      int r = this.base.read();
      this.alreadyRead += 1;
      return r;
    }

    @Override
    public synchronized int read(byte[] b) throws IOException {
      return read(b, 0, b.length);
    }

    @Override
    public synchronized int read(byte[] b, int off, int len2)
            throws IOException
    {
        int len = len2;
        if (this.alreadyRead >= this.maxSize) return -1;
        if (this.alreadyRead + len > this.maxSize)
            len = (int) (this.maxSize - this.alreadyRead);
        int r = this.base.read(b, off, len);
        this.alreadyRead += r;
        return r;
    }

    @Override
    public synchronized long skip(long n2) throws IOException
    {
        long n = n2;
        if (n < 0) return 0;
        if (this.alreadyRead >= this.maxSize) return 0;
        if (this.alreadyRead + n > this.maxSize)
            n = this.maxSize - this.alreadyRead;
        long r = this.base.skip(n);
        this.alreadyRead += r;
        return r;
    }

  }