package server;

import entity.RPCRequest;
import entity.RPCResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private static final AtomicInteger atomicInteger = new AtomicInteger(1);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            // 将接收的消息转换为RPCRequest对象
            RPCRequest rpcRequest = (RPCRequest) msg;
            // 记录接收到的消息，并通过atomicInteger记录接收次数，使用getAndIncrement保证线程安全的计数
            logger.info("server receive msg: [{}] ,times:[{}]", rpcRequest, atomicInteger.getAndIncrement());
            // 构建响应消息
            RPCResponse messageFromServer = RPCResponse.builder().message("message from server").build();
            // 将响应消息发送回客户端，并获取ChannelFuture对象
            ChannelFuture f = ctx.writeAndFlush(messageFromServer);
            // 添加监听器以在消息发送完成后关闭连接
            f.addListener(ChannelFutureListener.CLOSE);
        } finally {
            // 无论处理过程是否成功，最后都释放msg对象占用的资源
            // Netty使用引用计数来管理内存，因此需要手动释放
            ReferenceCountUtil.release(msg);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("server catch exception", cause);
        ctx.close();
    }
}