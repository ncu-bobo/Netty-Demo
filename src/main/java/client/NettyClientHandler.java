package client;

import entity.RPCResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            RPCResponse rpcResponse = (RPCResponse) msg;
            logger.info("client receive msg: [{}]", rpcResponse.toString());
            // 声明一个 AttributeKey 对象
            AttributeKey<RPCResponse> key = AttributeKey.valueOf("rpcResponse");
            // 将服务端的返回结果保存到 AttributeMap 上，AttributeMap 可以看作是一个Channel的共享数据源
            // AttributeMap的key是AttributeKey，value是Attribute
            ctx.channel().attr(key).set(rpcResponse);
            ctx.channel().close();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
