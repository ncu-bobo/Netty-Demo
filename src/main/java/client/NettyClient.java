package client;

import coder.NettyKryoDecoder;
import coder.NettyKryoEncoder;
import entity.RPCRequest;
import entity.RPCResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serialize.KryoSerializer;

public class NettyClient {
    //地址输出
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);
    private final String host; //主机地址
    private int port; //端口
    private static Bootstrap b;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    //初始化资源
    static {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        b = new Bootstrap();
        KryoSerializer kryoSerializer = new KryoSerializer();
        b.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                // 连接的超时时间，超过这个时间还是建立不上的话则代表连接失败
                //  如果 15 秒之内没有发送数据给服务端的话，就发送一次心跳请求
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        /*
                         自定义序列化编解码器
                         */
                        // RpcResponse -> ByteBuf
                        ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RPCResponse.class));
                        // ByteBuf -> RpcRequest
                        ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RPCRequest.class));
                        ch.pipeline().addLast(new NettyClientHandler());
                    }
                });
    }

    // 发送消息
    public RPCResponse sendMessage(RPCRequest rpcRequest) {
        try {
            // 尝试连接到服务器并同步等待直到连接完成
            ChannelFuture f = b.connect(host, port).sync();
            // 记录连接成功的信息
            logger.info("client connect  {}", host + ":" + port);
            // 从ChannelFuture获取Channel以用于通信
            Channel futureChannel = f.channel();
            // 记录发送消息的日志
            logger.info("send message");
            // 确保channel不为空
            if (futureChannel != null) {
                // 向服务器发送RPC请求，并添加监听器以处理消息发送成功或失败的情况
                futureChannel.writeAndFlush(rpcRequest).addListener(future -> {
                    // 如果发送成功，则记录发送成功的日志
                    if (future.isSuccess()) {
                        logger.info("client send message: [{}]", rpcRequest.toString());
                    } else {
                        // 如果发送失败，则记录错误信息和原因
                        logger.error("Send failed:", future.cause());
                    }
                });
                // 等待通道关闭，这是一个阻塞操作
                futureChannel.closeFuture().sync();
                // 使用自定义属性键获取RPC响应
                AttributeKey<RPCResponse> key = AttributeKey.valueOf("rpcResponse");
                // 返回RPC响应
                return futureChannel.attr(key).get();
            }
        } catch (InterruptedException e) {
            // 如果连接或等待过程中出现异常，则记录错误信息
            logger.error("occur exception when connect server:", e);
        }
        // 如果发生异常或通道为null，则返回null
        return null;
    }


    public static void main(String[] args) {
        // 构建好RPCRequest类
        RPCRequest rpcRequest = RPCRequest.builder()
                .interfaceName("interface")
                .methodName("hello").build();
        // 初始化client
        NettyClient nettyClient = new NettyClient("127.0.0.1", 8889);
        //连续发送四次信息
        for (int i = 0; i < 3; i++) {
            nettyClient.sendMessage(rpcRequest);
        }
        //输出最后一次得到的结果
        RPCResponse rpcResponse = nettyClient.sendMessage(rpcRequest);
        System.out.println(rpcResponse.toString());
    }
}
