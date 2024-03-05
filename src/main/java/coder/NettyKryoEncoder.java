package coder;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import serialize.Serializer;

@AllArgsConstructor
public class NettyKryoEncoder extends MessageToByteEncoder {
    private final Serializer serializer;
    private final Class<?> genericClass;

    //将对象转换为字节码，并附带其长度存入ByteBuf对象中
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (genericClass.isInstance(o)) {
            //1. 将对象转换为byte
            byte[] body = serializer.serialize(o);
            //2. 读取消息的长度
            int dataLength = body.length;
            //3. 写入消息对应的字节数组长度，writerIndex加4
            byteBuf.writeInt(dataLength);
            //4. 将字节数组写入ByteBuf对象
            byteBuf.writeBytes(body);
        }
    }
}
