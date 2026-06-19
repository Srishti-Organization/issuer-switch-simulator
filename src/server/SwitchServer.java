package server;

import domain.AccountRepository;
import iso.ISO8583Builder;
import iso.ISO8583Parser;
import processing.TransactionProcessor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class SwitchServer {
    private final int port;
    private final ExecutorService workers;
    private final ISO8583Parser parser= new ISO8583Parser();
    private final ISO8583Builder builder= new ISO8583Builder();
    private final TransactionProcessor processor;

    private volatile ServerSocket serverSocket;
    private volatile boolean running;
    public SwitchServer(int port, AccountRepository repository) {
        this.port=port;
        this.processor= new TransactionProcessor(repository);
        this.workers = Executors.newCachedThreadPool(namedDaemonFactory());
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(port));
        running= true;
        System.out.println("[server] issuer switch is listening on port " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "switch-shutdown"));
        try{
            while(running){
                Socket socket = serverSocket.accept();
                workers.submit(new ConnectionHandler(socket, parser, builder, processor));
            }
        }
        catch(IOException e){
            if(running){
                System.err.println("[server] socket accept loop failed" + e.getMessage());
            }
        }
        finally{
            stop();
        }
    }

    public synchronized void stop() {
        if(!running){
            return;
        }
        running= false;
        System.out.println("[server] shutting down");
        try{
            if(serverSocket != null){
                serverSocket.close();
            }
        }catch(IOException ignored){
        }
        workers.shutdown();
        try{
            if(!workers.awaitTermination(5, TimeUnit.SECONDS)){
                workers.shutdownNow();
            }
        } catch(InterruptedException ignored){
            workers.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    private static ThreadFactory namedDaemonFactory() {
        AtomicInteger seq= new AtomicInteger();
        return r->{
            Thread t = new Thread(r,"switch-conn-"+seq.incrementAndGet());
            t.setDaemon(true);
            return t;
        };
    }
}
