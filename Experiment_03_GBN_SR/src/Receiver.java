import java.net.*;
import java.nio.ByteBuffer;

public class Receiver {
    private DatagramSocket socket;
    private int expectSeq = 0;
    private double lossRate = 0.1;

    public Receiver(int port) throws Exception {
        socket = new DatagramSocket(port);
    }

    public void receive() throws Exception {
        byte[] buf = new byte[2048];
        while (true) {
            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            socket.receive(dp);
            Packet p = Packet.fromBytes(dp.getData());
            if (!p.isValid()) {
                System.out.println("校验失败，丢弃 seq=" + p.seqNum);
                continue;
            }
            System.out.println("接收 seq=" + p.seqNum);
            if (p.seqNum == expectSeq) {
                System.out.println("交付: " + new String(p.data));
                expectSeq++;
            } else {
                System.out.println("乱序 seq=" + p.seqNum + "，期望 " + expectSeq);
            }
            int ackNum = expectSeq - 1;
            if (Math.random() < lossRate) {
                System.out.println("模拟 ACK 丢失 ACK=" + ackNum);
                continue;
            }
            byte[] ackBuf = ByteBuffer.allocate(4).putInt(ackNum).array();
            DatagramPacket ackDp = new DatagramPacket(
                    ackBuf, ackBuf.length, dp.getAddress(), dp.getPort());
            socket.send(ackDp);
            System.out.println("发送 ACK=" + ackNum);
        }
    }
}
