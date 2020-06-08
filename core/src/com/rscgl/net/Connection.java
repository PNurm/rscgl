package com.rscgl.net;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Connection implements Runnable {

    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    private boolean isClosed;
    private boolean isClosing;

    private int writerOffset;
    private byte[] writerBuffer;
    private int writerLength;

    private int readerReadAttempts = 0;
    private int readerPacketLength = 0;
    private byte[] readerTmpByte = new byte[1];

    private boolean ioError;
    private String ioMessage;

    public Connection(String ip, int port) throws IOException {
        this.socket = openSocket(port, ip);
        this.socket.setSoTimeout(30000);
        this.socket.setTcpNoDelay(true);
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();

        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    private Socket openSocket(int port, String host) throws IOException {
        Socket socket = new Socket(InetAddress.getByName(host), port);
        socket.setSoTimeout(30000);
        socket.setTcpNoDelay(true);
        return socket;
    }

    public void write(byte[] src, int off, int length) throws IOException {
        if (isClosing) {
            return;
        }
        if (writerBuffer == null)
            writerBuffer = new byte[5000];
        synchronized (this) {
            for (int i = 0; i < length; i++) {
                writerBuffer[writerLength] = src[i + off];
                writerLength = (writerLength + 1) % 5000;
                if (writerLength == (writerOffset + 4900) % 5000)
                    throw new IOException("buffer overflow");
            }
            notify();
        }
    }

    @Override
    public void run() {
        //This thread is solely for writing to outStream, reading is done by client main thread.
        while (!isClosed) {
            int length;
            int offset;
            synchronized (this) {
                //We've written all there is, wait for more data.
                if (writerLength == writerOffset) {
                    try {
                        wait();
                    } catch (InterruptedException Ex) { }
                }

                if (isClosed) {
                    return;
                }
                //Get the offset and length for next write
                offset = writerOffset;
                if (writerLength >= writerOffset)
                    length = writerLength - writerOffset;
                else
                    length = 5000 - writerOffset;
            }

            if (length > 0) {
                //Write to stream as long as there's data to write.
                try {
                    outputStream.write(writerBuffer, offset, length);
                } catch (IOException ioexception) {
                    ioError = true;
                    ioMessage = "Twriter:" + ioexception;
                }

                //Increase offset by what we just wrote, overflow to 0 at 5000 bytes.
                writerOffset = (writerOffset + length) % 5000;

                try {
                    //Flush when we've got everything written to stream.
                    if (writerLength == writerOffset) {
                        outputStream.flush();
                    }
                } catch (IOException ioexception1) {
                    ioError = true;
                    ioMessage = "Twriter:" + ioexception1;
                }
            }
        }
    }

    public void close() {
        isClosing = true;
        try {
            if(inputStream != null) {
                inputStream.close();
            }
            if(outputStream != null) {
                outputStream.close();
            }
            if(socket != null) {
                socket.close();
            }
        } catch (IOException ioexception) {
            System.out.println("Error closing stream");
        }

        isClosed = true;
        synchronized (this) {
            notify();
        }
        writerBuffer = null;
    }

    public int read() throws IOException {
        if (isClosing) {
            return 0;
        }

        this.read(this.readerTmpByte, 0, 1);
        return 255 & this.readerTmpByte[0];
    }

    public int available() throws IOException {
        if (isClosing) {
            return 0;
        }
        return inputStream.available();
    }

    public void read(byte[] src, int offset, int length) throws IOException {
        if (!isClosing) {
            int byteRead;
            for (/**/; length > 0; length -= byteRead) {
                byteRead = inputStream.read(src, offset, length);
                if (byteRead <= 0) {
                    throw new IOException("EOF");
                }
                offset += byteRead;
            }
        }
    }

    public int decodePacket(byte[] buffer) {
        try {
            readerReadAttempts++;
            if (readerReadAttempts > 100000) {
                ioError = true;
                ioMessage = "time-out";
                return 0;
            }
            if (readerPacketLength == 0 && available() >= 2) {
                readerPacketLength = ((short) (read() << 8) | (short) read());
                readerPacketLength -= 2;
            }
            if (this.readerPacketLength > 0 && this.available() >= this.readerPacketLength) {
                read(buffer, 0, readerPacketLength);
                int packetLength = readerPacketLength;
                readerPacketLength = 0;
                readerReadAttempts = 0;
                return packetLength;
            }
        } catch (IOException e) {
            ioError = true;
            ioMessage = e.getMessage();
        }
        return 0;
    }


    public boolean isClosed() {
        return isClosed;
    }

    public void write(PacketBuffer outBuffer) throws IOException {
        if (ioError) {
            ioError = false;
            throw new IOException(ioMessage);
        }
        write(outBuffer.dataBuffer, 0, outBuffer.offset);
    }
}
