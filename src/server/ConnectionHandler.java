package server;

import iso.ISO8583Builder;
import iso.ISO8583Parser;
import iso.ISOMessage;
import processing.TransactionProcessor;
import util.ByteUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

final class ConnectionHandler implements Runnable {
    private static final int LENGTH_PREFIX_BYTES = 4;
    private static final int MAX_BODY_BYTES=64*1024;
    private final Socket socket;
    private final ISO8583Parser parser;
    private final ISO8583Builder builder;
    private final TransactionProcessor processor;

    ConnectionHandler(Socket socket, ISO8583Parser parser, ISO8583Builder builder, TransactionProcessor processor) {
        this.socket = socket;
        this.parser = parser;
        this.builder = builder;
        this.processor = processor;
    }

    @Override
    public void run() {
        String peer = socket.getRemoteSocketAddress().toString();
        System.out.println("[conn] open " + peer);
        try{
            socket.setKeepAlive(true);
            socket.setTcpNoDelay(true);
            InputStream in = new BufferedInputStream(socket.getInputStream());
            OutputStream out = new BufferedOutputStream(socket.getOutputStream());

            byte[] lengthBuf=new byte[LENGTH_PREFIX_BYTES];
            while(true){
                if(!ByteUtil.readFully(in,lengthBuf)){
                    break;
                }
                int bodyLength= parseLength(lengthBuf);
                if(bodyLength<0 || bodyLength>MAX_BODY_BYTES){
                    throw new IOException("Illegal body length: " + bodyLength);
                }
                byte[] body = new byte[bodyLength];
                if(!ByteUtil.readFully(in,body)){
                    throw new IOException("EOF while reading message body");
                }
                ISOMessage request= parser.parse(body);
                System.out.println("[recv] " + peer + " "+ request);
                ISOMessage response=processor.process(request);
                System.out.println("[send] " + peer + " "+ response);
                byte[] respBody=builder.build(response);
                out.write(formatLength(respBody.length));
                out.write(respBody);
                out.flush();
            }
        }catch(IOException e){
            System.out.println("[conn] error " + peer + ": " + e.getMessage());
        }catch(RuntimeException e){
            System.out.println("[conn] fault " + peer + ": " + e);
        }finally{
            closeQuietly();
            System.out.println("[conn] close "+ peer);
        }
    }

    private int parseLength(byte[] prefix) throws IOException {
        String s= new String(prefix, StandardCharsets.US_ASCII);
        try{
            return Integer.parseInt(s);
        } catch(NumberFormatException e){
            throw new IOException("Non-numeric length prefix: " + s+ " ");
        }
    }

    private byte[] formatLength(int len){
        return String.format("04%d",len).getBytes(StandardCharsets.US_ASCII);
    }
    private void closeQuietly(){
        try{
            socket.close();
        }
        catch(IOException ignored){

        }
    }
}
