package client;

import iso.ISO8583Builder;
import iso.ISO8583Parser;
import iso.ISOMessage;
import util.ByteUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public final class TestClient {
    private final ISO8583Builder builder=new ISO8583Builder();
    private final ISO8583Parser parser=new ISO8583Parser();
    private final InputStream in;
    private final OutputStream out;

    private TestClient(Socket socket) throws IOException {
        this.in=new BufferedInputStream(socket.getInputStream());
        this.out=new BufferedOutputStream(socket.getOutputStream());
    }

    public static void main(String[] args) throws IOException {
        String host=  args.length>0?args[0]:"127.0.0.1";
        int port=args.length>1?Integer.parseInt(args[1]):8583;
        try(Socket socket=new Socket(host,port)){
            socket.setTcpNoDelay(true);
            TestClient testClient=new TestClient(socket);

            System.out.println("== Sale $250.00 on card ending 1111 (funded) ==");
            client.exchange(client.sale("4111111111111111", "000025000", "000001"));

            System.out.println("== Sale $600.00 on card ending 2222 (only $50) ==");
            client.exchange(client.sale("4222222222222222", "000060000", "000002"));

            System.out.println("== Balance inquiry on card ending 1111 ==");
            client.exchange(client.balanceInquiry("4111111111111111", "000003"));

            System.out.println("== Reversal of the first sale (STAN 000001) ==");
            client.exchange(client.reversal("4111111111111111", "000001"));

            System.out.println("== Balance inquiry after reversal ==");
            client.exchange(client.balanceInquiry("4111111111111111", "000004"));
        }
    }

    private ISOMessage sale(String pan, String amount, String stan){
        ISOMessage m=new ISOMessage("0200");
        m.set(2,pan);
        m.set(3, "000000");
        m.set(4, pad12(amount));
        m.set(7, "0610120000");
        m.set(11, stan);
        m.set(37, rrn(stan));
        m.set(41, "TERM0001");
        m.set(49, "840");
        return m;
    }

    private ISOMessage balanceInquiry(String pan, String stan) {
        ISOMessage m = new ISOMessage("0100");
        m.set(2, pan);
        m.set(3, "310000");
        m.set(4, "000000000000");
        m.set(11, stan);
        m.set(37, rrn(stan));
        m.set(41, "TERM0001");
        m.set(49, "840");
        return m;
    }

    private ISOMessage reversal(String pan, String originalStan) {
        ISOMessage m = new ISOMessage("0400");
        m.set(2, pan);
        m.set(3, "000000");
        m.set(4, "000000000000");
        m.set(11, originalStan);
        m.set(37, rrn(originalStan));
        m.set(41, "TERM0001");
        m.set(49, "840");
        return m;
    }

    private static String pad12(String digits) {
        return String.format("%12s", digits).replace(' ','0');
    }
    private static String rrn(String stan) {
        String base= "RNN"+stan;
        return String.format("%-12s",base).replace(' ','0');
    }

    private void exchange(ISOMessage request) throws IOException {
        byte[] body= builder.build(request);
        out.write(String.format("%04d", body.length).getBytes(StandardCharsets.US_ASCII));
        out.write(body);
        out.flush();
        System.out.println(" ->"+ request);

        byte[] lengthBuf= new byte[4];
        if(!ByteUtil.readFully(in, lengthBuf)){
            System.out.println(" <- (connection closed by host)");
            return;
        }
        int len= Integer.parseInt(new String(lengthBuf, StandardCharsets.US_ASCII));
        byte[] respBody= new byte[len];
        ByteUtil.readFully(in, respBody);
        ISOMessage response= parser.parse(respBody);
        System.out.println(" <-"+ response+ " [DE39=" +response.get(39)+ "]");
        System.out.println();
    }
}
