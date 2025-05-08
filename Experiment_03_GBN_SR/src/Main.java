import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("用法：");
            System.out.println("  接收: java Main recv <port>");
            System.out.println("  发送: java Main send <host> <port> <message>");
            return;
        }
        if ("recv".equals(args[0])) {
            int port = Integer.parseInt(args[1]);
            System.out.println("接收端启动，监听端口 " + port);
            new Receiver(port).receive();
        } else if ("send".equals(args[0])) {
            String host = args[1];
            int port = Integer.parseInt(args[2]);
            String msg = args[3];
            byte[] all = msg.getBytes(StandardCharsets.UTF_8);
            int mss = 5;
            List<Packet> pkts = new ArrayList<>();
            int seq = 0;
            for (int i = 0; i < all.length; i += mss) {
                int end = Math.min(all.length, i + mss);
                byte[] part = new byte[end - i];
                System.arraycopy(all, i, part, 0, part.length);
                pkts.add(new Packet(seq++, part));
            }
            new Sender(host, port).send(pkts);
        }
    }
}
