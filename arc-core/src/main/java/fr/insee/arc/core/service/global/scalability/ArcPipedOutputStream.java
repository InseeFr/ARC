package fr.insee.arc.core.service.global.scalability;

import java.io.IOException;
import java.io.OutputStream;

public class ArcPipedOutputStream extends OutputStream

{

  ArcPipedInputStream sink;



  /**
   * Creates an unconnected ArcPipedOutputStream.
   */

  public
  ArcPipedOutputStream() throws IOException
  {
    this(null);
  }



  /**
   * Creates a ArcPipedOutputStream with a default buffer size and connects it to
   * <code>sink</code>.
   * @exception IOException It was already connected.
   */

  public
  ArcPipedOutputStream(ArcPipedInputStream sink) throws IOException
  {
    this(sink, 0x10000);
  }



  /**
   * Creates a ArcPipedOutputStream with buffer size <code>bufferSize</code> and
   * connects it to <code>sink</code>.
   * @exception IOException It was already connected.
   */

  public
  ArcPipedOutputStream(ArcPipedInputStream sink, int bufferSize) throws IOException
  {
    if (sink != null)
    {
      connect(sink);
      sink.buffer = new byte[bufferSize];
    }
  }



  /**
   * @exception IOException The pipe is not connected.
   */

  public void
  close() throws IOException
  {
    if (sink == null)
    {
      throw new IOException("Unconnected pipe");
    }

    synchronized (sink.buffer)
    {
      sink.closed = true;
      flush();
    }
  }



  /**
   * @exception IOException The pipe is already connected.
   */

  public void
  connect(ArcPipedInputStream sink) throws IOException
  {
    if (this.sink != null)
    {
      throw new IOException("Pipe already connected");
    }

    this.sink = sink;
    sink.source = this;
  }



  protected void
  finalize() throws Throwable
  {
    close();
  }



  public void
  flush() throws IOException
  {
    synchronized (sink.buffer)
    {
      // Release all readers.
      sink.buffer.notifyAll();
    }
  }



  public void
  write(int b) throws IOException
  {
    write(new byte[] {(byte) b});
  }



  public void
  write(byte[] b) throws IOException
  {
    write(b, 0, b.length);
  }



  /**
   * @exception IOException The pipe is not connected or a reader has closed it.
   */

  public void
  write(byte[] b, int off, int len) throws IOException
  {
    if (sink == null)
    {
      throw new IOException("Unconnected pipe");
    }

    if (sink.closed)
    {
      throw new IOException("Broken pipe");
    }

    synchronized (sink.buffer)
    {
      if
      (
        sink.writePosition == sink.readPosition &&
        sink.writeLaps > sink.readLaps
      )
      {
        // The circular buffer is full, so wait for some reader to consume
        // something.

        try
        {
          sink.buffer.wait();
        }

        catch (InterruptedException e)
        {
          throw new IOException(e.getMessage());
        }

        // Try again.

        write(b, off, len);

        return;
      }

      // Don't write more than the capacity indicated by len or the space
      // available in the circular buffer.

      int amount =
        Math.min
        (
          len,
          (
            sink.writePosition < sink.readPosition ?
              sink.readPosition : sink.buffer.length
          ) - sink.writePosition
        );

      System.arraycopy(b, off, sink.buffer, sink.writePosition, amount);
      sink.writePosition += amount;

      if (sink.writePosition == sink.buffer.length)
      {
        sink.writePosition = 0;
        ++sink.writeLaps;
      }

      // The buffer is only released when the complete desired block was
      // written.

      if (amount < len)
      {
        write(b, off + amount, len - amount);
      }
      else
      {
        sink.buffer.notifyAll();
      }
    }
  }

}