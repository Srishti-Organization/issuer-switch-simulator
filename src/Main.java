import server.SwitchServer;

import java.io.IOException;

public final class Main {
    private static final int DEFAULT_PORT=8583;
    private Main(){
    }
    public static void main(String[] args) throws IOException{
        int port =DEFAULT_PORT;
        if(args.length>0){
            port=Integer.parseInt(args[0]);
        }
        AccountRepository repository = new AccountRepository();
        System.out.println("[boot] seeded"+repository.size()+"mock accounts");
        SwitchServer server= new SwitchServer(port,repository);
        server.start();
    }
}