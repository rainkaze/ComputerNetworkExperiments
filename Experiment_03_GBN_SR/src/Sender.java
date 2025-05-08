import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class Sender {
    private DatagramSocket socket;
    private InetAddress receiverAddr;
    private int receiverPort;
    private int windowSize = 4;
    private int base = 0;
    private List<Packet> packetBuffer;
    private Timer[] timers;
    private double lossRate = 0.1;

    public Sender(String host, int port) throws Exception {
        socket = new DatagramSocket();
        receiverAddr = InetAddress.getByName(host);
        receiverPort = port;
        timers = new Timer[1000];
        socket.setSoTimeout(1000);
    }

    public void send(List<Packet> packets) throws Exception {
        packetBuffer = packets;
        int nextSeq = 0;
        while (base < packetBuffer.size()) {
            while (nextSeq < base + windowSize && nextSeq < packetBuffer.size()) {
                sendPacket(packetBuffer.get(nextSeq));
                startTimer(nextSeq);
                nextSeq++;
            }
            try {
                byte[] buf = new byte[4];
                DatagramPacket ackDp = new DatagramPacket(buf, buf.length);
                socket.receive(ackDp);
                int ackNum = ByteBuffer.wrap(ackDp.getData()).getInt();
                System.out.println("收到 ACK: " + ackNum);
                if (ackNum >= base) {
                    for (int i = base; i <= ackNum; i++) {
                        timers[i].cancel();
                    }
                    base = ackNum + 1;
                }
            } catch (SocketTimeoutException e) {
                System.out.println("超时，重传窗口内包 [" + base + "," + (nextSeq-1) + "]");
                for (int i = base; i < nextSeq; i++) {
                    sendPacket(packetBuffer.get(i));
                    restartTimer(i);
                }
            }
        }
        System.out.println("所有数据包发送完成");
        socket.close();
    }

    private void sendPacket(Packet p) throws IOException {
        if (Math.random() < lossRate) {
            System.out.println("模拟丢包 seq=" + p.seqNum);
            return;
        }
        byte[] data = p.toBytes();
        DatagramPacket dp = new DatagramPacket(data, data.length, receiverAddr, receiverPort);
        socket.send(dp);
        System.out.println("发送包 seq=" + p.seqNum);
    }

    private void startTimer(int seq) {
        timers[seq] = new Timer();
        timers[seq].schedule(new RetransmitTask(seq), 500);
    }

    private void restartTimer(int seq) {
        timers[seq].cancel();
        startTimer(seq);
    }

    private class RetransmitTask extends TimerTask {
        private int seq;
        public RetransmitTask(int seq) { this.seq = seq; }
        @Override
        public void run() {
            try {
                System.out.println("定时器触发，重传 seq=" + seq);
                sendPacket(packetBuffer.get(seq));
                restartTimer(seq);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
