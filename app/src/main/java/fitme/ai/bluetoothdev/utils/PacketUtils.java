package fitme.ai.bluetoothdev.utils;

import android.util.Log;


import java.io.UnsupportedEncodingException;

/**
 * sockect组包与拆包方法
 * Created by yml on 2018/1/4.
 */

public class PacketUtils {

    private static PacketUtils instance;
    public static PacketUtils getInstance(){
        if(instance==null){
            instance=new PacketUtils();
        }
        return instance;
    }
    /**
     * 封装socket发送的包
     * header + body
     * @param msgBody
     */
    public byte[] getSendPacket(int commondId, String msgBody, int packetId){
        byte[] buffer=null;
        //发送包长度
        int bodyLength=0;
        byte[] bBytes=null;
        try {
            if(msgBody!=null){
                bBytes=msgBody.getBytes("UTF-8");
                bodyLength=bBytes.length;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //拼接header
        buffer=intToBytes(12+bodyLength);//包总长度
        byte[] commondBytes=intToBytes(commondId);//Command_Id
        buffer=appendBytes(buffer,commondBytes);//拼接
        int packetIdNum=(int) System.currentTimeMillis();
        Log.i("aaaa","------System.currentTimeMillis()-----"+ System.currentTimeMillis());
        if(packetId>0){
            packetIdNum=packetId;
            Log.i("aaaa","------packetIdNum-----"+packetIdNum);
        }
        Log.i("ssss","------packetIdNum-----"+packetIdNum);
        byte[] packetIdBytes=intToBytes(packetIdNum);//包Id（建议采用时间戳取整的方式）
        buffer=appendBytes(buffer,packetIdBytes);//拼接
        //拼接body
        if(msgBody!=null){
            buffer=appendBytes(buffer,bBytes);
        }
        return buffer;
    }

    /**
     * 16进制 转 字节数组
     * @param data
     */
    public byte[] intToBytes(int data) {
        byte[] intBuf = new byte[4];
        intBuf[3] = (byte) (data & 0xff);
        intBuf[2] = (byte) ((data >>> 8) & 0xff);
        intBuf[1] = (byte) ((data >>> 16) & 0xff);
        intBuf[0] = (byte) ((data >>> 24) & 0xff);
        Log.i("ssss",intBuf[0]+"--"+intBuf[1]+"--"+intBuf[2]+"--"+intBuf[3]);
        return intBuf;
    }

    /**
     * 字节数组 转 16进制
     * @return
     */
    public String getHexDump(byte[] buffer) {
        String dump = "";
        try {
            int dataLen = buffer.length;
            for (int i = 0; i < dataLen; i++) {
                dump += Character.forDigit((buffer[i] >> 4) & 0x0f, 16);
                dump += Character.forDigit(buffer[i] & 0x0f, 16);
            }
        } catch (Throwable t) {
            // catch everything as this is for debug
            dump = "Throwable caught when dumping = " + t;
        }
        return dump;
    }

    /**
     * 字节数组中获取从起始位fromIdx开始的对应长度段的int型数字
     * @param buffer
     * @param fromIdx
     * @return
     * @throws NotEnoughDataInByteBufferException
     */
    public int readInt(byte[] buffer,int fromIdx,int readlength) throws NotEnoughDataInByteBufferException {
        int result = 0;
        int len = buffer.length;
        if (len >= (fromIdx+readlength)) {
            for(int i=fromIdx;i<fromIdx+readlength;i++){
                if(i > fromIdx){
                    result <<= 8;
                }
                result |= buffer[i] & 0xff;
            }
            return result;
        }else {
            throw new NotEnoughDataInByteBufferException(len, 4);
        }
    }

    /**
     * 将数组buffer添加到desBytes数组后面
     * @param desBytes
     * @param buffer
     * @return
     */
    public byte[] appendBytes(byte[] desBytes,byte[] buffer){
        int len=0;
        if(desBytes!=null){
            len=desBytes.length;
        }
        byte[] newBuf = new byte[len + buffer.length];
        if (len > 0) {
            System.arraycopy(desBytes, 0, newBuf, 0, len);
        }
        if(buffer!=null){
            System.arraycopy(buffer, 0, newBuf, len, buffer.length);
        }
        return newBuf;
    }

    /**
     * 根据包起始位置及编码格式，获取包体
     * @param buffer
     * @param fromIdx
     * @param encodeTypeStr
     * @return
     */
    public String getBodyStr(byte[] buffer, int fromIdx, String encodeTypeStr){
        if(buffer==null || (buffer.length<=fromIdx )){
            return null;
        }
        String bodyMsg=null;
        int msgLength=0;
        try {
            msgLength=readInt(buffer,0,4)-12;
            Log.i("ssss","=====Command_Id:"+ Integer.toHexString(readInt(buffer,4,4)));
            Log.i("ssss","=====Packet_Id:"+readInt(buffer,8,4));
            Log.i("ssss","msgLength:"+msgLength);
        } catch (NotEnoughDataInByteBufferException e) {
            e.printStackTrace();
        }

        try {
            if(msgLength>12){
                bodyMsg=new String(buffer,fromIdx,msgLength,encodeTypeStr);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return bodyMsg;
    }

    /**
     * 获取字节数组的总长度
     * @param buffer
     * @return
     */
    public int getSocketBodyLength(byte[] buffer){
        int totalLength=0;
        try {
            totalLength=readInt(buffer,0,4);
            Log.i("ssss","totalLength:"+totalLength);
        } catch (NotEnoughDataInByteBufferException e) {
            e.printStackTrace();
        }
        return totalLength;
    }
}
