import java.nio.ByteBuffer;

public class Packet {
    public int seqNum;         // 序列号
    public byte[] data;        // 有效载荷
    public int checksum;       // 校验和

    public Packet(int seqNum, byte[] data) {
        this.seqNum = seqNum;
        this.data = data;
        this.checksum = computeChecksum();
    }

    private int computeChecksum() {
        int sum = seqNum;
        for (byte b : data) sum += (b & 0xFF);
        return ~sum;
    }

    public boolean isValid() {
        return computeChecksum() == checksum;
    }

    public byte[] toBytes() {
        ByteBuffer buf = ByteBuffer.allocate(4 + 4 + data.length);
        buf.putInt(seqNum);
        buf.putInt(checksum);
        buf.put(data);
        return buf.array();
    }

    public static Packet fromBytes(byte[] buf) {
        ByteBuffer bb = ByteBuffer.wrap(buf);
        int seq = bb.getInt();
        int chksum = bb.getInt();
        byte[] payload = new byte[bb.remaining()];
        bb.get(payload);
        Packet p = new Packet(seq, payload);
        p.checksum = chksum;
        return p;
    }
}
